

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import apb.Apb;
import apb.BuildException;
import apb.Environment;
import apb.Proxy;

import apb.coverage.CoverageBuilder;

import apb.metadata.CoverageInfo;
import apb.metadata.TestModule;

import apb.tasks.JavaTask;

import apb.testrunner.output.TestReport;

import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.io.File.pathSeparator;
import static java.lang.Character.isLowerCase;
import static java.util.Arrays.asList;

import static apb.tasks.CoreTasks.java;

import static apb.testrunner.TestRunner.listTests;
import static apb.testrunner.TestRunner.worseResult;

import static apb.utils.FileUtils.makePath;
import static apb.utils.FileUtils.makePathFromStrings;
import static apb.utils.StringUtils.isNotEmpty;
import static apb.utils.StringUtils.makeString;
//
// User: emilio
// Date: Nov 5, 2008
// Time: 5:37:49 PM

//
public class TestLauncher
{
    //~ Instance fields ......................................................................................

    /**
     * Run the tests in an independent (fork'ed) process
     */
    boolean fork;

    /**
     * Run EACH test suite in an independent (fork'ed) process
     */
    boolean         forkPerSuite;
    private boolean classPathInSystemClassloader;

    /**
     * Enable assertions when running the tests
     */
    private boolean enableAssertions;

    /**
     * Enable the java debugger in the forked process
     */
    private boolean enableDebugger;

    /**
     * Fail if no tests are found
     */
    private boolean failIfEmpty;

    /**
     * Fail the build when a test fails
     */
    private boolean                        failOnError;
    @NotNull private final CoverageBuilder coverageBuilder;
    @NotNull private final CoverageInfo    coverage;
    @NotNull private final Environment     env;
    @Nullable private File                 reportDir;
    @NotNull private final File            testClasses;
    private final int                      maxMemory;

    /**
     * The list of tests to exclude
     */
    @NotNull private List<String> excludes;

    /**
     * The list of tests to include
     */
    @NotNull private List<String> includes;

    /**
     * The list of test groups to execute
     */
    @NotNull private final List<String> testGroups;
    private final Map<String, String>   environmentVariables;

    /**
     * List of System properties to pass to the tests.
     */
    @NotNull private Map<String, String> properties;

    /**
     * The classes to be tested
     */
    @NotNull private final Set<File> classesToTest;

    /**
     * The classpath for the tests
     */
    @NotNull private Set<File> classPath;
    private final String       creatorClass;

    /**
     * To be able to run a simple TestCase
     */
    private String       singleTest;
    private final String workingDirectory;

    @NotNull private final TestReport report;

    //~ Constructors .........................................................................................

    public TestLauncher(final TestModule testModule, @NotNull final File testClasses, List<String> testGroups,
                        final TestReport reports, File reportsDir, Collection<File> classPath,
                        Collection<File> classesToTest, final CoverageBuilder coverageBuilder)
    {
        env = Apb.getEnv();
        this.classPath = new LinkedHashSet<File>(classPath);
        this.classesToTest = new LinkedHashSet<File>(classesToTest);
        this.classPath.removeAll(classesToTest);

        reportDir = reportsDir;
        report = reports;

        coverage = testModule.coverage;
        fork = testModule.fork;
        forkPerSuite = testModule.forkPerSuite;
        failIfEmpty = testModule.failIfEmpty;
        failOnError = testModule.failOnError;
        this.testClasses = testClasses;

        if (isNotEmpty(testModule.runOnly)) {
            setSingleTest(testModule.runOnly);
        }
        else if (testModule.includes().isEmpty()) {
            includes = TestModule.DEFAULT_INCLUDES;
            testModule.excludes().addAll(TestModule.DEFAULT_EXCLUDES);
            excludes = testModule.excludes();
        }
        else {
            includes = testModule.includes();
            excludes = testModule.excludes();
        }

        this.testGroups = testGroups;

        properties = expandProperties(env, testModule.properties(), testModule.useProperties());
        enableAssertions = testModule.enableAssertions;

        enableDebugger = testModule.enableDebugger;

        if (forkPerSuite || enableDebugger || isCoverageEnabled()) {
            fork = true;
        }

        classPathInSystemClassloader = testModule.classPathInSystemClassloader;
        this.coverageBuilder = coverageBuilder;
        this.coverageBuilder.setEnabled(isCoverageEnabled());
        maxMemory = testModule.memory;
        environmentVariables = testModule.environment();
        workingDirectory = testModule.workingDirectory;
        creatorClass = testModule.testType.creatorClass(testModule.customCreator);
    }

    //~ Methods ..............................................................................................

