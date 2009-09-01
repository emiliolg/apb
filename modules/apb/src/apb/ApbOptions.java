

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

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import apb.metadata.Module;

import apb.utils.DebugOption;
import apb.utils.OptionParser;
import apb.utils.StandaloneEnv;

import static java.lang.System.getProperty;

import static apb.Messages.*;
//
// User: emilio
// Date: Sep 3, 2008
// Time: 6:53:36 PM

class ApbOptions
    extends OptionParser
{
    //~ Instance fields ......................................................................................

    private final Option<String> debug;

    private final Option<String> defineProperty;

    private final Option<Boolean> forceBuild;
    private final Option<Boolean> noFailOnError;
    private final Option<Boolean> nonRecursive;
    private final Option<Boolean> quiet;
    private final Option<Boolean> showStackTrace;
    private final Option<Boolean> track;
    private final Option<Boolean> verbose;

    //~ Constructors .........................................................................................

    public ApbOptions(String[] ops)
    {
        super(ops, "apb");
        showStackTrace = addBooleanOption('s', "show-stack-trace", SHOW_STACK_TRACE);
        quiet = addBooleanOption('q', "quiet", QUIET_OUTPUT);
        verbose = addBooleanOption('v', "verbose", VERBOSE);
        noFailOnError = addBooleanOption('c', "continue", CONTINUE_AFTER_ERROR);
        forceBuild = addBooleanOption('f', "force-build", FORCE_BUILD);
        nonRecursive = addBooleanOption('n', "non-recursive", NON_RECURSIVE);
        defineProperty = addOption('D', "define", DEFINE_PROPERTY, "<name>=<value>");
        defineProperty.setCanRepeat(true);
        track = addBooleanOption('t', "track-execution", TRACK_EXECUTION);
        debug = addOption('d', "debug", DEBUG, "<info type>");
        debug.addValidValue(DebugOption.ALL);

        for (DebugOption op : DebugOption.values()) {
            debug.addValidValue(op.toString());
        }

        debug.setCanRepeat(true);
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
        if (quiet.getValue()) {
            environment.setQuiet();
        }

        if (nonRecursive.getValue()) {
            environment.setNonRecursive();
        }

        environment.setDebugOptions(debugOptions());

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

    public boolean showStackTrace()
    {
        return showStackTrace.getValue();
    }

    protected void printVersion()
    {
        final Package pkg = ApbOptions.class.getPackage();
        final String  version = pkg.getImplementationVersion();
        System.err.printf("%-4s: %s\n", pkg.getName(), version == null ? "" : version);
        System.err.printf("java: %s\n", getProperty("java.version"));
        System.err.printf("OS  : %s %s on %s\n", getProperty("os.name"), getProperty("os.version"),
                          getProperty("os.arch"));

        System.exit(0);
    }

    EnumSet<DebugOption> debugOptions()
    {
        EnumSet<DebugOption> result = EnumSet.noneOf(DebugOption.class);

        for (String d : debug.getValues()) {
            if (d.equals(DebugOption.ALL)) {
                return EnumSet.allOf(DebugOption.class);
            }

            DebugOption o = DebugOption.find(d);

            if (o != null) {
                result.add(o);
            }
        }

        if (track.getValue()) {
            result.add(DebugOption.TRACK);
        }

        if (verbose.getValue()) {
            result.add(DebugOption.TASK_INFO);
        }

        return result;
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
