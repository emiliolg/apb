

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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import apb.metadata.CoverageInfo;
import apb.metadata.Module;
import apb.metadata.TestModule;

import apb.testrunner.output.TestReport;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//
// User: emilio
// Date: Dec 2, 2008
// Time: 5:26:23 PM

//
public class TestModuleHelper
    extends ModuleHelper
{
    //~ Instance fields ......................................................................................

    @Nullable private File coverageDir;
    @Nullable private File reportsDir;
    @Nullable private File workingDirectory;

    //~ Constructors .........................................................................................

    TestModuleHelper(ProjectBuilder pb, @NotNull TestModule module)
    {
        super(pb, module);
    }

    //~ Methods ..............................................................................................

    public TestModule getModule()
    {
        return (TestModule) super.getModule();
    }

    public CoverageInfo getCoverageInfo()
    {
        return getModule().coverage;
    }

    @NotNull public ModuleHelper getModuleToTest()
    {
        Module m = getModule().getModuleToTest();

        if (m == null) {
            throw new IllegalStateException("'moduleToTest' not initialized");
        }

        return m.getHelper();
    }

    @Override public boolean isTestModule()
    {
        return true;
    }

    @NotNull public File getWorkingDirectory()
    {
        if (workingDirectory == null) {
            workingDirectory = fileFromBase(getModule().workingDirectory);
        }

        return workingDirectory;
    }

    public List<TestReport> getReports()
    {
        return getModule().reports();
    }

    @NotNull public File getReportsDir()
    {
        if (reportsDir == null) {
            reportsDir = fileFromBase(getModule().reportsDir);
        }

        return reportsDir;
    }

    @NotNull public File getCoverageDir()
    {
        if (coverageDir == null) {
            coverageDir = fileFromBase(getModule().coverage.output);
        }

        return coverageDir;
    }

    @NotNull public List<File> getClassesToTest()
    {
        return Collections.singletonList(getModuleToTest().getOutput());
    }

    public boolean isCoverageEnabled()
    {
        return getCoverageInfo().enable;
    }

    public List<File> getSourcesToTest()
    {
        return getModuleToTest().getSourceDirs();
    }

    public int getMemory()
    {
        return getModule().memory;
    }

    public Map<String, String> getEnvironmentVariables()
    {
        return getModule().environment();
    }

    public String getCreatorClass()
    {
        final TestModule m = getModule();
        return m.testType.creatorClass(m.customCreator);
    }
}
