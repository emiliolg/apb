

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

import java.util.List;

import apb.utils.StandaloneEnv;

public class Main
{
    //~ Methods ..............................................................................................

    public static void main(String[] args)
        throws Throwable
    {
        Environment  env = new StandaloneEnv(Main.class.getPackage().getName());
        ApbOptions   options = new ApbOptions(args, env);
        List<String> arguments = options.parse();
        Main.execute(env, arguments);
    }

    public static boolean execute(Environment env, String element, String command)
        throws Throwable
    {

        try {
            ProjectElementHelper projectElement = env.constructProjectElement(element);
            projectElement.setTopLevel(true);
            projectElement.build(command);
            return true;
        }
        catch (DefinitionException e) {
            env.logSevere("%s\n", e.getMessage());
        }
        catch (BuildException b) {
            Throwable e = b.getCause() == null ? b : b.getCause();
            env.logSevere("%s\n", b.getMessage());

            if (env.showStackTrace()) {
                throw e;
            }
        }

        return false;
    }

    private static void execute(Environment env, List<String> arguments)
        throws Throwable
    {
        env.resetClock();

        boolean ok = true;

        for (String argument : arguments) {
            final String[] argParts = splitParts(argument);

            if (!execute(env, argParts[0], argParts[1])) {
                ok = false;
            }
        }

        env.completedMessage(ok);
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
