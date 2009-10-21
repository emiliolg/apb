

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
import java.util.List;
import java.util.Set;

import apb.Apb;
import apb.Constants;
import apb.DefinitionsIndex;
import apb.ModuleInfo;

import apb.tests.build.ApbTestCase;
import apb.tests.testutils.FileAssert;

import static java.util.Arrays.asList;

import static apb.utils.FileUtils.makePath;
//
// User: emilio
// Date: Sep 24, 2009
// Time: 3:24:10 PM

//
public class IndexTest
    extends ApbTestCase
{
    //~ Methods ..............................................................................................

    public void test()
    {
        final long ts = (System.currentTimeMillis() - 10000) / 1000 * 1000;

        final File indexFile = new File(tmpFile(Constants.DEFINITIONS_CACHE));
        env.putProperty(Constants.DEFINITIONS_CACHE_PROPERTY, indexFile.getPath());
        env.putProperty("project.path", makePath(projectPath));

        final Set<File> path = Apb.loadProjectPath();

        DefinitionsIndex index = new DefinitionsIndex(env, path);
        assertEquals(asList("Chat", "HelloWorld", "Info", "Math", "Samples", "submod.Mod", "tests.Math")
                     .toString(), index.toString());
        FileAssert.assertExists(indexFile);
        indexFile.setLastModified(ts);

        index = new DefinitionsIndex(env, path);

        // Ensure that it was not written again
        assertEquals(ts, indexFile.lastModified());

        assertEquals(asList("Math", "tests.Math").toString(), index.findAllByName("Mat").toString());

        ModuleInfo moduleInfo = index.searchCurrentDirectory();
        assertNull(moduleInfo);

        final String mathProject = dataFile("projects/math");
        moduleInfo = index.searchByDirectory(mathProject);

        if (moduleInfo != null) {
            assertEquals("Math", String.valueOf(moduleInfo));
            assertEquals(MATH_EXPECTED_COMMANDS.toString(), moduleInfo.getCommands().toString());
            assertEquals(mathProject, moduleInfo.getContentDir().getPath());
            assertEquals("package", moduleInfo.getDefaultCommand());
            assertEquals("math", moduleInfo.getId());
            assertEquals(dataFile("projects/DEFS/Math"), moduleInfo.getPath());
        }
    }

    //~ Static fields/initializers ...........................................................................

    static final List<String> MATH_EXPECTED_COMMANDS =
        asList("clean", "compile", "compile-tests", "help", "idegen:eclipse", "idegen:idea", "javadoc",
               "module:clone", "package", "resources", "run-minimal-tests", "run-tests");
}
