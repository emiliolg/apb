
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
import static java.util.Arrays.asList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import apb.TestModuleHelper;
import apb.testrunner.output.TestReport;
import org.jetbrains.annotations.NotNull;

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
    //~ Instance initializers ................................................................................

    {
        /**
         * By default tests do not generate any jar
         */
        pkg.type = PackageType.NONE;
    }

    //~ Instance fields ......................................................................................

    /**
     *  Info for coverage
     */
    @BuildProperty public CoverageInfo coverage = new CoverageInfo();

    /**
     * A custom creator classname
     */
    @BuildProperty public String customCreator;

    /**
     * Whether to enable assertions when running the tests
     */
    @BuildProperty public boolean enableAssertions = true;

    /**
     * Enable the java debugger in the forked process
     */
    @BuildProperty public boolean enableDebugger;

    /**
     * Fail if no tests are found
     */
    @BuildProperty public boolean failIfEmpty;

    /**
     * Fail the build when a test fails
     */
    @BuildProperty public boolean failOnError;

    /**
     *  Whether to fork a new process to run the tests or not.
     */
    @BuildProperty public boolean fork = true;

    /**
     *  Whether to fork a new process for EACH test suite.
     */
    @BuildProperty public boolean forkPerSuite;

    /**
     * Max. memory allocate for the tests (in megabytes).
     */
    @BuildProperty public int memory = 256;

    /**
      * The directory to generate the reports output
      */
    @BuildProperty public String reportsDir = "$output-base/reports";

    @BuildProperty(description = "The name of a specified test to be run.")
    public String runOnly = "";

    /**
     * Wheter to show output in reports or not
     */
    @BuildProperty public boolean showOutput;

    /**
     * The type of runner for the test
     */
    public final TestType testType = TestType.JUNIT;

    /**
     * Indicates whether deep classpath is used for running tests
     */
    public boolean useDeepClasspath = true;

    /**
     * Working directory for running the tests
     */
    @BuildProperty public String workingDirectory = "$output-base";

    /**
     * test reports
     * Environment variables to be set when running the tests
     */
    private final Map<String, String> environment = new HashMap<String, String>();

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
     * Additional Java Args to be set when running the tests
     */
    private final List<String> javaArgs = new ArrayList<String>();

    /**
     * The module being tested
     */
    private Module moduleToTest;

    /**
     * Properties to be set when running the tests
     */
    private final Map<String, String> properties = new HashMap<String, String>();

    /**
     * test reports
     */
    private final List<TestReport.Builder> reports = new ArrayList<TestReport.Builder>();

    /**
     * The list of modules & libraries that if required by test-module, must run on the System ClassPath
     */
    private final DependencyList systemDependencies = new DependencyList();

    /**
     * The list of properties to copy from the apb to the test to be run
     */
    private final List<String> useProperties = new ArrayList<String>();

    //~ Methods ..............................................................................................

    public DependencyList getSystemDependencies()
    {
        return systemDependencies;
    }

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

    public List<TestReport.Builder> reports()
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

    public List<String> javaArgs()
    {
        return javaArgs;
    }

    /**
     * Method used to set the Reports to be generated when running the test.
     * @param report The Report generators to be used
     */
    public void reports(TestReport.Builder... report)
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
        final TestModuleHelper helper = getHelper();
        final String groups = helper.getProperty("tests.groups","");
        if (groups.isEmpty()) {
            helper.runTests();
        }
        else {
            helper.runTests(groups.split(","));
        }
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
        getHelper().runTests("minimal");
    }

    @NotNull @Override public TestModuleHelper getHelper()
    {
        return (TestModuleHelper) super.getHelper();
    }

    @Override public void clean()
    {
        super.clean();
        getHelper().cleanTestReports();
    }

    public Module getModuleToTest()
    {
        return moduleToTest;
    }

    protected final void systemDependencies(Dependencies... dependencyList)
    {
        systemDependencies.addAll(dependencyList);
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
}
