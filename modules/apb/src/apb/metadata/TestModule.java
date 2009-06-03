

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import apb.Environment;
import apb.tasks.TestTask;
import apb.testrunner.output.TestReport;

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
     *  Info for coverage
     */
    public final CoverageInfo coverage = new CoverageInfo();

    /**
     * A custom creator classname
     */
    public String customCreator = null;

    /**
     * Whether to enable assertions when running the tests
     */
    public boolean enableAssertions = true;

    /**
     * Enable the java debugger in the forked process
     */
    public boolean enableDebugger = false;

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
     * Max. memory allocate for the tests (in megabytes).
     */
    public int memory = 256;

    /**
      * The directory to generate the reports output
      */
    @BuildProperty public String reportsDir = "$moduledir/output/reports";

    /**
     * The type of runner for the test
     */
    public TestType testType = TestType.JUNIT;

    /**
     * Working directory for running the tests
     */
    @BuildProperty public String workingDirectory = "$moduledir";

    /**
     * Environment variables to be set when running the tests
     */
    private final Map<String, String> environment = new HashMap<String, String>();

    /**
     * The list of tests files to exclude.
     */
    private final List<String> excludes = new ArrayList<String>();

    /**
     * The list of tests files to include.
     * If empty {@link TestModule#DEFAULT_INCLUDES} and {@link TestModule#DEFAULT_EXCLUDES} are used
     */
    private final List<String> includes = new ArrayList<String>();

    /**
     * The list of tests groups to include.
     */
    private final List<String> groups = new ArrayList<String>();



    /**
     * Properties to be set when running the tests
     */
    private final Map<String, String> properties = new HashMap<String, String>();

    /**
     * test reports
     */
    private final List<TestReport> reports = new ArrayList<TestReport>();

    /**
     * The module being tested
     */
    private Module module;

    //~ Methods ..............................................................................................

    public List<String> includes()
    {
        return includes;
    }

    public List<String> excludes()
    {
        return excludes;
    }

    public List<String> groups() {
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

    /**
     * Method used to set the Reports to be generated when running the test.
     * @param report The Report generators to be used
     */
    public void reports(TestReport... report)
    {
        reports.addAll(Arrays.asList(report));
    }

    /**
     * Method used to define the list of tests to be run
     * @param patterns The list of patterns to be included
     */
    public void includes(String... patterns)
    {
        includes.addAll(Arrays.asList(patterns));
    }

    /**
     * Method used to define the list of tests to be excluded from the run
     * @param patterns The list of patterns to be excluded
     */
    public void excludes(String... patterns)
    {
        excludes.addAll(Arrays.asList(patterns));
    }


    /**
     * Method used to define the list of test group to be run
     * @param patterns The list of test groups to be included
     */
    public void groups(String... patterns)
    {
        groups.addAll(Arrays.asList(patterns));
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
     * @param value Property value
     */
    public void setProperty(String var, String value)
    {
        properties.put(var, value);
    }

    /**
     * Run the tests
     * @param env
     */
    @BuildTarget(depends = "compile", recursive=false)
    public void run(Environment env)
    {
        TestTask.execute(env);
    }

    /**
     * Run the minimal tests
     * @param env
     */
    @BuildTarget(depends = "compile", recursive=false)
    public void runMinimal(Environment env)
    {
        env.putProperty("tests.groups", "minimal");

        TestTask.execute(env);
    }

    @Override public void clean(Environment env)
    {
        super.clean(env);
        TestTask.cleanReports(env);
    }

    /**
     * Define the module this test is testing
     * Update the dependencies
     * @param m The original module
     */
    void setModule(Module m)
    {
        if (module == null) {
            dependencies.add(m);
            dependencies.addAll(m.dependencies());
            module = m;
        }
    }

    //~ Static fields/initializers ...........................................................................

    public static final List<String> DEFAULT_INCLUDES = Arrays.asList("**/*Test.class", "**/*TestCase.class", "**/*TestSuite.class");
    public static final List<String> DEFAULT_EXCLUDES = Arrays.asList("**/*$*");

    //~ Instance initializers ................................................................................

    {
        /**
         * By default tests do not generate any jar
         */
        pkg.type = PackageType.NONE;
    }

}
