

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

import java.lang.reflect.Method;

import apb.metadata.BuildTarget;
import apb.metadata.ProjectElement;
//
// User: emilio
// Date: Mar 4, 2009
// Time: 3:26:39 PM

public class Help
    extends Command
{
    //~ Constructors .........................................................................................

    Help()
    {
        super("help", getHelpMethod(), "List the available commands with a brief description");
    }

    //~ Methods ..............................................................................................

    @BuildTarget @SuppressWarnings("UnusedDeclaration")
    public static void help(ProjectElement element, Environment env)
    {
        System.err.println("Commands for '" + element.getName() + "' : ");

        for (Command cmd : listCommands(element.getClass())) {
            if (!cmd.isDefault()) {
                System.err.printf("    %-20s: %s\n", cmd, cmd.getDescription());
            }
        }

        System.exit(0);
    }

    private static Method getHelpMethod()
    {
        try {
            return apb.Help.class.getMethod("help", ProjectElement.class, Environment.class);
        }
        catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
