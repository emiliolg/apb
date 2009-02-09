
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

package apb.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import apb.BuildException;
import apb.Environment;
import apb.TestModuleHelper;

import apb.coverage.CoverageBuilder;

import apb.metadata.CoverageInfo;
import apb.metadata.TestModule;

import apb.testrunner.Invocation;
import apb.testrunner.TestRunner;
import apb.testrunner.output.TestReport;
import apb.testrunner.output.TestReportBroadcaster;

import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static apb.testrunner.TestRunner.listTests;
import static apb.testrunner.TestRunner.worseResult;

import static apb.utils.FileUtils.makePathFromStrings;
//
// User: emilio
// Date: Nov 5, 2008
// Time: 5:37:49 PM

//
public class TestTask
    extends Task
{
    //~ Instance fields ......................................................................................

    /**
     * Run the tests in an independent (fork'ed) process
     */
    boolean fork;

    /**
     * Run EACH test suite in an independent (fork'ed) process
     */
    boolean forkPerSuite;

    /**
     * The classes to be tested
     */
    @NotNull private final List<File> classesToTest;

    /**
     * The classpath for the tests
     */
    @NotNull private Collection<File>   classPath;
    @NotNull private final CoverageInfo coverage;

    /**
     * Enable assertions when running the tests
     */
    private boolean enableAssertions;

    /**
     * Enable the java debugger in the forked process
     */
    private boolean enableDebugger;

    /**
     * The list of tests to exclude
     */
    @NotNull private List<String> excludes;

    /**
     * Fail if no tests are found
     */
    private boolean failIfEmpty;

    /**
     * Fail the build when a test fails
     */
    private boolean failOnError;

    /**
     * The list of tests to include
     */
    @NotNull private List<String> includes;

    /**
     * The test module helper
     */
    private TestModuleHelper moduleHelper;

    /**
     * List of System properties to pass to the tests.
     */
    @NotNull private Map<String, String> properties;
    @NotNull private TestReport          report;
    @Nullable private File               reportDir;

    //~ Constructors .........................................................................................

    public TestTask(@NotNull Environment env)
    {
        super(env);
        moduleHelper = env.getTestModuleHelper();
        classPath = moduleHelper.deepClassPath(false);

        classesToTest = moduleHelper.getClassesToTest();
        classPath.removeAll(classesToTest);

        coverage = moduleHelper.getCoverageInfo();

        reportDir = moduleHelper.getReportsDir();

        setReport(moduleHelper.getReports());

        TestModule testModule = moduleHelper.getModule();
        fork = testModule.fork;
        forkPerSuite = testModule.forkPerSuite;
        failIfEmpty = testModule.failIfEmpty;
        failOnError = testModule.failOnError;

        if (testModule.includes().isEmpty()) {
            includes = TestModule.DEFAULT_INCLUDES;
            excludes = TestModule.DEFAULT_EXCLUDES;
        }
        else {
            includes = testModule.includes();
            excludes = testModule.excludes();
        }

        properties = testModule.properties();
        enableAssertions = testModule.enableAssertions;

        enableDebugger = testModule.enableDebugger;

        if (forkPerSuite || enableDebugger || coverage.enable) {
            fork = true;
        }
    }

    //~ Methods ..............................................................................................

    public static void execute(Environment env)
    {
        TestTask task = new TestTask(env);
        task.execute();
    }

    public static void cleanReports(Environment env)
    {
        TestModuleHelper helper = env.getTestModuleHelper();

        RemoveTask.remove(env, helper.getReportsDir());
        RemoveTask.remove(env, helper.getCoverageDir());
    }

    public void setReport(@NotNull List<TestReport> testReports)
    {
        if (testReports.size() == 1) {
            report = testReports.get(0);
        }
        else if (!testReports.isEmpty()) {
            report = new TestReportBroadcaster(testReports);
        }
    }

    /**
     * Set to execute specific tests
     *
     * @param tests The tests to be executed
     */
    public void setTests(String... tests)
    {
        excludes = Collections.emptyList();
        includes = new ArrayList<String>();

        for (String t : tests) {
            t = t.replace('.', '/');
            includes.add("**/" + t + ".class");
        }
    }

    public void execute()
    {
        if (env.isVerbose()) {
            env.logVerbose("Test Classpath: \n");

            for (File file : classPath) {
                env.logVerbose("                %s\n", file);
            }
        }

        Properties originalSystemProperties = processProperties(!fork);

        int result = fork ? executeOutOfProcess() : executeInProcess();

        if (originalSystemProperties != null) {
            // restore system properties, only makes sense when not forking..
            System.setProperties(originalSystemProperties);
        }

        if (result == TestRunner.NO_TESTS) {
            if (failIfEmpty) {
                throw new BuildException("No tests were executed!");
            }
        }
        else if (result != TestRunner.OK) {
            if (failOnError) {
                throw new BuildException(FAILED_TESTS);
            }

            env.logWarning(FAILED_TESTS);

            if (reportDir != null) {
                env.logWarning("                 Check: %s\n", reportDir);
            }
        }
    }

    protected Properties processProperties(boolean setInSystem)
    {
        Properties original = null;

        if (!properties.isEmpty()) {
            if (setInSystem) {
                original = (Properties) System.getProperties().clone();

                // Add all system properties configured by the user
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    System.setProperty(entry.getKey(), entry.getKey());
                }
            }

            if (env.isVerbose()) {
                env.logVerbose("Properties: \n");

                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    env.logVerbose("         %s='%s'\n", entry.getKey(), entry.getValue());
                }
            }
        }

        return original;
    }

    @NotNull private Invocation testCreator()
        throws Exception
    {
        return new Invocation(moduleHelper.getCreatorClass());
    }

    private ClassLoader createClassLoader(Collection<File> classPathUrls)
    {
        try {
            ClassLoader classLoader = new URLClassLoader(FileUtils.toURLArray(classPathUrls));
            classLoader.setDefaultAssertionStatus(enableAssertions);
            return classLoader;
        }
        catch (MalformedURLException e) {
            throw new BuildException(e);
        }
    }

    private int executeOutOfProcess()
    {
        File            reportSpecsFile = null;
        CoverageBuilder coverageBuilder = null;

        try {
            reportSpecsFile = reportSpecs();
            coverageBuilder = new CoverageBuilder(env, moduleHelper);

            return forkPerSuite ? executeEachSuite(reportSpecsFile, coverageBuilder)
                                : invokeRunner(testCreator(), reportSpecsFile, coverageBuilder, null);
        }
        catch (Exception e) {
            throw new BuildException(e);
        }
        finally {
            if (reportSpecsFile != null) {
                reportSpecsFile.delete();
            }

            if (coverageBuilder != null) {
                coverageBuilder.stopRun();
            }
        }
    }

    private int executeEachSuite(@NotNull File reportSpecsFile, @NotNull CoverageBuilder coverageBuilder)
        throws Exception
    {
        ClassLoader cl = createClassLoader(classPath);

        final Invocation  creator = testCreator();
        final Set<String> tests = listTests(cl, creator, moduleHelper.getOutput(), includes, excludes);
        report.startRun(tests.size());

        int result = TestRunner.OK;

        for (String testSet : tests) {
            result = worseResult(result, invokeRunner(creator, reportSpecsFile, coverageBuilder, testSet));
        }

        report.stopRun();
        return result;
    }

    private int invokeRunner(@NotNull Invocation creator, @NotNull File reportSpecsFile,
                             @NotNull CoverageBuilder coverageBuilder, @Nullable String suite)
    {
        // Create Java Command
        List<String> args = new ArrayList<String>();

        JavaTask java = new JavaTask(env, false, coverageBuilder.runnerMainClass(), args);

        java.setClasspath(runnerClassPath());

        args.addAll(coverageBuilder.addCommandLineArguments());

        // Memory
        java.setMemory(moduleHelper.getMemory());
        // Pass environment variables

        java.putAll(moduleHelper.getEnvironmentVariables());

        // Add properties
        java.setProperties(properties);

        if (enableDebugger) {
            for (String arg : DEBUG_ARGUMENTS) {
                java.addJavaArg(arg);
            }
        }

        java.setCurrentDirectory(moduleHelper.getWorkingDirectory());

        // testrunner arguments
        if (env.isVerbose()) {
            args.add("-v");
        }

        if (failIfEmpty) {
            args.add("-f");
        }

        if (!coverage.enable) {
            args.add("-c");
            final Collection<File> path = new ArrayList<File>(classPath);
            path.addAll(classesToTest);
            args.add(FileUtils.makePath(path));
        }

        if (suite == null) {
            args.add("-i");
            args.add(makePathFromStrings(includes));
            args.add("-e");
            args.add(makePathFromStrings(excludes));
        }
        else {
            args.add("-s");
            args.add(suite.replace('.', '/'));
        }

        args.add("--creator");
        args.add(creator.toString());

        args.add("--report-specs-file");
        args.add(reportSpecsFile.getAbsolutePath());

        // Add the test directory
        args.add(moduleHelper.getOutput().getAbsolutePath());

        // Execute
        java.execute();

        return java.getExitValue();
    }

    private String runnerClassPath()
    {
        final File appJar = env.applicationJarFile();

        List<File> path = new ArrayList<File>();

        if (coverage.enable) {
            path.add(new File(appJar.getParent(), "emma.jar"));
            path.addAll(classPath);
        }
        else {
            path.add(appJar);
        }

        return FileUtils.makePath(path);
    }

    private File reportSpecs()
    {
        try {
            File               file = File.createTempFile("reps", null);
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
            os.writeObject(report);
            os.close();
            return file;
        }
        catch (IOException e) {
            throw new BuildException("Cannot create temporary file to store report specification", e);
        }
    }

    private int executeInProcess()
    {
        try {
            TestRunner        runner =
                new TestRunner(moduleHelper.getOutput(), reportDir, includes, excludes);
            final ClassLoader loader = createClassLoader(classPath);
            runner.run(testCreator(), report, loader);
        }
        catch (Exception e) {
            env.handle(e);
        }

        return TestRunner.ERROR;
    }

    //~ Static fields/initializers ...........................................................................

    private static final List<String> DEBUG_ARGUMENTS =
        Arrays.asList("-Xdebug", "-Xnoagent",
                      "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005");

    private static final String FAILED_TESTS = "Some tests have failed.\n";
}
