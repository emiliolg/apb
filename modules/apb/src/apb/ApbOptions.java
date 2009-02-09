
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

package apb;

import java.util.Collection;
import java.util.List;

import apb.metadata.ProjectElement;

import apb.utils.OptionParser;

import static apb.Messages.*;
//
// User: emilio
// Date: Sep 3, 2008
// Time: 6:53:36 PM

class ApbOptions
    extends OptionParser
{
    //~ Instance fields ......................................................................................

    private Environment     environment;
    private Option<Boolean> forceBuild;
    private Option<Boolean> noFailOnError;
    private Option<Boolean> quiet;
    private Option<Boolean> showStackTrace;
    private Option<Boolean> verbose;

    //~ Constructors .........................................................................................

    public ApbOptions(String[] ops, Environment env)
    {
        super(ops, "apb", "0.1");
        showStackTrace = addOption('t', "show-stack-trace", SHOW_STACK_TRACE, false);
        quiet = addOption('q', "quiet", QUIET_OUTPUT, false);
        verbose = addOption('v', "verbose", VERBOSE, false);
        noFailOnError = addOption('c', "continue", CONTINUE_AFTER_ERROR, false);
        forceBuild = addOption('f', "force-build", FORCE_BUILD, false);
        environment = env;
    }

    //~ Methods ..............................................................................................

    public String getArgShortDescription()
    {
        return "Mod.command ...";
    }

    public String[] getArgFullDescription()
    {
        return new String[] { MODULE_OR_PROJECT, COMMANDS(Command.printCommands(ProjectElement.class)) };
    }

    public List<String> parse()
    {
        doCompletion();
        final List<String> result = super.parse();

        if (result.isEmpty()) {
            printHelp();
        }

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
        return result;
    }

    private void doCompletion()
    {
        if (arguments.size() >= 3 && "--complete".equals(arguments.get(0))) {
            int    argc = Integer.parseInt(arguments.get(1)) + 2;
            String last = argc < arguments.size() ? arguments.get(argc) : "";

            if ("-".equals(last)) {
                last = "--";
            }

            if (last.startsWith("--")) {
                print(findOptions(last.substring(2)));
            }
            else if (last.startsWith("-")) {
                print(findShortOptions(last.charAt(1)));
            }
            else {
                int dot = last.indexOf(".");

                if (dot == -1) {
                    print(environment.listModules(last));
                }
                else {
                    print(environment.listCommands(last.substring(0, dot), last.substring(dot + 1)));
                }
            }

            System.exit(0);
        }
    }

    private void print(Collection<String> opts)
    {
        for (String opt : opts) {
            System.out.print(opt + " ");
        }
    }
}
