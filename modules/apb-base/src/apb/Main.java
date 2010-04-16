

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


package apb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import apb.utils.FileUtils;

import static java.util.Arrays.asList;

import static apb.Messages.BUILD_COMPLETED;
import static apb.Messages.BUILD_FAILED;

import static apb.utils.FileUtils.validateDir;

/**
 * Apb main class
 */
public class Main
{
    //~ Methods ..............................................................................................

    /**
     * Apb entry point when invoked from the command line
     * @param args Arguments to apb
     */
    public static void main(String[] args)
        throws Throwable
    {
        checkEnvironment();
        ApbOptions   options = new ApbOptions(asList(args));
        List<String> arguments = options.parse();

        Environment env = Apb.createBaseEnvironment(options);

        final Set<File> path = Apb.loadProjectPath();

        if (arguments.isEmpty()) {
            arguments = searchDefault(env, options, path);
        }

        boolean success = Main.execute(env, arguments, path, options.showStackTrace());

        if (!success) {
            Apb.exit(1);
        }
    }

    private static void checkEnvironment()
        throws IOException
    {
        File   dir = FileUtils.getApbDir();
        String msg = validateDir(dir);

        if (!msg.isEmpty()) {
            throw new IOException(msg);
        }

        File properties = new File(dir, Constants.APB_PROPERTIES);

        if (!properties.exists()) {
            File apbHome = Apb.getHome();

            if (apbHome != null) {
                BufferedReader s =
                    new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/resources/apb.properties"),
                                                             "UTF8"));
                PrintStream    w = new PrintStream(properties);
                String         line;

                while ((line = s.readLine()) != null) {
                    w.printf(line, apbHome.getPath());
                    w.println();
                }

                w.close();
            }
        }
    }

    /**
     * Try to find a module definition whose 'module-dir" match the current directory or a parent
     * of the current directory one.
     * @param env
     * @param options
     * @param projectPath
     * @result The definiton
     */
    private static List<String> searchDefault(Environment env, ApbOptions options,
                                              final Set<File> projectPath)
    {
        final List<String> result;
        final ModuleInfo   info = new DefinitionsIndex(env, projectPath).searchCurrentDirectory();

        if (info != null) {
            env.logInfo("Executing: %s.%s\n", info.getName(), info.getDefaultCommand());
            result = Collections.singletonList(info.getPath());
        }
        else {
            options.printHelp();
            Apb.exit(0);
            result = Collections.emptyList();
        }

        return result;
    }

    private static boolean execute(Environment env, List<String> arguments, final Set<File> projectPath,
                                   boolean showStackTrace)
        throws Throwable
    {
        Throwable e = null;
        long      clock = System.currentTimeMillis();

        for (String argument : arguments) {
            final String[] argParts = splitParts(argument);

            try {
                ProjectBuilder b = new ProjectBuilder(env, projectPath);
                b.build(env, argParts[0], argParts[1]);
            }
            catch (DefinitionException d) {
                e = d.getCause();
                String causeMsg = e.getMessage();

                if (e instanceof FileNotFoundException) {
                    causeMsg = "File not found: " + causeMsg;
                }
                else if (causeMsg == null) {
                    causeMsg = e.toString();
                }

                env.logSevere("%s\nCause: %s\n", d.getMessage(), causeMsg);
            }
            catch (BuildException b) {
                e = b.getCause() == null ? b : b.getCause();
                env.logSevere("%s\n", b.getMessage());
            }
        }

        if (e == null) {
            env.logInfo(BUILD_COMPLETED(String.valueOf(System.currentTimeMillis() - clock)));
        }
        else {
            if (showStackTrace) {
                e.printStackTrace(System.err);
            }

            env.logInfo(BUILD_FAILED);
        }

        return e == null;
    }

    private static String[] splitParts(String argument)
    {
        int    dot = argument.lastIndexOf('.');
        String commandName;

        if (dot != -1) {
            commandName = argument.substring(dot + 1);
        }
        else {
            dot = argument.length();
            commandName = Constants.DEFAULT_COMMAND;
        }

        return new String[] { argument.substring(0, dot), commandName };
    }
}
