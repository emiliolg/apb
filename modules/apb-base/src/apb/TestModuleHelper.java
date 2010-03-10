
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

package apb;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import apb.metadata.Dependency;
import apb.metadata.DependencyList;
import apb.metadata.LocalLibrary;
import apb.metadata.Module;
import apb.metadata.TestModule;
import apb.tasks.CoreTasks;
import apb.tasks.FileSet;
import apb.testrunner.TestLauncher;
import apb.testrunner.output.TestReport;
import apb.testrunner.output.TestReportBroadcaster;

import static java.util.Arrays.asList;

import static apb.testrunner.TestLauncher.instatiateCreator;
//
// User: emilio
// Date: Dec 2, 2008
// Time: 5:26:23 PM

/**
 * Provides additional functionality for {@link apb.metadata.TestModule} objects
 *
 */

public class TestModuleHelper
    extends ModuleHelper
{
    //~ Instance fields ......................................................................................

    @Nullable private File         coverageDir;
    @Nullable private ModuleHelper moduleToTest;
    @Nullable private File         reportsDir;
    @Nullable private File         workingDirectory;

    //~ Constructors .........................................................................................

    TestModuleHelper(ProjectBuilder pb, @NotNull TestModule module)
    {
        super(pb, module);
    }

    //~ Methods ..............................................................................................

    /**
     * Returns the TestModule associated to this helper
     */
    public TestModule getModule()
    {
        return (TestModule) super.getModule();
    }

    /**
     * Returns the Module this Module is testing
     */
    @NotNull public ModuleHelper getModuleToTest()
    {
        if (moduleToTest == null) {
            throw new IllegalStateException("'moduleToTest' not initialized");
        }

        return moduleToTest;
    }

    /**
      * Returns true if this is a test module, false otherwise
      */
    @Override public boolean isTestModule()
    {
        return true;
    }

    /**
     * Get the directory where the tests specified for this modules are going to be run
     */
    @NotNull public File getWorkingDirectory()
    {
        if (workingDirectory == null) {
            workingDirectory = fileFromBase(getModule().workingDirectory);
        }

        return workingDirectory;
    }

    /**
     * Get the directory where the tests reports must be written
     */
    @NotNull public File getReportsDir()
    {
        if (reportsDir == null) {
            reportsDir = fileFromBase(getModule().reportsDir);
        }

        return reportsDir;
    }

    /**
     * Get the directory where the coverage reports must be written
     */
    @NotNull public File getCoverageDir()
    {
        if (coverageDir == null) {
            coverageDir = fileFromBase(getModule().coverage.output);
        }

        return coverageDir;
    }

    /**
     * Get the set of classes to test.
     * This classes are the one that will be considered to define test coverage.
     */
    @NotNull public List<File> getClassesToTest()
    {
        final List<ModuleHelper> helpers = getModulesToTest();
        final List<File>         result = new ArrayList<File>(helpers.size());

        for (ModuleHelper helper : helpers) {
            result.add(helper.getOutput());
        }

        return Collections.unmodifiableList(result);
    }

    /**
     * Returns true if coverage is enabled, false otherwise
     */
    public boolean isCoverageEnabled()
    {
        return getModule().coverage.enable;
    }

    /**
     * Get the directories where sources associated to the classes to test are located.
     */
    public List<File> getSourcesToTest()
    {
        final List<ModuleHelper> helpers = getModulesToTest();
        final List<File>         result = new ArrayList<File>(helpers.size());

        for (ModuleHelper helper : helpers) {
            result.addAll(helper.getSourceDirs());
        }

        return Collections.unmodifiableList(result);
    }

    /**
     * Run all the tests for the specified groups
     * If no groups are specified, all tests will be run
     */
    public void runTests(String... groups)
    {
        if (groups.length > 0) {
            putProperty("tests.groups", groups[0]);
        }

        final List<String> testGroups;

        if (groups.length != 0) {
            testGroups = asList(groups);
        }
        else {
            // Check test.groups property to override the value specified in the module
            // Todo This must be removed once override is enable for List<String> properties !
            //
            final String gs = getProperty("tests.groups", "");

            if (gs.isEmpty()) {
                testGroups = getModule().groups();
            }
            else {
                testGroups = asList(gs.split(","));
            }
        }

        TestLauncher testLauncher =
            new TestLauncher(getModule(), getOutput(), testGroups, buildReports(), deepClassPath(true, false),
                             getClassesToTest(), this);
        testLauncher.setReportDir(getReportsDir());
        testLauncher.addSystemClassPath(deepClassPath(getModule().getSystemDependencies(), true));
        testLauncher.execute();
    }

    /**
     * Clean all generated tests reports
     */
    public void cleanTestReports()
    {
        CoreTasks.delete(FileSet.fromDir(getReportsDir()), FileSet.fromDir(getCoverageDir())).execute();
    }

    public String getTestCreator()
    {
        final TestModule m = getModule();
        return m.testType.creatorClass(m.customCreator);
    }

    @Override public Iterable<Dependency> getDirectDependencies()
    {
        List<Dependency> result = new ArrayList<Dependency>(getModule().dependencies());
        result.add(testFrameworkLibrary());
        return result;
    }

    public void setModuleToTest(@NotNull ModuleHelper moduleToTest)
    {
        this.moduleToTest = moduleToTest;
        final DependencyList deps = getModule().dependencies();
        final Module         m = moduleToTest.getModule();
        deps.add(m);
        deps.addAll(m.dependencies());
    }

    @NotNull private List<ModuleHelper> getModulesToTest()
    {
        final List<ModuleHelper> result = new ArrayList<ModuleHelper>();

        final ModuleHelper moduleToTest = getModuleToTest();
        result.add(moduleToTest);

        for (Module module : moduleToTest.modulesToPackage()) {
            result.add(module.getHelper());
        }

        return Collections.unmodifiableList(result);
    }

    private LocalLibrary testFrameworkLibrary()
    {
        String jar =
            instatiateCreator(getTestCreator(), getClass().getClassLoader()).getTestFrameworkJar().getPath();

        LocalLibrary l = new LocalLibrary(jar);

        // Set sources if possible
        if (jar.endsWith(".jar")) {
            l.setSources(jar.substring(0, jar.length() - ".jar".length()) + "-sources.jar");
        }

        return l;
    }

    private TestReport buildReports()
    {
        List<TestReport.Builder> testReports = getModule().reports();

        return testReports.isEmpty()
               ? TestReport.SIMPLE.build(this)
               : testReports.size() == 1 ? testReports.get(0).build(this)
                                         : new TestReportBroadcaster(this, testReports);
    }
}
