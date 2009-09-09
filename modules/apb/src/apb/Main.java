
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

import java.util.Collections;
import java.util.List;

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
        Environment  env = new StandaloneEnv(Main.class.getPackage().getName(), options.definedProperties());
        options.initEnv(env);

        if (arguments.isEmpty()) {
            arguments = searchDefault(env, options);
        }

        final boolean ok = execute(env, arguments);

        if (!ok) {
            System.exit(1);
        }
    }

    public static boolean execute(Environment env, String element, String command)
        throws DefinitionException
    {
        ProjectElementHelper projectElement = env.constructProjectElement(element);
        projectElement.setTopLevel(true);
        projectElement.build(command);
        return true;
    }

    /**
     * Try to find a module definition whose 'module-dir" match the current directory or a parent
     * of the current directory one.
     * @param env
     * @param options
     * @result The definiton
     */
    private static List<String> searchDefault(Environment env, ApbOptions options)
    {
        final List<String> result;
        final ModuleInfo   info = env.getDefinitionsIndex().searchCurrentDirectory();

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

    private static boolean execute(Environment env, List<String> arguments)
        throws Throwable
    {
        env.resetClock();

        boolean ok = true;

        for (String argument : arguments) {
            final String[] argParts = splitParts(argument);

            try {
                if (!execute(env, argParts[0], argParts[1])) {
                    ok = false;
                }
            }
            catch (DefinitionException e) {
                env.logSevere("%s\nCause: %s\n", e.getMessage(), e.getCause().getMessage());

                if (env.showStackTrace()) {
                    throw e.getCause();
                }

                ok = false;
            }
            catch (BuildException b) {
                ok = false;
                Throwable e = b.getCause() == null ? b : b.getCause();
                env.logSevere("%s\n", b.getMessage());

                if (env.showStackTrace()) {
                    throw e;
                }
            }
        }

        if (ok) {
            env.logInfo(BUILD_COMPLETED(System.currentTimeMillis() - env.getClock()));
        }
        else {
            env.logInfo(BUILD_FAILED);
        }

        return ok;
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
