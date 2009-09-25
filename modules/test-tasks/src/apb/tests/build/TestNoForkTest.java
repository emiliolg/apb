

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
import java.util.Map;

import apb.DefinitionException;

import apb.tests.testutils.FileAssert;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class TestNoForkTest
    extends ApbTestCase
{
    //~ Methods ..............................................................................................

    public void testRunNoFork()
        throws DefinitionException
    {
        final File   classFile = new File(tmpdir, "output/classes/math/Fraction.class").getAbsoluteFile();
        final File   testClassFile =
            new File(tmpdir, "output/classes/math/test/FractionTest.class").getAbsoluteFile();
        final File   jarFile = new File(tmpdir, "output/lib/tests-math-1.0.jar").getAbsoluteFile();
        final String reportFile = new File(tmpdir, "output/reports").getAbsolutePath();

        build("Math", "run-tests");
        checkOutput(COMPILING_1_FILE,  //
                    COMPILING_1_FILE,  //
                    BUILDING + jarFile.getPath(),  //
                    SOME_TESTS_HAVE_FAILED,  //
                    CHECK + reportFile);

        FileAssert.assertExists(classFile);
        FileAssert.assertExists(testClassFile);

        build("Math", "clean");
        checkOutput(DELETING_DIRECTORY + classFile.getParentFile().getParent(),  //
                    DELETING_DIRECTORY + classFile.getParent(),  //
                    DELETING_DIRECTORY + testClassFile.getParent(),  //
                    DELETING_FILE + jarFile.getPath(),  //
                    DELETING_DIRECTORY + reportFile,  //
                    DELETING_DIRECTORY + new File(tmpdir, "output/coverage").getAbsolutePath());
        FileAssert.assertDoesNotExist(classFile);
        FileAssert.assertDoesNotExist(testClassFile);
    }

    @Override protected void createEnv(Map<String, String> properties)
    {
        properties.put("coverage.enable", "false");
        properties.put("fork", "false");
        super.createEnv(properties);
    }
}
