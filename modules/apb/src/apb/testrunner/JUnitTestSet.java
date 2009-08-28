
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import apb.testrunner.output.TestReport;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import junit.framework.TestSuite;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JUnitTestSet
    extends TestSet<Object>
{
    //~ Instance fields ......................................................................................

    private final Method    suiteMethod;
    private final TestSuite testSuite;

    //~ Constructors .........................................................................................

    private JUnitTestSet(Class<Object> testClass, Method method, TestSuite suite)
    {
        super(testClass);

        suiteMethod = method;
        testSuite = suite;

        assert method != null || suite != null : "Not a valid Test class : " + testClass;
    }

    //~ Methods ..............................................................................................

    public void execute(@NotNull final TestReport report, @NotNull ClassLoader classLoader,
                        @NotNull List<String> testGroups)
        throws TestSetFailedException
    {
        Test test = constructTestObject(testGroups);

        if (test != null) {
            TestResultWrapper testResult = new TestResultWrapper();

            testResult.addListener(new TestListenerAdaptor(report));

            final ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
            try {
                test.run(testResult);
            }
            finally {
                Thread.currentThread().setContextClassLoader(previousClassLoader);
            }
        }
    }

    @Nullable static JUnitTestSet buildTestSet(Class<Object> testClass)
    {
        final Method    method = getSuiteMethod(testClass);
        final TestSuite suite;

        //noinspection VariableNotUsedInsideIf
        if (method == null) {
            suite = buildSuite(testClass);

            if (suite == null) {
                return null;
            }
        }
        else {
            suite = null;
        }

        return new JUnitTestSet(testClass, method, suite);
    }

    /**
     * This is an ugly hack to filter-out useless TestSuites
     */
    private static boolean isValidSuite(TestSuite testSuite)
    {
        final int testCount = testSuite.testCount();

        if (testCount != 1) {
            return testCount > 1;
        }

        final Test test = testSuite.testAt(0);

        if (!(test instanceof TestCase)) {
            return true;
        }

        final TestCase testCase = (TestCase) test;
        return !"warning".equals(testCase.getName()) ||
               !testCase.getClass().getName().startsWith("junit.framework.");
    }

    @Nullable private static Method getSuiteMethod(Class<?> clazz)
    {
        final Method suiteMethod;

        try {
            suiteMethod = clazz.getMethod(SUITE_METHOD);
        }
        catch (NoSuchMethodException ignore) {
            return null;
        }

        final int m = suiteMethod.getModifiers();

        if (Modifier.isPublic(m) && Modifier.isStatic(m) &&
                Test.class.isAssignableFrom(suiteMethod.getReturnType())) {
            return suiteMethod;
        }

        return null;
    }

    @Nullable private static TestSuite buildSuite(Class<?> clazz)
    {
        if (!Test.class.isAssignableFrom(clazz)) {
            return null;
        }

        final TestSuite testSuite = new TestSuite(clazz);
        return isValidSuite(testSuite) ? testSuite : null;
    }

    private static boolean mustRun(Method suiteMethod, @NotNull List<String> testGroups)
    {
        final apb.annotation.Test annotation = suiteMethod.getAnnotation(apb.annotation.Test.class);

        // Check skip
        if (annotation != null && annotation.skip()) {
            return false;
        }

        // Has groups ?
        if (testGroups.isEmpty()) {
            return true;
        }

        // Has groups and no annotation -> skip
        if (annotation == null) {
            return false;
        }

        // Run for all groups ?
        if (apb.annotation.Test.ALL.equals(annotation.groups()[0])) {
            return true;
        }

        // Check if belongs to any of the groups
        for (String gr : annotation.groups()) {
            if (testGroups.contains(gr)) {
                return true;
            }
        }

        return false;
    }

    @Nullable private Test constructTestObject(@NotNull List<String> testGroups)
        throws TestSetFailedException
    {
        final Class<?> testClazz = getTestClass();

        // First try to see if there is a 'suite' method.
        final Method suiteMethod = this.suiteMethod;

        // No suite build one
        if (suiteMethod == null) {
            //but only if no annotations because can only annotate suites.
            if (!testGroups.isEmpty()) {
                return null;
            }

            return this.testSuite;
        }

        // Check if I've to run it and run it

        if (mustRun(suiteMethod, testGroups)) {
            try {
                return (Test) suiteMethod.invoke(null);
            }
            catch (InvocationTargetException e) {
                throw new TestSetFailedException(e.getTargetException());
            }
            catch (Exception e) {
                throw new TestSetFailedException(e);
            }
        }

        // Skip otherwise
        return null;
    }

    //~ Static fields/initializers ...........................................................................

    @NonNls private static final String SUITE_METHOD = "suite";

    //~ Inner Classes ........................................................................................

    public static class TestListenerAdaptor
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

        public void addSkipped()
        {
            report.skip();
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
