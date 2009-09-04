

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


package apb.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import apb.TestModuleHelper;

import apb.tasks.TestTask;

import apb.testrunner.output.TestReport;

import org.jetbrains.annotations.NotNull;

import static java.util.Arrays.asList;

/**
 * This class defines a TestModule for the building system
 * This class have handy defaults for several attributes that assume the following layout:
 * <pre>
 *
 *  basedir/
 *         +--- project-definitions--+
 *                                   +-- Module1.java
 *                                   +-- Module2.java
 *                                   .
 *                                   .
 *                                   +-- ModuleN.java
 *
 *        +--- module1/               <-- Test Working directory
 *                    +-- src    --   <-- Sources here
 *                    +-- gsrc   --   <-- Generated Sources here
 *                    .
 *                    +-- output +-- classes --   <-- Compiled classes here
 *                               +-- reports --   <-- Test reports here
 *                               +-- coverage--   <-- Coverage reports here
 *
 *        +--- module2/
 *        .
 *        .
 *        +--- moduleN/
 *
 * </pre>

 */
public class TestModule
    extends Module
{
    //~ Instance fields ......................................................................................

    /**
     * Wheter the dependencies classpath is included in the system classoader.
     */
    public boolean classPathInSystemClassloader = false;

    /**
     * Whether to enable assertions when running the tests
     */
    public boolean enableAssertions = true;

    /**
     * Enable the java debugger in the forked process
     */
    @BuildProperty public boolean enableDebugger = false;

    /**
     * Fail if no tests are found
     */
    public boolean failIfEmpty = false;

    /**
     * Fail the build when a test fails
     */
    public boolean failOnError = false;

    /**
     *  Whether to fork a new process to run the tests or not.
     */
    public boolean fork = true;

    /**
     *  Whether to fork a new process for EACH test suite.
     */
    public boolean forkPerSuite = false;

    /**
     *  Info for coverage
     */
    public CoverageInfo coverage = new CoverageInfo();

    /**
     * Max. memory allocate for the tests (in megabytes).
     */
    public int memory = 256;

    /**
     * A custom creator classname
     */
    public String customCreator = null;

    /**
      * The directory to generate the reports output
      */
    @BuildProperty public final String reportsDir = "output/$moduledir/reports";

    /**
     * Working directory for running the tests
     */
    @BuildProperty public final String workingDirectory = "output/$moduledir";

    /**
     * The type of runner for the test
     */
    public final TestType testType = TestType.JUNIT;

    /**
     * The list of tests files to exclude.
     */
    private final List<String> excludes = new ArrayList<String>();

    /**
     * The list of tests groups to include.
     */
    private final List<String> groups = new ArrayList<String>();

    /**
     * The list of tests files to include.
     * If empty {@link TestModule#DEFAULT_INCLUDES} and {@link TestModule#DEFAULT_EXCLUDES} are used
     */
    private final List<String> includes = new ArrayList<String>();

    /**
     * test reports
     */
    private final List<TestReport> reports = new ArrayList<TestReport>();

    /**
     * The list of properties to copy from the apb to the test to be run
     */
    private final List<String> useProperties = new ArrayList<String>();

    /**
     * Environment variables to be set when running the tests
     */
    private final Map<String, String> environment = new HashMap<String, String>();

    /**
     * Properties to be set when running the tests
     */
    private final Map<String, String> properties = new HashMap<String, String>();

    /**
     * The module being tested
     */
    private Module moduleToTest;

    //~ Methods ..............................................................................................

    public List<String> includes()
    {
        return includes;
    }

    public List<String> excludes()
    {
        return excludes;
    }

    public List<String> groups()
    {
        return groups;
    }

    public List<TestReport> reports()
    {
        return reports;
    }

    public Map<String, String> environment()
    {
        return environment;
    }

    public Map<String, String> properties()
    {
        return properties;
    }

    public List<String> useProperties()
    {
        return useProperties;
    }

    /**
     * Method used to set the Reports to be generated when running the test.
     * @param report The Report generators to be used
     */
    public void reports(TestReport... report)
    {
        reports.addAll(asList(report));
    }

    /**
     * Method used to define the list of tests to be run
     * @param patterns The list of patterns to be included
     */
    public void includes(String... patterns)
    {
        includes.addAll(asList(patterns));
    }

    /**
     * Method used to define the list of tests to be excluded from the run
     * @param patterns The list of patterns to be excluded
     */
    public void excludes(String... patterns)
    {
        excludes.addAll(asList(patterns));
    }

    /**
     * Method used to define the list of test group to be run
     * @param patterns The list of test groups to be included
     */
    public void groups(String... patterns)
    {
        groups.addAll(asList(patterns));
    }

    /**
     * Add or Set a variable to the environment used when running the tests
     * @param var Environment variable name
     * @param value The value
     */
    public void setenv(String var, String value)
    {
        environment.put(var, value);
    }

    /**
     * Add or Set a property to be used when running the tests
     * @param var Property name
     * @param value Property value. The value will be expanded.
     * @see apb.Environment#expand(String)
     */
    public void setProperty(String var, String value)
    {
        properties.put(var, value);
    }

    /**
     * A list of properties to be copied from the apb Environment to the test to be run
     */
    public void useProperties(String... props)
    {
        useProperties.addAll(asList(props));
    }

    /**
     * Run the tests
     */
    @BuildTarget(
                 depends = "package",
                 recursive = false
                )
    public void run()
    {
        TestTask.execute(getHelper());
    }

    /**
     * Run the minimal tests
     */
    @BuildTarget(
                 depends = "package",
                 recursive = false
                )
    public void runMinimal()
    {
        getHelper().putProperty("tests.groups", "minimal");

        TestTask.execute(getHelper());
    }

    @NotNull @Override public TestModuleHelper getHelper()
    {
        return (TestModuleHelper) super.getHelper();
    }

    @Override public void clean()
    {
        super.clean();
        TestTask.cleanReports(getHelper());
    }

    public Module getModuleToTest()
    {
        return moduleToTest;
    }

    /**
     * Define the module this test is testing
     * Update the dependencies
     * @param m The original module
     */
    void setModuleToTest(Module m)
    {
        if (moduleToTest == null) {
            dependencies.add(m);
            dependencies.addAll(m.dependencies());
            moduleToTest = m;
        }
    }

    //~ Static fields/initializers ...........................................................................

    public static final List<String> DEFAULT_INCLUDES =
        asList("**/*Test.class", "**/*TestCase.class", "**/*TestSuite.class");
    public static final List<String> DEFAULT_EXCLUDES = asList("**/*$*");

    //~ Instance initializers ................................................................................

    {
        /**
         * By default tests do not generate any jar
         */
        pkg.type = PackageType.NONE;
    }
}
