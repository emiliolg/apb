

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


package apb.tests.build;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import apb.DefinitionException;

import apb.tests.testutils.FileAssert;

import apb.utils.Console;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class CloneTest
    extends ApbTestCase
{
    //~ Methods ..............................................................................................

    public void testClone()
        throws DefinitionException
    {
        final String newProjDir = tmpFile("projects");

        Map<String, String> inputs = new HashMap<String, String>();
        inputs.put("Project directory", new File(newProjDir, "DEFS").getPath());
        inputs.put("New module name", "submodule.NewMod");
        inputs.put("New group name", "org.submodule");
        inputs.put("New top package name", "org.submodule.newm");

        Console.setConsole(new TestConsole(inputs));
        build("submod.Mod", "module:clone");
        checkOutput(COPYING_1_FILE);
        FileAssert.assertExists(new File(newProjDir, "DEFS/submodule/NewMod.java"));
        FileAssert.assertExists(new File(newProjDir,
                                         "submodule/new-mod/src/org/submodule/newm/TestFile.java"));
    }

    //~ Static fields/initializers ...........................................................................

    private static final String COPYING_1_FILE = "Copying  1 file\\+";

    //~ Inner Classes ........................................................................................

    private static class TestConsole
        extends Console
    {
        private Map<String, String> inputs;

        public TestConsole(Map<String, String> m)
        {
            inputs = m;
        }

        protected String readLine(String prompt, String defaultValue)
        {
            String result = inputs.get(prompt);

            if (result == null) {
                throw new RuntimeException("Not value for : " + prompt);
            }

            return result;
        }
    }
}
