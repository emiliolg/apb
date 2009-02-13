
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

package apb.tasks;

import apb.BuildException;
import apb.Environment;
import apb.ModuleHelper;
import apb.metadata.Dependency;
import apb.metadata.Module;
import apb.metadata.PackageInfo;
import apb.metadata.PackageType;
import apb.utils.DirectoryScanner;
import apb.utils.FileUtils;
import org.jetbrains.annotations.NotNull;

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
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
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

    private int               level = Deflater.DEFAULT_COMPRESSION;
    @NotNull private Manifest manifest;
    private List<File>        sourceDir;

    //~ Constructors .........................................................................................

    public JarTask(@NotNull Environment env, @NotNull File jarFile)
    {
        super(env);
        this.jarFile = jarFile;
        sourceDir = new ArrayList<File>();
        excludes = Collections.emptyList();
        includes = Arrays.asList("**/**");
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

            if (!packageInfo.includeDependencies().isEmpty()) {
                for (Dependency d : packageInfo.includeDependencies()) {
                    if (d instanceof Module) {
                        ModuleHelper m = (ModuleHelper) env.getHelper((Module) d);
                        jarTask.addDir(m.getOutput());
                    }
                }
            }


            jarTask.execute();

            if (packageInfo.generateSourcesJar) {
                jarTask = new JarTask(helper.getEnv(), helper.getSourcePackageFile());
                jarTask.addDir(helper.getSource());
                jarTask.setExcludes(DirectoryScanner.DEFAULT_EXCLUDES);
                jarTask.execute();
            }
        }
    }

    public void setManifestAttribute(String name, String value)
    {
        manifest.getMainAttributes().putValue(name, value);
    }

    public void setManifestAttribute(Attributes.Name name, String value)
    {
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

                for (File dir : files.keySet()) {
                    for (String fileName : files.get(dir)) {
                        writeToJar(jarOutputStream, dir.getAbsolutePath(), new File(dir, fileName),
                                   addedDirs);
                    }
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
        final JarOutputStream  jarOutputStream = new JarOutputStream(os, manifest);
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

    private void writeToJar(JarOutputStream jarOut, String baseDir, File file, Set<String> addedDirs)
        throws IOException
    {
        String fileName = file.getAbsolutePath();

        if (!fileName.startsWith(baseDir)) {
            env.handle("Wrong basedir");
            return;
        }

        fileName = fileName.substring(baseDir.length() + 1).replace(File.separatorChar, '/');

        if (fileName.isEmpty() || file.length() == 0 || file.isDirectory()) {
            return;
        }

        writeParentDirs(jarOut, fileName, addedDirs);

        env.logVerbose("Adding entry... %s\n", fileName);

        JarEntry entry = new JarEntry(fileName);
        jarOut.putNextEntry(entry);

        byte[]          arr = new byte[1024];
        FileInputStream fileIn = new FileInputStream(file);
        int             n;

        while ((n = fileIn.read(arr)) > 0) {
            jarOut.write(arr, 0, n);
        }

        fileIn.close();
        jarOut.flush();
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
