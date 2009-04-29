

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import apb.index.DefinitionsIndex;
import apb.index.ModuleInfo;

import apb.utils.OptionParser;
import apb.utils.StandaloneEnv;
//
// User: emilio
// Date: Mar 4, 2009
// Time: 2:43:30 PM

//
public class OptionCompletion
{
    //~ Instance fields ......................................................................................

    private Environment env;

    private List<OptionParser.Option> options;

    //~ Constructors .........................................................................................

    public OptionCompletion(List<OptionParser.Option> options)
    {
        env = new StandaloneEnv();
        this.options = options;
    }

    //~ Methods ..............................................................................................

    public void execute(List<String> arguments)
    {
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
        else if (last.isEmpty()) {
            final ModuleInfo info = env.getDefinitionsIndex().searchCurrentDirectory();

            if (info == null) {
                printCompletions("");
            }
            else {
                printCommands(info, "");
            }
        }
        else {
            printCompletions(last);
        }

        System.exit(0);
    }

    private static void print(Collection<String> opts)
    {
        for (String opt : opts) {
            System.out.print(opt + " ");
        }
    }

    private List<String> findOptions(String opt)
    {
        List<String> result = new ArrayList<String>();

        for (OptionParser.Option option : options) {
            if (option.getName().startsWith(opt)) {
                result.add("--" + option.getName());
            }
        }

        return result;
    }

    private List<String> findShortOptions(char chr)
    {
        List<String> result = new ArrayList<String>();

        for (OptionParser.Option option : options) {
            if (option.getLetter() == chr) {
                result.add("-" + option.getLetter());
            }
        }

        return result;
    }

    private void printCompletions(String last)
    {
        int    dot = last.lastIndexOf(".");
        String moduleName = dot == -1 ? last : last.substring(0, dot);

        final DefinitionsIndex index = env.getDefinitionsIndex();

        List<ModuleInfo> modules = index.findAllByName(moduleName);

        // If the String before the dot does not match a single module
        // Try to see if the whole string results in a better match
        if (modules.size() != 1 && dot != -1) {
            List<ModuleInfo> m2 = index.findAllByName(last);

            if (!m2.isEmpty() && modules.isEmpty() || m2.size() < modules.size()) {
                modules = m2;
                dot = -1;
            }
        }

        if (modules.size() == 1) {
            printCommands(modules.get(0), dot == -1 ? "" : last.substring(dot + 1));
        }
        else {
            for (ModuleInfo module : modules) {
                System.out.print(module.getName() + " ");
            }
        }
    }

    private void printCommands(ModuleInfo module, String cmdStart)
    {
        for (String cmdName : module.getCommands()) {
            if (cmdName.startsWith(cmdStart)) {
                System.out.print(module.getName() + "." + cmdName + " ");
            }
        }
    }
}
