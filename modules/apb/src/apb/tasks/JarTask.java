

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
import apb.Environment;
import apb.Messages;
import apb.ModuleHelper;

import apb.metadata.Dependency;
import apb.metadata.PackageInfo;
import apb.metadata.PackageType;

import apb.utils.DirectoryScanner;
import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Sep 9, 2008
// Time: 3:01:10 PM

//
public class JarTask
    extends Task
{
    //~ Instance fields ......................................................................................

    private String       comment;
    private boolean      doCompress = true;
    private List<String> excludes, includes;
    private File         jarFile;

    private int                               level = Deflater.DEFAULT_COMPRESSION;
    @NotNull private Manifest                 manifest;
    @NotNull private Map<String, Set<String>> services;
    private List<File>                        sourceDir;

    //~ Constructors .........................................................................................

    public JarTask(@NotNull Environment env, @NotNull File jarFile)
    {
        super(env);
        this.jarFile = jarFile;
        sourceDir = new ArrayList<File>();
        excludes = Collections.emptyList();
        includes = Arrays.asList("**/**");
        services = Collections.emptyMap();
        manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().putValue("Created-By", "APB");
    }

    //~ Methods ..............................................................................................

    public static void execute(@NotNull Environment env)
    {
        ModuleHelper      helper = env.getModuleHelper();
        final PackageInfo packageInfo = helper.getPackageInfo();

        if (packageInfo.type != PackageType.NONE) {
            JarTask jarTask = new JarTask(env, helper.getPackageFile());
            jarTask.addDir(helper.getOutput());
            final String mainClass = packageInfo.mainClass;

            if (mainClass != null && !mainClass.isEmpty()) {
                jarTask.setManifestAttribute(Attributes.Name.MAIN_CLASS, mainClass);
            }

            if (packageInfo.addClassPath) {
                jarTask.setManifestAttribute(Attributes.Name.CLASS_PATH, helper.classFileForManifest());
            }

            jarTask.setManifestAttribute(Attributes.Name.IMPLEMENTATION_VERSION, helper.getModule().version);

            jarTask.addManifestAttributes(packageInfo.attributes());

            if (!packageInfo.includeDependencies().isEmpty()) {
                for (Dependency d : packageInfo.includeDependencies()) {
                    if (d.isModule()) {
                        ModuleHelper m = (ModuleHelper) env.getHelper(d.asModule());
                        if (m.hasPackage())
                            jarTask.addDir(m.getPackageFile());
                    }
                    else if (d.isLibrary()) {
                        for (File file : d.asLibrary().getFiles(env)) {
                            jarTask.addDir(file);
                        }
                    }
                }
            }

            jarTask.setServices(packageInfo.services());
            jarTask.execute();

            if (packageInfo.generateSourcesJar) {
                jarTask = new JarTask(helper.getEnv(), helper.getSourcePackageFile());
                jarTask.addDir(helper.getSource());
                jarTask.setExcludes(FileUtils.DEFAULT_EXCLUDES);
                jarTask.execute();
            }
        }
    }

    public void setManifestAttribute(String name, String value)
    {
        setManifestAttribute(new Attributes.Name(name), value);
    }

    public void setManifestAttribute(Attributes.Name name, String value)
    {
        env.logVerbose("Set manifest attribute %s=%s\n", name, value);
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

        for (File dir : sourceDir) {
            DirectoryScanner scanner = new DirectoryScanner(dir, includes, excludes);

            try {
                files.put(dir, scanner.scan());
            }
            catch (IOException e) {
                env.handle(e);
            }
        }

        if (!uptodate(jarTimeStamp, files)) {
            buildJar(files);
        }
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public void setExcludes(@NotNull List<String> patterns)
    {
        excludes = patterns;
    }

    public void setIncludes(@NotNull List<String> patterns)
    {
        includes = patterns;
    }

    public void addManifestAttributes(Map<Attributes.Name, String> attributes)
    {
        for (Map.Entry<Attributes.Name, String> atts : attributes.entrySet()) {
            setManifestAttribute(atts.getKey(), atts.getValue());
        }
    }

    private void setServices(@NotNull Map<String, Set<String>> services)
    {
        this.services = services;
    }

    private void addDir(File file)
    {
        sourceDir.add(file);
    }

    private long checkJarFile()
    {
        final boolean exists = jarFile.exists();
        long          result = -1;

        if (exists) {
            if (!jarFile.isFile()) {
                throw new BuildException(jarFile + " is not a file.");
            }

            if (!jarFile.canWrite()) {
                throw new BuildException(jarFile + " is read-only.");
            }

            result = jarFile.lastModified();
        }

        return result;
    }

    private void buildJar(Map<File, List<String>> files)
    {
        try {
            env.logInfo("Building: %s\n", jarFile.getCanonicalPath());

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

    /**
     * Check if the jar file is uptodate.
     * (The timestamp for all files is lower than the jar one)
     * @param jarTimeStamp The timestamp for the jar file
     * @param files The set of files to add
     * @return true if the jar is 'uptodate'
     */
    private boolean uptodate(long jarTimeStamp, Map<File, List<String>> files)
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

    private void writeToJar(JarOutputStream jarOut, final String fileName, final InputStream is,
                            Set<String> addedDirs)
        throws IOException
    {
        writeParentDirs(jarOut, fileName, addedDirs);

        env.logVerbose("Adding entry... %s\n", fileName);

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
            env.logVerbose("Adding dir...   %s\n", dirName);
            JarEntry ze = new JarEntry(dirName);
            ze.setSize(0);
            ze.setMethod(ZipEntry.STORED);
            ze.setCrc(EMPTY_CRC);

            jarOut.putNextEntry(ze);
        }
    }

    //~ Static fields/initializers ...........................................................................

    private static final long EMPTY_CRC = new CRC32().getValue();
}
