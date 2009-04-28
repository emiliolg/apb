
// ...........................................................................................................
// Copyright (c) 1993, 2009, Oracle and/or its affiliates. All rights reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Oracle Corp.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// Last changed on 2009-04-27 15:06:13 (-0300), by: emilio. $Revision$
// ...........................................................................................................

package apb;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static apb.Messages.*;
import apb.metadata.Module;
import apb.utils.OptionParser;
import apb.utils.StandaloneEnv;
//
// User: emilio
// Date: Sep 3, 2008
// Time: 6:53:36 PM

class ApbOptions
    extends OptionParser
{
    //~ Instance fields ......................................................................................

    private Option<String> defineProperty;

    private Option<Boolean> forceBuild;
    private Option<Boolean> noFailOnError;
    private Option<Boolean> nonRecursive;
    private Option<Boolean> quiet;
    private Option<Boolean> showStackTrace;
    private Option<Boolean> verbose;

    //~ Constructors .........................................................................................

    public ApbOptions(String[] ops)
    {
        super(ops, "apb", version());
        showStackTrace = addBooleanOption('t', "show-stack-trace", SHOW_STACK_TRACE);
        quiet = addBooleanOption('q', "quiet", QUIET_OUTPUT);
        verbose = addBooleanOption('v', "verbose", VERBOSE);
        noFailOnError = addBooleanOption('c', "continue", CONTINUE_AFTER_ERROR);
        forceBuild = addBooleanOption('f', "force-build", FORCE_BUILD);
        nonRecursive = addBooleanOption('n', "non-recursive", NON_RECURSIVE);
        defineProperty = addOption('D', "define", DEFINE_PROPERTY, "<name>=<value>");
        defineProperty.setCanRepeat(true);
    }

    //~ Methods ..............................................................................................

    public String getArgShortDescription()
    {
        return "Mod.command ...";
    }

    public String[] getArgFullDescription()
    {
        return new String[] { MODULE_OR_PROJECT, COMMANDS(printCommands()) };
    }

    public List<String> parse()
    {
        doCompletion();
        return super.parse();
    }

    public void initEnv(Environment environment)
    {
        if (verbose.getValue()) {
            environment.setVerbose();
        }
        else if (quiet.getValue()) {
            environment.setQuiet();
        }

        if (showStackTrace.getValue()) {
            environment.setShowStackTrace();
        }

        if (nonRecursive.getValue()) {
            environment.setNonRecursive();
        }

        environment.setFailOnError(!noFailOnError.getValue());
        environment.setForceBuild(forceBuild.getValue());
    }

    public Map<String, String> definedProperties()
    {
        Map<String, String> result = new LinkedHashMap<String, String>();

        for (String define : defineProperty.getValues()) {
            int pos = define.indexOf('=');

            if (pos == -1) {
                result.put(define, "true");
            }
            else {
                result.put(define.substring(0, pos).trim(), define.substring(pos + 1).trim());
            }
        }

        return result;
    }

    private static String version()
    {
        final String result = ApbOptions.class.getPackage().getImplementationVersion();
        return result == null ? "" : result;
    }

    private static String printCommands()
    {
        StringBuilder cmds = new StringBuilder();

        // Create a Mock Module Helper
        ModuleHelper mock = new ModuleHelper(new Module(), new StandaloneEnv());
        Set<String>  nameSpaces = new TreeSet<String>();

        for (Command cmd : mock.listCommands().values()) {
            if (cmd.hasNameSpace()) {
                nameSpaces.add(cmd.getNameSpace());
            }
            else {
                cmds.append(cmds.length() == 0 ? "[ " : " | ");
                cmds.append(cmd.getName());
            }
        }

        for (String nameSpace : nameSpaces) {
            cmds.append(" | ").append(nameSpace).append(":*");
        }

        cmds.append(" ]");
        return cmds.toString();
    }

    private void doCompletion()
    {
        if (arguments.size() >= 3 && "--complete".equals(arguments.get(0))) {
            OptionCompletion op = new OptionCompletion(options);

            op.execute(arguments);
        }
    }
}
