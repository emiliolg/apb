

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
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import apb.Command;
import apb.DefinitionException;
import apb.Environment;
import apb.ProjectElementHelper;

import apb.utils.FileUtils;
//
// User: emilio
// Date: Mar 4, 2009
// Time: 2:43:30 PM

//
public class OptionCompletion
{
    //~ Methods ..............................................................................................

    public static void printCompletions(Environment env, String last)
    {
        int dot = last.lastIndexOf(".");

        if (dot == -1) {
            print(listModules(env, last));
        }
        else {
            try {
                print(listCommands(env, last.substring(0, dot), last.substring(dot + 1)));
            }
            catch (DefinitionException e) {
                // The dot was not a marker for a command but a package Name !
                print(listModules(env, last));
            }
        }
    }

    public static void print(Collection<String> opts)
    {
        for (String opt : opts) {
            System.out.print(opt + " ");
        }
    }

    public static Collection<String> listModules(Environment env, String last)
    {
        SortedSet<String> result = new TreeSet<String>();

        for (File f : FileUtils.listJavaSources(env.getProjectPath())) {
            final String fileName = f.getPath().replace(File.separatorChar, '.');

            if (fileName.startsWith(last) || f.getName().startsWith(last)) {
                result.add(fileName.substring(0, fileName.length() - apb.utils.FileUtils.JAVA_EXT.length()));
            }
        }

        if (result.size() == 1) {
            try {
                return listCommands(env, result.first(), "");
            }
            catch (DefinitionException ignore) {
                // Ignore the Exception Just return the single element
            }
        }

        return result;
    }

    public static Collection<String> listCommands(Environment env, String module, String command)
        throws DefinitionException
    {
        Set<String>          result = new TreeSet<String>();
        ProjectElementHelper helper = env.constructProjectElement(module);

        for (String cmd : Command.listCommands(helper.getElement().getClass())) {
            if (cmd.startsWith(command)) {
                result.add(module + "." + cmd);
            }
        }

        return result;
    }
}
