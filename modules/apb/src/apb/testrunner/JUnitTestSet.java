
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
import java.lang.reflect.Modifier;

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
import java.util.List;
import java.util.ArrayList;
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

    public void execute(final TestReport report, ClassLoader loader, List<String> testGroups)
        throws TestSetFailedException
    {
        Test       test = constructTestObject(testGroups);

        if(test != null){
            TestResult testResult = new TestResult();

            testResult.addListener(new TestListenerAdaptor(report));

            test.run(testResult);
        }
    }

    private Test constructTestObject(List<String> testGroups)
    {
        // First try to see if there is a 'suite' method.

        try {
            Method suiteMethod = getTestClass().getMethod(SUITE_METHOD);

            final int m = suiteMethod.getModifiers();

            if (isPublic(m) && isStatic(m) && Test.class.isAssignableFrom(suiteMethod.getReturnType())) {
                if(testGroups != null && testGroups.size() > 0){
                    for (String groupName : testGroups) {
                        final apb.annotation.Test annotation = suiteMethod.getAnnotation(apb.annotation.Test.class);

                        if(annotation != null && annotation.group().equals(groupName)){
                        return (Test) suiteMethod.invoke(null);
                        }


                    }

                    return null;
                }
                else{
                    return (Test) suiteMethod.invoke(null);
                }

            }
        }
        catch (Exception e) {
            // No suite method
        }


        return createSuite(getTestClass(), testGroups);
    }

    public  TestSuite createSuite(final Class theClass, List<String> testGroups) {
        TestSuite suite = new TestSuite();
        suite.setName(theClass.getName());
        try {
            TestSuite.getTestConstructor(theClass);
        } catch (NoSuchMethodException e) {
            return null;
        }

        if (!Modifier.isPublic(theClass.getModifiers())) {
            return null;
        }

        Class superClass= theClass;
        List<String> names= new ArrayList<String>();
        while (Test.class.isAssignableFrom(superClass)) {
            Method[] methods= superClass.getDeclaredMethods();
            for (Method method : methods) {
                addTestMethod(suite, method, names, theClass, testGroups);
            }
            superClass= superClass.getSuperclass();
        }
        if (suite.countTestCases() == 0)
            return null;

        return suite;
    }

    private void addTestMethod(TestSuite suite, Method m, List<String> names, Class theClass, List<String> testGroups) {
        String name= m.getName();
        if (names.contains(name))
            return;
        if (!isPublicTestMethod(m)) {
            if (!isTestMethod(m))
                return;
        }

        if(testGroups != null && testGroups.size() > 0){

            for (String testGroup : testGroups) {
                final apb.annotation.Test annotation = m.getAnnotation(apb.annotation.Test.class);

                if(annotation != null && annotation.group().equals(testGroup)){
                    suite.addTest(TestSuite.createTest(theClass, name));
                }

            }
        }
        else{
            names.add(name);
            suite.addTest(TestSuite.createTest(theClass, name));
        }
    }


    private boolean isPublicTestMethod(Method m) {
        return isTestMethod(m) && Modifier.isPublic(m.getModifiers());
    }

       private boolean isTestMethod(Method m) {
         String name= m.getName();
         Class[] parameters= m.getParameterTypes();
         Class returnType= m.getReturnType();
         return parameters.length == 0 && name.startsWith("test") && returnType.equals(Void.TYPE);
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
