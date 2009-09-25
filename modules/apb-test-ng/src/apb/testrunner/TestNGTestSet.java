

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


package apb.testrunner;

import java.util.List;

import apb.testrunner.output.TestReport;

import org.jetbrains.annotations.NotNull;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;

import static apb.utils.CollectionUtils.listToString;
//
// User: emilio
// Date: May 15, 2009
// Time: 11:12:49 AM

//
public class TestNGTestSet
    extends TestSet<Object>
{
    //~ Instance fields ......................................................................................

    @NotNull private final String singleTest;

    //~ Constructors .........................................................................................

    public TestNGTestSet(Class<Object> testClass, String singleTest)
        throws TestSetFailedException
    {
        super(testClass);
        this.singleTest = singleTest;
    }

    //~ Methods ..............................................................................................

    public void execute(@NotNull TestReport report, @NotNull ClassLoader classLoader,
                        @NotNull List<String> testGroups)
        throws TestSetFailedException
    {
        TestNG tng = new TestNG(true);
        tng.setGroups(listToString(testGroups));

        if (!singleTest.isEmpty()) {
            tng.setDefaultTestName(singleTest);
        }

        tng.setVerbose(0);

        TestListenerAdaptor reporter = new TestListenerAdaptor(report);
        tng.addListener((Object) reporter);

        //tng.setOutputDirectory( report.getAbsolutePath() );
        final Class[] tcs = { getTestClass() };
        tng.setTestClasses(tcs);
        tng.run();
    }

    //~ Inner Classes ........................................................................................

    public static class TestListenerAdaptor
        implements ITestListener
    {
        private final TestReport report;

        public TestListenerAdaptor(TestReport report)
        {
            this.report = report;
        }

        public void addSkipped()
        {
            report.skip();
        }

        public void onTestStart(ITestResult testResult)
        {
            report.startTest(testName(testResult));
        }

        public void onTestSuccess(ITestResult result)
        {
            validateCurrentTest(result);
            report.endTest();
        }

        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        public void onTestFailure(ITestResult result)
        {
            validateCurrentTest(result);
            report.failure(result.getThrowable());
        }

        public void onTestSkipped(ITestResult result)
        {
            validateCurrentTest(result);
            report.skip();
        }

        public void onTestFailedButWithinSuccessPercentage(ITestResult result)
        {
            onTestFailure(result);
        }

        public void onStart(ITestContext iTestContext)
        {
            // empty
        }

        public void onFinish(ITestContext iTestContext)
        {
            // empty
        }

        private static String testName(ITestResult test)
        {
            return test.getName() + "(" + test.getTestClass().getName() + ")";
        }

        private void validateCurrentTest(ITestResult test)
        {
            final String name = testName(test);
            final String current = report.getCurrentTest();

            if (!name.equals(current)) {
                throw new IllegalStateException(name + " vs " + current);
            }
        }
    }
}
