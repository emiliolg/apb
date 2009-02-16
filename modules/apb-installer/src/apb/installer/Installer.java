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


package apb.installer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

//
public class Installer {
    //~ Instance fields ......................................................................................

    private String[] args;
    private File binDir;
    private boolean extract;
    private File jarFile;
    private File libDir;
    private boolean silent;
    private File antlibDir;
    private boolean createDirs;
    private static final String APB_MEMORY = "256";

    //~ Constructors .........................................................................................

    public Installer(File jarFile, String[] args) {
        this.jarFile = jarFile;
        this.args = args;
    }

    //~ Methods ..............................................................................................

    public void run()
            throws IOException {
        JarFile jar = new JarFile(jarFile);

        if (extract) {
            extract(jar, null, new File("./"));
        } else {
            if (libDir == null) {
                libDir = binDir;
            }
            generateScript();
            extract(jar, "bin/", binDir);
            extract(jar, "lib/", libDir);
            if (antlibDir != null) {
                extract(jar, "antlib/", antlibDir);
            }
        }
    }

    private void generateScript() throws FileNotFoundException {

        final File scriptFile = new File(binDir, "apb");

        println("Generating apb shell script: " + scriptFile);
        PrintWriter writer = new PrintWriter(scriptFile);
        File l = Utils.makeRelative(binDir, libDir);
        String dir;
        if (l == null) {
            dir = libDir.getAbsolutePath();
        } else if (isWindows()) {
            dir = "$(cygpath --windows $(dirname $(type -p $0))/)" + l;
        }else {
            dir = "$(dirname $(type -p $0))/" + l;
        }
        dir += File.separator;
        String[] props = {APB_MEMORY, dir};
        for (int i = 0; i < script.length; i++) {
            writer.println(MessageFormat.format(script[i], props));

        }
        writer.close();
        if (scriptFile.setExecutable(true))
            println("Making script executable.");
    }

    private static final String[] script = {
            "# Startup script for apb", //
            "", //
            "java -jar -Xmx{0}m -jar {1}apb.jar $*"
    };

    public boolean parseArgs()
            throws ArgException {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (!arg.startsWith("-")) {
                binDir = validDirectory(arg);
            } else if ("--lib".equals(arg)) {
                libDir = validDirectory(++i);
            } else if ("--antlib".equals(arg)) {
                antlibDir = validDirectory(++i);
            } else if ("-x".equals(arg)) {
                extract = true;
            } else if ("-c".equals(arg)) {
                createDirs = true;
            } else if ("-s".equals(arg)) {
                silent = true;
            } else {
                throw new ArgException("Unknown option: " + arg);
            }
        }

        return extract || binDir != null;
    }

    void usage() {
        final String[] strings = {jarFile.getName()};

        for (int i = 0; i < usageMsg.length; i++) {
            System.err.println(MessageFormat.format(usageMsg[i], strings));
        }

        System.exit(0);
    }

    private void extract(JarFile jar, String prefix, File targetDir)
            throws IOException {
        Enumeration e = jar.entries();

        while (e.hasMoreElements()) {
            JarEntry entry = (JarEntry) e.nextElement();

            String name = entry.getName();

            if (name.startsWith("apb-")) {
                if (prefix == null) {
                    extract(jar, entry, targetDir, name);
                } else {
                    name = name.substring(name.indexOf('/') + 1);

                    if (name.startsWith(prefix)) {
                        name = name.substring(prefix.length());
                        if (name.length() > 0) {
                            extract(jar, entry, targetDir, name);
                        }
                    }
                }
            }
        }
    }

    private void extract(JarFile jar, JarEntry entry, File targetDir, String fileName)
            throws IOException {
        File file = new File(targetDir, fileName);
        println("Extracting : " + entry.getName() + " into " + file.getPath());

        if (entry.isDirectory()) {
            file.mkdirs();
        } else {
            copyToFile(file, jar.getInputStream(entry));
        }
    }

    private void println(String msg) {
        if (!silent)
            System.out.println(msg);
    }

    private void copyToFile(File file, InputStream is)
            throws IOException {
        OutputStream os = new FileOutputStream(file);
        int len;
        byte[] buffer = new byte[8192];

        while ((len = is.read(buffer)) >= 0) {
            os.write(buffer, 0, len);
        }

        os.close();
    }

    private File validDirectory(int i)
            throws ArgException {
        if (i >= args.length) {
            throw new ArgException("Option requires an argument");
        }

        return validDirectory(args[i]);
    }

    private File validDirectory(String dirName)
            throws ArgException {
        File dir = new File(dirName);

        if (!dir.exists()) {
            if (!createDirs) {
                throw new ArgException("Not existing directory: '" + dir + "'");
            }
            if (!dir.mkdirs())
                throw new ArgException("Can Not create directory: '" + dir + "'");
            println("Creating directory: " + dir);
        }

        if (!dir.isDirectory()) {
            throw new ArgException("Not a directory: '" + dir + "'");
        }

        if (!dir.canWrite()) {
            throw new ArgException("Directory: '" + dir + "' does not have write permissions.");
        }

        return dir;
    }

    //~ Static fields/initializers ...........................................................................

    public static final String[] usageMsg = {
            "Usage:",  //
            "     java -jar {0} -x [-s]", "           to extract the whole distribution",
            "  or java -jar {0}    [-s] [-c] [--ant antlib_dir] [--lib lib_dir] bin_dir",
            "           to install the relevant files under the specified directories",  //
            "",  //
            "Where:",  //
            "   bin_dir:  is the directory where the apb shell binary will be copied", "",  //
            "Options:",  //
            "   --lib lib_dir    Specify the directory where the apb needed jars will be copied.",
            "                    If the option is ommited the jars will be copied to the bin directory.", "",  //
            "   --ant antlib_dir Specify the directory where the jar with apb ant tasks will be copied.",
            "                    If the option is ommited the apb ant tasks will not be installed.", "",  //
            "   -s               Silent mode.",
            "   -c               Create directories as needed",
            "Example:",  //
            "     java -jar {0} --lib /usr/local/lib /usr/local/bin",
    };

    public boolean isWindows() {
        String name = System.getProperty("os.name");

        return  name.contains("Windows");
    }
}
