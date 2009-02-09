
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

package apb.testrunner;

import java.lang.reflect.Method;

import apb.testrunner.output.TestReport;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.jetbrains.annotations.NonNls;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
//
// User: emilio
// Date: Nov 7, 2008
// Time: 1:52:17 PM

//
public final class JUnitTestSet
    extends TestSet<junit.framework.Test>
{
    //~ Constructors .........................................................................................

    public JUnitTestSet(Class<Test> testClass)
        throws TestSetFailedException
    {
        super(testClass);
    }

    //~ Methods ..............................................................................................

    public void execute(final TestReport report, ClassLoader loader)
        throws TestSetFailedException
    {
        Test       test = constructTestObject();
        TestResult testResult = new TestResult();

        testResult.addListener(new TestListenerAdaptor(report));

        test.run(testResult);
    }

    private Test constructTestObject()
    {
        // First try to see if there is a 'suite' method.

        try {
            Method suiteMethod = getTestClass().getMethod(SUITE_METHOD);

            final int m = suiteMethod.getModifiers();

            if (isPublic(m) && isStatic(m) && Test.class.isAssignableFrom(suiteMethod.getReturnType())) {
                return (Test) suiteMethod.invoke(null);
            }
        }
        catch (Exception e) {
            // No suite method
        }

        // If not call TestSuite constructor.

        return new TestSuite(getTestClass());
    }

    //~ Static fields/initializers ...........................................................................

    @NonNls private static final String SUITE_METHOD = "suite";

    //~ Inner Classes ........................................................................................

    private static class TestListenerAdaptor
        implements TestListener
    {
        private final TestReport report;

        public TestListenerAdaptor(TestReport report)
        {
            this.report = report;
        }

        public void addError(Test test, Throwable t)
        {
            validateCurrentTest(test);
            report.failure(t);
        }

        public void addFailure(Test test, AssertionFailedError t)
        {
            validateCurrentTest(test);
            report.failure(t);
        }

        public void endTest(Test test)
        {
            validateCurrentTest(test);
            report.endTest();
        }

        public void startTest(Test test)
        {
            report.startTest(testName(test));
        }

        private static String testName(Test test)
        {
            String name = "";

            if (test instanceof TestCase) {
                name = ((TestCase) test).getName();
            }

            return name;
        }

        private void validateCurrentTest(Test test)
        {
            final String name = testName(test);
            final String current = report.getCurrentTest();

            if (!name.equals(current)) {
                throw new IllegalStateException(name + " vs " + current);
            }
        }
    }
}
