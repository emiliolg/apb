
// ...........................................................................................................
// Copyright (c) 1993, 2009, Oracle and/or its affiliates. All rights reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Oracle Corp.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// Last changed on 2009-04-28 13:54:39 (-0300), by: emilio. $Revision$
// ...........................................................................................................

package apb;

import java.util.Collections;
import java.util.List;

import apb.index.ModuleInfo;

import apb.utils.StandaloneEnv;

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

        Main.execute(env, arguments);
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

    private static void execute(Environment env, List<String> arguments)
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
                env.logSevere("%s\n", e.getMessage());

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
