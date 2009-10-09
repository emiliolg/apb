

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
import java.util.Collections;
import java.util.List;

import apb.utils.OptionParser;

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Mar 4, 2009
// Time: 2:43:30 PM

//
public class OptionCompletion
{
    //~ Instance fields ......................................................................................

    @NotNull private DefinitionsIndex definitionsIndex;

    @NotNull private final List<OptionParser.Option> options;

    //~ Constructors .........................................................................................

    public OptionCompletion(List<OptionParser.Option> options, DefinitionsIndex index)
    {
        this.options = options;
        definitionsIndex = index;
    }

    //~ Methods ..............................................................................................

    public String execute(List<String> arguments)
    {
        int argc = Integer.parseInt(arguments.get(1)) - 1;
        return execute(argc, arguments.subList(3, arguments.size()));
    }

    public String execute(int argc, List<String> arguments)
    {
        String              last = argc < arguments.size() ? arguments.get(argc) : "";
        final StringBuilder result = new StringBuilder();

        if (last.startsWith("--")) {
            printOptions(result, last.substring(2));
        }
        else if (last.startsWith("-")) {
            printShortOptions(result);
        }
        else {
            List<?> values = optionValues(arguments, argc - 1);

            if (!values.isEmpty()) {
                printValues(result, values, last);
            }
            else if (last.isEmpty()) {
                final ModuleInfo info = definitionsIndex.searchCurrentDirectory();

                if (info == null) {
                    printCompletions(result, "");
                }
                else {
                    printCommands(result, info);
                }
            }
            else {
                printCompletions(result, last);
            }
        }

        return result.toString();
    }

    private static void printValues(StringBuilder output, Collection<?> values, String start)
    {
        for (Object v : values) {
            String s = String.valueOf(v);

            if (s.startsWith(start)) {
                addEntry(output, s);
            }
        }
    }

    private static void addEntry(StringBuilder output, String str)
    {
        if (output.length() > 0) {
            output.append(" ");
        }

        output.append(str);
    }

    private void printOptions(StringBuilder result, String opt)
    {
        List<String> opts = new ArrayList<String>();

        for (OptionParser.Option option : options) {
            if (option.getName().startsWith(opt)) {
                opts.add("--" + option.getName());
            }
        }

        printSorted(result, opts);
    }

    private void printShortOptions(StringBuilder result)
    {
        List<String> opts = new ArrayList<String>();

        for (OptionParser.Option option : options) {
            final char letter = option.getLetter();

            if (letter != 0) {
                opts.add("-" + letter);
            }
        }

        printSorted(result, opts);
    }

    private void printSorted(StringBuilder result, List<String> opts)
    {
        Collections.sort(opts, String.CASE_INSENSITIVE_ORDER);

        for (String op : opts) {
            addEntry(result, op);
        }
    }

    private List<?> optionValues(List<String> opts, int index)
    {
        String opt = index >= 0 && index < opts.size() ? opts.get(index) : "";

        if (opt.startsWith("--")) {
            opt = opt.substring(2);

            for (OptionParser.Option option : options) {
                if (option.getName().equals(opt)) {
                    return option.getValidValues();
                }
            }
        }
        else if (opt.startsWith("-") && opt.length() == 2) {
            char o = opt.charAt(1);

            for (OptionParser.Option option : options) {
                if (option.getLetter() == o) {
                    return option.getValidValues();
                }
            }
        }

        return Collections.emptyList();
    }

    private void printCompletions(StringBuilder output, String last)
    {
        int    dot = last.lastIndexOf(".");
        String moduleName = dot == -1 ? last : last.substring(0, dot + 1);

        List<ModuleInfo> modules = definitionsIndex.findAllByName(moduleName);

        if (dot != -1) {
            if (modules.size() == 1) {
                List<String> cmds = listCommands(modules.get(0), last.substring(dot + 1));

                if (!cmds.isEmpty()) {
                    printSorted(output, cmds);
                    return;
                }
            }
            // If the String before the dot does not match
            // Try to see if the whole string results in a better match

            List<ModuleInfo> m2 = definitionsIndex.findAllByName(last);

            if (!m2.isEmpty() && modules.isEmpty() || m2.size() < modules.size()) {
                modules = m2;
            }
        }

        if (modules.size() == 1) {
            printCommands(output, modules.get(0));
        }
        else {
            for (ModuleInfo module : modules) {
                addEntry(output, module.getName());
            }
        }
    }

    private List<String> listCommands(ModuleInfo module, String cmdStart)
    {
        List<String> result = new ArrayList<String>();

        for (String cmdName : module.getCommands()) {
            if (cmdName.startsWith(cmdStart)) {
                result.add(module.getName() + "." + cmdName);
            }
        }

        return result;
    }

    private void printCommands(StringBuilder output, ModuleInfo module)
    {
        printSorted(output, listCommands(module, ""));
    }
}
