

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

import apb.DefinitionException;

import apb.tests.testutils.FileAssert;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class TestTest
    extends ApbTestCase
{
    //~ Methods ..............................................................................................

    public void testCompile()
        throws DefinitionException
    {
        build("Math", "compile-tests");
        checkOutput(COMPILING_1_FILE,  //
                    COMPILING_1_FILE);
        FileAssert.assertExists(fractionClass);
        FileAssert.assertExists(fractionTestClass);

        build("Math", "compile-tests");
        checkOutput();

        build("Math", "clean");
        checkOutput(DELETING_DIRECTORY + mathClasses,  //
                    DELETING_DIRECTORY + testMathClasses);

        FileAssert.assertDoesNotExist(fractionClass);
        FileAssert.assertDoesNotExist(fractionTestClass);
    }

    public void testRun()
        throws DefinitionException
    {
        final File   coverageDir = new File(testOutputDir, "coverage");
        final File   coverageHtml = new File(coverageDir, "coverage.html");
        final String reportDir = new File(testOutputDir, "reports").getPath();

        build("Math", "run-tests");
        checkOutput(COMPILING_1_FILE,  //
                    COMPILING_1_FILE,  //
                    BUILDING + mathJar.getPath(),  //
                    SUITE_1_1_RUN,  //
                    SEPAR,  //
                    TEST_DIVIDE_FAILED1, TEST_DIVIDE_FAILED2, TEST_DIVIDE_FAILED3,  //
                    STACK,  //
                    "", "",  //
                    SEPAR,  //
                    "",  //
                    SEPAR,  //
                    MODULE_TESTS_MATH,  //
                    SEPAR,  //
                    EMMA,  //
                    COVERAGE,  //
                    SOME_TESTS_HAVE_FAILED,  //
                    CHECK + reportDir);

        FileAssert.assertExists(fractionClass);
        FileAssert.assertExists(fractionTestClass);
        FileAssert.assertExists(coverageHtml);

        build("Math", "clean");
        checkOutput(DELETING_DIRECTORY + mathClasses,  //
                    DELETING_FILE + mathJar.getPath(),  //
                    DELETING_DIRECTORY + testMathClasses);

        FileAssert.assertDoesNotExist(fractionClass);
        FileAssert.assertDoesNotExist(fractionTestClass);
        FileAssert.assertDoesNotExist(coverageHtml);
        FileAssert.assertDoesNotExist(coverageDir);
    }

    //~ Static fields/initializers ...........................................................................

    private static final String MODULE_TESTS_MATH = "module = tests.Math\\+\\+";

    private static final String SUITE_1_1_RUN =
        "Suite ( 1/ 1): math.test.FractionTest                                 4 tests run\\+";
    private static final String COVERAGE = "Coverage summary Information\\+";
    private static final String EMMA = "EMMA\\+\\+";
    private static final String SEPAR = "-------------------------------------------------\\+";
    private static final String TEST_DIVIDE_FAILED1 = "Test: testDivide: FAILED";
    private static final String TEST_DIVIDE_FAILED2 = "expected:<1/1> but was:<4/16>";
    private static final String TEST_DIVIDE_FAILED3 =
        "junit.framework.AssertionFailedError: expected:<1/1> but was:<4/16>";
    private static final String STACK = "\tat\\+\\+";
}
