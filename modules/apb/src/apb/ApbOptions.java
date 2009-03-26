

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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import apb.utils.OptionParser;
import apb.utils.StandaloneEnv;

import static apb.Messages.*;
import apb.metadata.Module;
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
    private Option<Boolean> quiet;
    private Option<Boolean> showStackTrace;
    private Option<Boolean> verbose;

    //~ Constructors .........................................................................................

    public ApbOptions(String[] ops)
    {
        super(ops, "apb", "0.9.1");
        showStackTrace = addBooleanOption('t', "show-stack-trace", SHOW_STACK_TRACE);
        quiet = addBooleanOption('q', "quiet", QUIET_OUTPUT);
        verbose = addBooleanOption('v', "verbose", VERBOSE);
        noFailOnError = addBooleanOption('c', "continue", CONTINUE_AFTER_ERROR);
        forceBuild = addBooleanOption('f', "force-build", FORCE_BUILD);
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
        final List<String> result = super.parse();

        if (result.isEmpty()) {
            printHelp();
        }
        return result;
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

        environment.setFailOnError(!noFailOnError.getValue());
        environment.setForceBuild(forceBuild.getValue());
    }

    private static String printCommands()
    {
        StringBuilder cmds = new StringBuilder();

        // Create a Mock Module Helper
        ModuleHelper mock = new ModuleHelper(new Module(), new StandaloneEnv());
        Set<String> nameSpaces = new TreeSet<String>();
        for (Command cmd : mock.listCommands()) {
            if (cmd.hasNameSpace()) {
                nameSpaces.add(cmd.getNameSpace());
            }
            else if (!cmd.isDefault()) {
                cmds.append(cmds.length() == 0 ? "[ " : " | ");
                cmds.append(cmd.getName());
            }
        }
        for (String nameSpace : nameSpaces) {
            cmds.append(" | " ).append(nameSpace).append(":*");
        }

        cmds.append(" ]");
        return cmds.toString();
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

    private void doCompletion()
    {
        if (arguments.size() >= 3 && "--complete".equals(arguments.get(0))) {
            OptionCompletion op = new OptionCompletion(options);

            op.execute(arguments);
        }
    }
}
