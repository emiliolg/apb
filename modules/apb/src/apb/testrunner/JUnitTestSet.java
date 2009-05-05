

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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;

import apb.testrunner.output.TestReport;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
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

    public void execute(final TestReport report, ClassLoader loader, List<String> testGroups)
        throws TestSetFailedException
    {
        Test test = constructTestObject(getTestClass(), testGroups);

        if (test != null) {
            TestResultWrapper testResult = new TestResultWrapper();

            testResult.addListener(new TestListenerAdaptor(report));

            test.run(testResult);
        }
    }

    public Test createSuite(final Class theClass, List<String> testGroups)
    {
        TestSuite suite = new TestSuite();
        suite.setName(theClass.getName());

        try {
            TestSuite.getTestConstructor(theClass);
        }
        catch (NoSuchMethodException e) {
            return null;
        }

        if (!Modifier.isPublic(theClass.getModifiers())) {
            return null;
        }

        Class        superClass = theClass;
        List<String> names = new ArrayList<String>();

        while (Test.class.isAssignableFrom(superClass)) {
            Method[] methods = superClass.getDeclaredMethods();

            for (Method method : methods) {
                addTestMethod(suite, method, names, superClass, testGroups);
            }

            superClass = superClass.getSuperclass();
        }

        if (suite.countTestCases() == 0) {
            return null;
        }

        return suite;
    }

    private Test constructTestObject(Class<?> clazz, List<String> testGroups)
    {
        // First try to see if there is a 'suite' method.

        try {
            Method suiteMethod = clazz.getMethod(SUITE_METHOD);

            final int m = suiteMethod.getModifiers();

            if (isPublic(m) && isStatic(m) && Test.class.isAssignableFrom(suiteMethod.getReturnType())) {

                 Test test = (Test) suiteMethod.invoke(null);

                if(test instanceof TestSuite) {
                      return wrapSuite(testGroups, (TestSuite)test);
                }
                else{
                    return createSuite(clazz, testGroups);
                }
//                if (testGroups != null && !testGroups.isEmpty()) {
//                    for (String groupName : testGroups) {
//
//                        if (annotation != null){
//                            if(annotation.group().equals(groupName)){
//
//                                if(annotation.skip()) {
//                                    return new SkippedTest((Test) suiteMethod.invoke(null));
//                                }
//                                else{
//                                    return wrapSuite(testGroups, suiteMethod);
//                                }
//                            }
//                        }
//                    }
//
//                }
//                else {
//
//                if(annotation != null && annotation.skip()){
//                    return new SkippedTest((Test) suiteMethod.invoke(null));
//                }
//                else{
//                    return wrapSuite(testGroups, suiteMethod);
//                }
//                }
            }

        }
        catch (Exception e) {
            // No suite method
        }

        return createSuite(clazz, testGroups);
    }

    private Test wrapSuite(List<String> testGroups, TestSuite suite) throws IllegalAccessException, InvocationTargetException {
        Method suiteMethod;
        try {
            suiteMethod = suite.getClass().getMethod(SUITE_METHOD);
        } catch (NoSuchMethodException e) {
            try {
                return createSuite(getTestClass().getClassLoader().loadClass(suite.getName()), testGroups);
            } catch (ClassNotFoundException e1) {
                return null;
            }
        }
        final apb.annotation.Test annotation =
                suiteMethod.getAnnotation(apb.annotation.Test.class);

        if (testGroups != null && !testGroups.isEmpty()) {
            for (String groupName : testGroups) {

                if (annotation != null){
                    if(annotation.group().equals(groupName)){

                        if(annotation.skip()) {
                            return new SkippedTest(suite);
                        }
                        break;
                    }
                }
            }
        }

        if(annotation != null && annotation.skip()){
            return new SkippedTest(suite);
        }

        TestSuite testSuite = new TestSuite();
        testSuite.setName(suite.getName());
        TestSuite wrapper = new TestSuite(suite.getName());
        for (Enumeration e = suite.tests(); e.hasMoreElements();){
            testSuite.addTest(wrapTest((Test)e.nextElement(), testGroups));
        }
        return wrapper;
    }

    private Test wrapTest(Test test, List<String> testGroups) throws InvocationTargetException, IllegalAccessException {
        if(test instanceof TestSuite){
            return wrapSuite(testGroups, (TestSuite) test);
        }
        else{
            Method runMethod;
            try {
                runMethod = test.getClass().getMethod(((TestCase)test).getName());
            } catch (NoSuchMethodException e) {
                try {
                    Method method = test.getClass().getMethod("getTestName");
                    runMethod = test.getClass().getMethod((String)method.invoke(test));
                } catch (NoSuchMethodException e1) {
                    return test;
                }
            }
            final apb.annotation.Test annotation =
                    runMethod.getAnnotation(apb.annotation.Test.class);

            if (testGroups != null && !testGroups.isEmpty()) {
                for (String groupName : testGroups) {

                    if (annotation != null){
                        if(annotation.group().equals(groupName)){

                            if(annotation.skip()) {
                                return new SkippedTest(test);
                            }
                            break;
                        }
                    }
                }
            }

            if(annotation != null && annotation.skip()){
                return new SkippedTest(test);
            }

            return test;
        }
    }

    private void addTestMethod(TestSuite suite, Method m, List<String> names, Class theClass,
                               List<String> testGroups)
    {
        String name = m.getName();

        if (names.contains(name)) {
            return;
        }

        if (!isPublicTestMethod(m)) {
            if (!isTestMethod(m)){
                return;
            }
        }
        final apb.annotation.Test annotation = m.getAnnotation(apb.annotation.Test.class);

        if (testGroups != null && !testGroups.isEmpty()) {
            for (String testGroup : testGroups) {

                if (annotation != null){
                    if(annotation.group().equals(testGroup)){
                        names.add(name);
                        if(annotation.skip()) {
                            suite.addTest(new SkippedTest(TestSuite.createTest(theClass, name)));
                        }
                        else{
                            suite.addTest(TestSuite.createTest(theClass, name));
                        }
                        break;
                    }
                }
            }
        }
        else {
            if(annotation != null && annotation.skip()){
                suite.addTest(new SkippedTest(TestSuite.createTest(theClass, name)));
            }
            else{
                suite.addTest(TestSuite.createTest(theClass, name));
            }

        }

    }

    private boolean isPublicTestMethod(Method m)
    {
        return isTestMethod(m) && Modifier.isPublic(m.getModifiers());
    }

    private boolean isTestMethod(Method m)
    {
        String  name = m.getName();
        Class[] parameters = m.getParameterTypes();
        Class   returnType = m.getReturnType();
        return parameters.length == 0 && name.startsWith("test") && returnType.equals(Void.TYPE);
    }

    //~ Static fields/initializers ...........................................................................

    @NonNls private static final String SUITE_METHOD = "suite";

    //~ Inner Classes ........................................................................................

    static class TestListenerAdaptor
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

        public void addSkipped(){
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
