

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

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import apb.testrunner.output.TestReport;
import apb.utils.DirectoryScanner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//
// User: emilio
// Date: Nov 6, 2008
// Time: 1:13:23 PM

//
public class TestRunner
{
    //~ Instance fields ......................................................................................

    private boolean failEmpty;
    private boolean verbose;

    @NotNull private final File basedir;
    @NotNull private final File outputDir;

    @NotNull private final List<String> excludes;
    @NotNull private final List<String> includes;
    @NotNull private final List<String> testGroups;

    //~ Constructors .........................................................................................

    public TestRunner(@NotNull File basedir, File outputDir, @NotNull List<String> includes,
                      @NotNull List<String> excludes, @NotNull List<String> testGroups)
    {
        this.basedir = basedir;
        this.includes = includes;
        this.excludes = excludes;
        this.outputDir = outputDir;
        this.testGroups = testGroups;
    }

    //~ Methods ..............................................................................................

    public static Set<String> listTests(ClassLoader testsClassLoader, Invocation creator, File basedir,
                                        List<String> includes, List<String> excludes, String singleTest)
        throws TestSetFailedException
    {
        final TestSetCreator<?> testCreator = (TestSetCreator) creator.instantiate(testsClassLoader);
        return loadTests(testsClassLoader, testCreator, basedir, includes, excludes, singleTest).keySet();
    }

    public static int worseResult(int r1, int r2)
    {
        return Math.min(r1, r2);
    }

    public int run(Invocation creator, TestReport report, ClassLoader testsClassLoader, String singleTest)
        throws TestSetFailedException
    {
        return run((TestSetCreator<?>) creator.instantiate(testsClassLoader), report, testsClassLoader,
                   singleTest);
    }

    public int run(TestSetCreator<?> creator, TestReport report, ClassLoader testsClassLoader,
                   String singleTest)
        throws TestSetFailedException
    {
        final Collection<TestSet> tests =
            loadTests(testsClassLoader, creator, basedir, includes, excludes, singleTest).values();

        report = report.init(outputDir);
        report.startRun(tests.size());

        int exit = OK;

        for (TestSet<?> testSet : tests) {
            boolean ok = testSet.run(testsClassLoader, report, testGroups);

            if (!ok) {
                exit = ERROR;
            }
        }

        report.stopRun();

        return exit == OK ? exitValue(report) : ERROR;
    }

    public int runOne(String suite, TestSetCreator<?> creator, ClassLoader testsClassLoader,
                      TestReport report, String singleTest)
        throws TestSetFailedException
    {
        report = report.init(outputDir);
        TestSet<?> testSet = loadTest(testsClassLoader, creator, suite, singleTest);

        if (testSet != null) {
            testSet.run(testsClassLoader, report, testGroups);
        }

        return exitValue(report);
    }

    public void setVerbose(boolean v)
    {
        verbose = v;
    }

    public void setFailEmpty(boolean v)
    {
        failEmpty = v;
    }

    public boolean isVerbose()
    {
        return verbose;
    }

    private static <T> Set<String> listTestsNames(ClassLoader testsClassLoader, TestSetCreator<T> creator,
                                                      File basedir, List<String> includes,
                                                      List<String> excludes)
        throws TestSetFailedException
    {
        Set<String> testSets = new HashSet<String>();

        // Load tests
        for (String file : collectTests(basedir, excludes, includes)) {
            Class<T> testClass = loadTestClass(testsClassLoader, file, creator.getTestClass());

            if (Modifier.isAbstract(testClass.getModifiers())) {
                continue;
            }

            testSets.add(testClass.getName());
        }

        return testSets;
    }

    private static <T> Map<String, TestSet> loadTests(ClassLoader testsClassLoader, TestSetCreator<T> creator,
                                                      File basedir, List<String> includes,
                                                      List<String> excludes, String singleTest)
        throws TestSetFailedException
    {
        Map<String, TestSet> testSets = new TreeMap<String, TestSet>();

        // Load tests
        for (String file : collectTests(basedir, excludes, includes)) {
            TestSet<T> testSet = loadTest(testsClassLoader, creator, file, singleTest);

            if (testSet != null) {
                if (testSets.containsKey(testSet.getName())) {
                    throw new TestSetFailedException("Duplicate test '" + testSet.getName() + "'");
                }

                testSets.put(testSet.getName(), testSet);
            }
        }

        return testSets;
    }

    @Nullable private static <T> TestSet<T> loadTest(ClassLoader testsClassLoader, TestSetCreator<T> creator,
                                                     String suite, String singleTest)
        throws TestSetFailedException
    {
        final Class<T> testClass = loadTestClass(testsClassLoader, suite, creator.getTestClass());

        if (Modifier.isAbstract(testClass.getModifiers())) {
            return null;
        }

        return creator.createTestSet(testClass, singleTest);
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> loadTestClass(ClassLoader classLoader, String file, Class<T> testClass)
        throws TestSetFailedException
    {
        try {
            String className = file.replace(File.separatorChar, '.');
            className = className.substring(0, className.length() - ".class".length());
            final Class<?> c = classLoader.loadClass(className);

            if (testClass.isAssignableFrom(c)) {
                return (Class<T>) c;
            }

            throw new TestSetFailedException("Invalid type for class: " + c.getName());
        }
        catch (ClassNotFoundException e) {
            throw new TestSetFailedException(e);
        }
    }

    private static List<String> collectTests(File basedir, List<String> excludes, List<String> includes)
        throws TestSetFailedException
    {
        List<String> result = new ArrayList<String>();

        if (basedir.exists()) {
            DirectoryScanner scanner = new DirectoryScanner(basedir, includes, excludes);

            for (String s : scanner.scan()) {
                if (s.endsWith(".class")) {
                    result.add(s);
                }
            }
        }

        return result;
    }

    private int exitValue(TestReport testReport)
    {
        return failEmpty && testReport.getSuitesRun() == 0 ? NO_TESTS
                                                           : testReport.getSuitesFailed() == 0 ? OK : FAILURE;
    }

    //~ Static fields/initializers ...........................................................................

    public static final int  NO_TESTS = -1;
    public static final int  OK = 0;
    private static final int FAILURE = -2;
    public static final int  ERROR = -3;
}
