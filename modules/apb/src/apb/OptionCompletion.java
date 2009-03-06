

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import apb.utils.FileUtils;
import apb.utils.OptionParser;
//
// User: emilio
// Date: Mar 4, 2009
// Time: 2:43:30 PM

//
public class OptionCompletion
{
    //~ Instance fields ......................................................................................

    private Environment env;
    private String[] excludeDirs;

    private List<OptionParser.Option> options;

    //~ Constructors .........................................................................................

    public OptionCompletion(Environment env, List<OptionParser.Option> options)
    {
        this.env = env;
        this.options = options;
        excludeDirs = env.getProperty("project.path.exclude", DEFAULT_EXCLUDES).split(",");
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
        int dot = last.lastIndexOf(".");

        if (dot == -1) {
            print(listModules(last));
        }
        else {
            try {
                print(listCommands(last.substring(0, dot), last.substring(dot + 1)));
            }
            catch (DefinitionException e) {
                // The dot was not a marker for a command but a package Name !
                print(listModules(last));
            }
        }
    }

    private Collection<String> listModules(String last)
    {
        SortedSet<String> result = new TreeSet<String>();

        for (File f : FileUtils.listJavaSources(env.getProjectPath())) {
            if (!isDirExcluded(f.getParent())) {
                final String fileName = f.getPath().replace(File.separatorChar, '.');

                if (fileName.startsWith(last) || f.getName().startsWith(last)) {
                    result.add(fileName.substring(0,
                                                  fileName.length() - apb.utils.FileUtils.JAVA_EXT.length()));
                }
            }
        }

        if (result.size() == 1) {
            try {
                return listCommands(result.first(), "");
            }
            catch (DefinitionException ignore) {
                // Ignore the Exception Just return the single element
            }
        }

        return result;
    }

    private boolean isDirExcluded(String dir)
    {
        if (dir != null) {
            for (String excludeDir : excludeDirs) {
                if (excludeDir.equals(dir)) {
                    return true;
                }
            }
        }

        return false;
    }

    private Collection<String> listCommands(String module, String command)
        throws DefinitionException
    {
        Set<String>          result = new TreeSet<String>();
        ProjectElementHelper helper = env.constructProjectElement(module);

        for (Command cmd : Command.listCommands(helper.getElement().getClass())) {
            String cmdName = cmd.getName();
            if (cmdName.startsWith(command)) {
                result.add(module + "." + cmdName);
            }
        }

        return result;
    }

    //~ Static fields/initializers ...........................................................................

    private static final String DEFAULT_EXCLUDES = "default,libraries";
}
