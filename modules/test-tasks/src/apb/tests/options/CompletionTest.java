

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


package apb.tests.options;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import apb.Apb;
import apb.OptionCompletion;

import apb.index.DefinitionsIndex;

import apb.tests.build.ApbTestCase;

import apb.utils.ClassUtils;
import apb.utils.OptionParser;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.sort;

import static apb.utils.FileUtils.makePath;
//
// User: emilio
// Date: Sep 24, 2009
// Time: 3:24:10 PM

//
public class CompletionTest
    extends ApbTestCase
{
    //~ Methods ..............................................................................................

    public void testModules()
    {
        final File indexFile = new File(tmpFile("definitions.dir"));
        env.putProperty("definitions.dir", indexFile.getPath());
        env.putProperty("project.path", makePath(projectPath));

        List<OptionParser.Option> options = emptyList();
        OptionCompletion          oc =
            new OptionCompletion(options, new DefinitionsIndex(env, Apb.loadProjectPath()));

        String result = oc.execute(0, asList("Mat"));
        assertEquals("Math tests.Math", result);

        result = oc.execute(asList("", "1", "", "Mat"));
        assertEquals("Math tests.Math", result);

        result = oc.execute(0, asList("Samples.c"));
        assertEquals("Samples.clean Samples.compile Samples.compile-tests", result);

        result = oc.execute(0, asList(""));
        assertEquals("Chat HelloWorld Info Math Samples submod.Mod tests.Math", result);

        result = oc.execute(0, asList("tests.Ma"));
        final List<String> list = new ArrayList<String>(IndexTest.MATH_EXPECTED_COMMANDS);
        list.add("run");
        list.add("run-minimal");
        sort(list);
        assertEquals(makeCommands("tests.Math.", list), result);

        String prev = System.setProperty("user.dir", dataFile("projects/math"));
        result = oc.execute(0, asList(""));
        System.setProperty("user.dir", prev);

        assertEquals(makeCommands("Math.", IndexTest.MATH_EXPECTED_COMMANDS), result);
    }

    public void testOptions()
        throws Exception
    {
        OptionParser op = (OptionParser) ClassUtils.newInstance("apb.ApbOptions", emptyList());

        OptionCompletion oc = new OptionCompletion(op.getOptions(), null);

        String result = oc.execute(0, asList("--de"));
        assertEquals("--debug --define", result);

        result = oc.execute(0, asList("-"));
        assertEquals("-c -D -d -f -h -n -q -s -t -v", result);

        result = oc.execute(1, asList("--debug"));
        assertEquals("all dependencies properties task_info track", result);

        result = oc.execute(1, asList("-d"));
        assertEquals("all dependencies properties task_info track", result);
    }

    static String makeCommands(final String module, final List<String> commands)
    {
        StringBuilder result = new StringBuilder();

        for (String cmd : commands) {
            result.append(module).append(cmd).append(' ');
        }

        return result.substring(0, result.length() - 1);
    }
}
