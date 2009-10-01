

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
public class NoForkTest
    extends ApbTestCase
{
    //~ Methods ..............................................................................................

    public void testRunNoFork()
        throws DefinitionException
    {
        final File reportFile = new File(outputDir, "tests/math/reports");

        build("Math", "run-tests");
        checkOutput(COMPILING_1_FILE,  //
                    COMPILING_1_FILE,  //
                    BUILDING + mathJar.getPath(),  //
                    SOME_TESTS_HAVE_FAILED,  //
                    CHECK + reportFile);

        FileAssert.assertExists(fractionClass);
        FileAssert.assertExists(fractionTestClass);

        build("Math", "clean");
        checkOutput(DELETING_DIRECTORY + mathClasses,  //
                    DELETING_FILE + mathJar.getPath(),  //
                    DELETING_DIRECTORY + testMathClasses);
        FileAssert.assertDoesNotExist(fractionClass);
        FileAssert.assertDoesNotExist(fractionTestClass);
    }

    @Override protected void createEnv(Map<String, String> properties)
    {
        properties.put("coverage.enable", "false");
        properties.put("fork", "false");
        super.createEnv(properties);
    }
}
