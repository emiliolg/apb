

// Copyright 2008-2009 Emilio Lopez-Gabeiras
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License
//


package apb.tasks;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import apb.BuildException;
import apb.Messages;

import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;

import static java.util.Arrays.asList;

import static apb.utils.StringUtils.isNotEmpty;
//
// User: emilio
// Date: Sep 9, 2008
// Time: 3:01:10 PM

//
public class JarTask
    extends Task
{
    //~ Instance fields ......................................................................................

    private final boolean doCompress = true;
    private final File    jarFile;

    private final int                         level = Deflater.DEFAULT_COMPRESSION;
    private final List<FileSet>               sources;
    @NotNull private Manifest                 manifest;
    @NotNull private Map<String, Set<String>> services;

    private String comment;

    //~ Constructors .........................................................................................

    private JarTask(@NotNull File jarFile, @NotNull List<FileSet> sources)
    {
        this.jarFile = jarFile;
        this.sources = sources;
        services = Collections.emptyMap();
        manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().putValue("Created-By", "APB");
    }

    //~ Methods ..............................................................................................

    public JarTask version(@NotNull String version)
    {
        setManifestAttribute(Attributes.Name.IMPLEMENTATION_VERSION, version);
        return this;
    }

    public JarTask mainClass(String className)
    {
        if (isNotEmpty(className)) {
            setManifestAttribute(Attributes.Name.MAIN_CLASS, className);
        }

        return this;
    }

    public JarTask withClassPath(@NotNull String... fileNames)
    {
        return withClassPath(Arrays.asList(fileNames));
    }

    public JarTask withClassPath(@NotNull List<String> fileNames)
    {
        if (!fileNames.isEmpty()) {
            StringBuilder result = new StringBuilder();

            for (String fileName : fileNames) {
                if (result.length() != 0) {
                    result.append(' ');
                }

                File   f = env.fileFromBase(fileName);
                String entry = FileUtils.makeRelative(jarFile.getParentFile(), f).getPath();

                if (File.separatorChar != '/') {
                    entry = entry.replace(File.separatorChar, '/');
                }

                result.append(entry);
            }

            setManifestAttribute(Attributes.Name.CLASS_PATH, result.toString());
        }

        return this;
    }

    public JarTask manifestAttributes(Map<Attributes.Name, String> attributes)
    {
        for (Map.Entry<Attributes.Name, String> atts : attributes.entrySet()) {
            setManifestAttribute(atts.getKey(), atts.getValue());
        }

        return this;
    }

    public void setManifestAttribute(String name, String value)
    {
        setManifestAttribute(new Attributes.Name(name), value);
    }

    public void setManifestAttribute(Attributes.Name name, String value)
    {
        logVerbose("Set manifest attribute %s=%s\n", name, value);
        manifest.getMainAttributes().put(name, value);
    }

    public void setManifest(@NotNull InputStream is)
        throws IOException
    {
        manifest = new Manifest(is);
    }

    public void execute()
    {
        long                    jarTimeStamp = checkJarFile();
        Map<File, List<String>> files = new LinkedHashMap<File, List<String>>();

        for (FileSet fileSet : sources) {
            files.put(fileSet.getDir(), fileSet.list());
        }

        if (!uptodate(jarTimeStamp, files)) {
            buildJar(files);
        }
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public JarTask withServices(@NotNull Map<String, Set<String>> svcs)
    {
        services = svcs;
        return this;
    }

    /**
     * Check if the jar file is uptodate.
     * (The timestamp for all files is lower than the jar one)
     * @param jarTimeStamp The timestamp for the jar file
     * @param files The set of files to add
     * @return true if the jar is 'uptodate'
     */
    private static boolean uptodate(long jarTimeStamp, final Map<File, List<String>> files)
    {
        for (File dir : files.keySet()) {
            for (String fileName : files.get(dir)) {
                File file = new File(dir, fileName);

                // Check timestamps
                if (file.lastModified() > jarTimeStamp) {
                    return false;
                }
            }
        }

        return true;
    }

    private long checkJarFile()
    {
        long result = jarFile.lastModified();

        if (result == 0) {
            result = -1;
        }
        else if (!jarFile.isFile()) {
            throw new BuildException(jarFile + " is not a file.");
        }

        return result;
    }

    private void buildJar(Map<File, List<String>> files)
    {
        try {
            env.logInfo("Building: %s\n", FileUtils.normalizePath(jarFile));

            JarOutputStream jarOutputStream = null;

            boolean success = false;

            try {
                jarOutputStream = openJar();

                Set<String> addedDirs = new HashSet<String>();
                writeMetaInfEntries(jarOutputStream, addedDirs);

                boolean writeManifest = true;

                for (File dir : files.keySet()) {
                    for (String fileName : files.get(dir)) {
                        final File file = new File(dir, fileName);

                        if (file.length() != 0 && !file.isDirectory()) {
                            String normalizedName = fileName.replace(File.separatorChar, '/');

                            if (JarFile.MANIFEST_NAME.equalsIgnoreCase(normalizedName)) {
                                env.logWarning(Messages.MANIFEST_OVERRIDE(file));
                                writeManifest = false;
                            }

                            writeToJar(jarOutputStream, normalizedName, new FileInputStream(file), addedDirs);
                        }
                    }
                }

                if (writeManifest) {
                    writeManifest(jarOutputStream);
                }

                jarOutputStream.setComment(comment);
                success = true;
            }
            finally {
                closeJar(jarOutputStream, success);
            }
        }
        catch (IOException ioe) {
            jarFile.delete();
            throw new BuildException("Problem creating: " + jarFile + " " + ioe.getMessage(), ioe);
        }
    }

    private JarOutputStream openJar()
        throws IOException
    {
        FileUtils.validateDirectory(jarFile.getParentFile());

        if (jarFile.exists() && !jarFile.canWrite() && !jarFile.delete()) {
            throw new BuildException("Can not recreate: '" + jarFile + "'.");
        }

        final FileOutputStream os = new FileOutputStream(jarFile);
        final JarOutputStream  jarOutputStream = new JarOutputStream(os);
        jarOutputStream.setMethod(doCompress ? ZipOutputStream.DEFLATED : ZipOutputStream.STORED);
        jarOutputStream.setLevel(level);
        return jarOutputStream;
    }

    private void closeJar(JarOutputStream jarOutputStream, boolean success)
        throws IOException
    {
        if (jarOutputStream != null) {
            try {
                jarOutputStream.close();
            }
            catch (IOException ex) {
                if (success) {
                    throw ex;
                }
            }
        }
    }

    private void writeToJar(JarOutputStream jarOut, final String fileName, final InputStream is,
                            Set<String> addedDirs)
        throws IOException
    {
        writeParentDirs(jarOut, fileName, addedDirs);

        logVerbose("Adding entry... %s\n", fileName);

        JarEntry entry = new JarEntry(fileName);
        jarOut.putNextEntry(entry);

        byte[] arr = new byte[1024];
        int    n;

        while ((n = is.read(arr)) > 0) {
            jarOut.write(arr, 0, n);
        }

        is.close();
        jarOut.flush();
    }

    private void writeManifest(JarOutputStream jarOut)
        throws IOException
    {
        ZipEntry e = new ZipEntry(JarFile.MANIFEST_NAME);
        jarOut.putNextEntry(e);
        manifest.write(new BufferedOutputStream(jarOut));
        jarOut.closeEntry();
    }

    private void writeMetaInfEntries(JarOutputStream jarOut, Set<String> addedDirs)
        throws IOException
    {
        for (Map.Entry<String, Set<String>> e : services.entrySet()) {
            String        fileName = "META-INF/services/" + e.getKey();
            StringBuilder buff = new StringBuilder();

            for (String provider : e.getValue()) {
                buff.append(provider).append('\n');
            }

            writeToJar(jarOut, fileName, new ByteArrayInputStream(buff.toString().getBytes()), addedDirs);
        }
    }

    private void writeParentDirs(JarOutputStream jarOut, String fileName, Set<String> addedDirs)
        throws IOException
    {
        List<String> directories = new ArrayList<String>();
        int          slashPos = fileName.length();

        while ((slashPos = fileName.lastIndexOf('/', slashPos - 1)) != -1) {
            String dirName = fileName.substring(0, slashPos + 1);

            if (!addedDirs.contains(dirName)) {
                directories.add(dirName);
                addedDirs.add(dirName);
            }
        }

        for (int i = directories.size() - 1; i >= 0; i--) {
            String dirName = directories.get(i);
            logVerbose("Adding dir...   %s\n", dirName);
            JarEntry ze = new JarEntry(dirName);
            ze.setSize(0);
            ze.setMethod(ZipEntry.STORED);
            ze.setCrc(EMPTY_CRC);

            jarOut.putNextEntry(ze);
        }
    }

    //~ Static fields/initializers ...........................................................................

    private static final long EMPTY_CRC = new CRC32().getValue();

    //~ Inner Classes ........................................................................................

    public static class Builder
    {
        @NotNull private final File jarFile;

        /**
         * Private constructor called from factory methods
         * @param jarFile The jarfile to be created
         */

        Builder(@NotNull File jarFile)
        {
            this.jarFile = jarFile;
        }

        /**
         * Build the jar from the specified directories
         * @param sourceDirectories
         */
        public JarTask from(@NotNull File... sourceDirectories)
        {
            List<FileSet> sets = new ArrayList<FileSet>();

            for (File dir : sourceDirectories) {
                sets.add(FileSet.fromDir(dir));
            }

            return from(sets);
        }

        /**
         * Build the jar from the specified filesets
         * @param fileSets
         */
        public JarTask from(@NotNull FileSet... fileSets)
        {
            return from(asList(fileSets));
        }

        /**
         * Build the jar from the specified list of filesets
         * @param fileSets
         */
        public JarTask from(List<FileSet> fileSets)
        {
            return new JarTask(jarFile, fileSets);
        }
    }
}
