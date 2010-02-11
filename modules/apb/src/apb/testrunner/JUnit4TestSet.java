
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

import apb.testrunner.output.TestReport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.util.List;

final class JUnit4TestSet
    extends TestSet<Object>
{
    //~ Instance fields ......................................................................................

    //~ Constructors .........................................................................................

    private JUnit4TestSet(@NotNull Class<Object> testClass)
    {
        super(testClass);
    }

    //~ Methods ..............................................................................................

    public void execute(@NotNull final TestReport report, @NotNull ClassLoader classLoader,
                        @NotNull List<String> testGroups)
        throws TestSetFailedException
    {

        final Thread      currentThread = Thread.currentThread();
        final ClassLoader previousClassLoader = currentThread.getContextClassLoader();

        try {
            currentThread.setContextClassLoader(classLoader);

            final JUnitCore jUnitCore = new JUnitCore();
            final TestListenerAdaptor adaptor = new TestListenerAdaptor(report);
            jUnitCore.addListener(adaptor);
            jUnitCore.run(getTestClass());
        }
        finally {
            currentThread.setContextClassLoader(previousClassLoader);
        }

    }

    @Nullable static JUnit4TestSet buildTestSet(Class<Object> testClass, String singleTest)
    {
        assert JunitTestSetCreator.isJUnit4Test(testClass);
        return new JUnit4TestSet(testClass);
    }

    //~ Static fields/initializers ...........................................................................

    //~ Inner Classes ........................................................................................
    public static class TestListenerAdaptor extends RunListener {

        private final TestReport report;
        public TestListenerAdaptor(TestReport report)
        {
            this.report = report;
        }

        @Override
        public void testFinished(Description description) throws Exception {
            report.endTest();
        }

        @Override
        public void testStarted(Description description) throws Exception {
            report.startTest(description.getMethodName());
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            report.failure(failure.getException());
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            report.failure(failure.getException());
        }

        @Override
        public void testIgnored(Description description) throws Exception {
            report.skip();
        }
   }
}