    public void execute()
    {
        if (env.isVerbose()) {
            showClassPath();
            showProperties();
        }

        int result = fork ? executeOutOfProcess() : executeInProcess();

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

            if (reportDir != null && reportDir.exists()) {
                env.logWarning("                 Check: %s\n", reportDir);
            }
        }
    }

    private static Map<String, String> expandProperties(final Environment         env,
                                                        final Map<String, String> properties,
                                                        List<String>              useProperties)
    {
        Map<String, String> result = new HashMap<String, String>();

        for (Map.Entry<String, String> e : properties.entrySet()) {
            result.put(e.getKey(), env.expand(e.getValue()));
        }

        List<String> inherit = new ArrayList<String>(useProperties);
        inherit.addAll(asList(Proxy.PROXY_PROPERTIES));

        for (String prop : inherit) {
            if (env.hasProperty(prop)) {
                result.put(prop, env.getProperty(prop));
            }
        }

        return result;
    }

    /**
     * Set to execute specific tests
     *
     * @param tests The tests to be executed
     */
    private void setTests(String... tests)
    {
        excludes = Collections.emptyList();
        includes = new ArrayList<String>();

        for (String t : tests) {
            if (isNotEmpty(t)) {
                t = t.replace('.', '/');
                includes.add("**/" + t + ".class");
            }
        }
    }

    private void setSingleTest(String s)
    {
        final int dot = s.lastIndexOf('.');

        if (dot != -1 && isLowerCase(s.charAt(dot + 1))) {
            singleTest = s.substring(dot + 1);
            s = s.substring(0, dot);
        }

        setTests(s);
    }

    private void showClassPath()
    {
        env.logVerbose("Test Classpath: \n");

        for (File file : classPath) {
            env.logVerbose("                %s\n", file);
        }
    }

    private void showProperties()
    {
        env.logVerbose("Properties: \n");

        for (Map.Entry<String, String> e : properties.entrySet()) {
            env.logVerbose("         %s='%s'\n", e.getKey(), e.getValue());
        }
    }

    private boolean isCoverageEnabled()
    {
        return coverage.enable && !enableDebugger;
    }

    @NotNull private Invocation testCreator()
        throws Exception
    {
        return new Invocation(creatorClass);
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
        File reportSpecsFile = null;

        try {
            reportSpecsFile = reportSpecs();
            return forkPerSuite
                   ? executeEachSuite(reportSpecsFile)
                   : invokeRunner(testCreator(), reportSpecsFile, null, makeString(testGroups, ':'));
        }
        catch (Exception e) {
            throw new BuildException(e);
        }
        finally {
            if (reportSpecsFile != null) {
                reportSpecsFile.delete();
            }

            coverageBuilder.stopRun();
        }
    }

    private int executeEachSuite(@NotNull File reportSpecsFile)
        throws Exception
    {
        List<File> cp = new ArrayList<File>(classPath);
        cp.add(testClasses);
        ClassLoader cl = createClassLoader(cp);

        final Invocation  creator = testCreator();
        final Set<String> tests = listTests(cl, creator, testClasses, includes, excludes, singleTest);
        report.startRun(tests.size());

        int result = TestRunner.OK;

        for (String testSet : tests) {
            result =
                worseResult(result,
                            invokeRunner(creator, reportSpecsFile, testSet, makeString(testGroups, ':')));
        }

        report.stopRun();
        return result;
    }

    private int invokeRunner(@NotNull Invocation creator, @NotNull File reportSpecsFile,
                             @Nullable String suite, @NotNull String groups)
    {
        // Create Arguments for Java Command
        List<String> args = new ArrayList<String>();

        args.addAll(coverageBuilder.addCommandLineArguments());

        // testrunner arguments
        if (env.isVerbose()) {
            args.add("-v");
        }

        if (failIfEmpty) {
            args.add("-f");
        }

        if (!isCoverageEnabled()) {
            args.add("-c");
            args.add(makePath(classPath) + pathSeparator + makePath(classesToTest));
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

        if (isNotEmpty(singleTest)) {
            args.add("--single-test");
            args.add(singleTest);
        }

        if (!groups.isEmpty()) {
            args.add("-g");
            args.add(groups);
        }

        File r = reportDir;

        if (r != null) {
            args.add("-o");
            args.add(r.getPath());
        }

        args.add("--creator");
        args.add(creator.toString());

        args.add("--report-specs-file");
        args.add(reportSpecsFile.getAbsolutePath());

        // Add the test directory
        args.add(testClasses.getAbsolutePath());
        escapeDollars(args);

        // Execute  the java command
        JavaTask java =
            java(coverageBuilder.runnerMainClass(), args).withClassPath(runnerClassPath())  //
                                                         .maxMemory(maxMemory)  //
                                                         .withProperties(properties)  //
                                                         .withEnvironment(environmentVariables)  //
                                                         .onDir(workingDirectory);

        if (enableDebugger) {
            for (String arg : DEBUG_ARGUMENTS) {
                java.addJavaArg(arg);
            }
        }

        java.execute();

        return java.getExitValue();
    }

    /**
     * Escape $ in args to prevent property expansion
     * @param args
     */
    private void escapeDollars(List<String> args)
    {
        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i);
            args.set(i, arg.replace("$", "\\$"));
        }
    }

    private String runnerClassPath()
    {
        final File appJar = Apb.applicationJarFile();

        List<File> path = new ArrayList<File>();

        if (isCoverageEnabled()) {
            path.add(new File(appJar.getParent(), "emma.jar"));
            path.addAll(classPath);
        }
        else if (classPathInSystemClassloader) {
            path.addAll(classPath);
        }
        else {
            path.add(appJar);
        }

        path.addAll(env.getExtClassPath());

        return makePath(path);
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
        Properties originalSystemProperties = null;

        if (!properties.isEmpty()) {
            originalSystemProperties = (Properties) System.getProperties().clone();

            // Add all system properties configured by the user
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                System.setProperty(entry.getKey(), entry.getKey());
            }
        }

        try {
            TestRunner        runner = new TestRunner(testClasses, reportDir, includes, excludes, testGroups);
            final ClassLoader loader = createClassLoader(classPath);
            runner.run(testCreator(), report, loader, singleTest);
        }
        catch (Exception e) {
            env.handle(e);
        }
        finally {
            if (originalSystemProperties != null) {
                System.setProperties(originalSystemProperties);
            }
        }

        return TestRunner.ERROR;
    }

    //~ Static fields/initializers ...........................................................................

    private static final List<String> DEBUG_ARGUMENTS =
        Arrays.asList("-Xdebug", "-Xnoagent",
                      "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005");

    private static final String FAILED_TESTS = "Some tests have failed.\n";
}
