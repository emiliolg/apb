

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

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import apb.index.DefinitionsIndex;
import apb.index.ModuleInfo;

import apb.utils.StandaloneEnv;

import static apb.Messages.BUILD_COMPLETED;
import static apb.Messages.BUILD_FAILED;

public class Main
{
    //~ Methods ..............................................................................................

    public static void main(String[] args)
        throws Throwable
    {
        ApbOptions   options = new ApbOptions(args);
        List<String> arguments = options.parse();

        Environment env = new StandaloneEnv(options.definedProperties());
        options.initEnv(env);
        Apb.setEnv(env);

        final Set<File> path = Apb.loadProjectPath();

        if (arguments.isEmpty()) {
            arguments = searchDefault(env, options, path);
        }

        Main.execute(env, arguments, path, options.showStackTrace());
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
            result = Collections.emptyList();
        }

        return result;
    }

    private static void execute(Environment env, List<String> arguments, final Set<File> projectPath,
                                boolean showStackTrace)
        throws Throwable
    {
        Throwable e = null;
        long      clock = System.currentTimeMillis();

        for (String argument : arguments) {
            final String[] argParts = splitParts(argument);

            try {
                ProjectBuilder b = new ProjectBuilder(env, projectPath);
                b.build(argParts[0], argParts[1]);
            }
            catch (DefinitionException d) {
                env.logSevere("%s\nCause: %s\n", d.getMessage(), d.getCause().getMessage());
                e = d.getCause();
            }
            catch (BuildException b) {
                e = b.getCause() == null ? b : b.getCause();
                env.logSevere("%s\n", b.getMessage());
            }
        }

        if (e == null) {
            env.logInfo(BUILD_COMPLETED(System.currentTimeMillis() - clock));
        }
        else {
            if (showStackTrace) {
                e.printStackTrace(System.err);
            }
            env.logInfo(BUILD_FAILED);
        }
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
            commandName = Command.DEFAULT_COMMAND;
        }

        return new String[] { argument.substring(0, dot), commandName };
    }
}
