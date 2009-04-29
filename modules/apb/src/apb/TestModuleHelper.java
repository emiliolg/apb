
// ...........................................................................................................
// Copyright (c) 1993, 2009, Oracle and/or its affiliates. All rights reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Oracle Corp.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// Last changed on 2009-04-24 17:02:14 (-0300), by: emilio. $Revision$
// ...........................................................................................................

package apb;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import apb.metadata.CoverageInfo;
import apb.metadata.ProjectElement;
import apb.metadata.TestModule;

import apb.testrunner.output.TestReport;

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Dec 2, 2008
// Time: 5:26:23 PM

//
public class TestModuleHelper
    extends ModuleHelper
{
    //~ Instance fields ......................................................................................

    @NotNull private File coverageDir;

    private ModuleHelper moduleToTest;

    @NotNull private File reportsDir;
    @NotNull private File workingDirectory;

    //~ Constructors .........................................................................................

    TestModuleHelper(@NotNull TestModule module, @NotNull Environment env)
    {
        super(module, env);
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

    public void setModuleToTest(ModuleHelper module)
    {
        moduleToTest = module;
    }

    @NotNull public ModuleHelper getModuleToTest()
    {
        if (moduleToTest == null) {
            throw new IllegalStateException("'moduleToTest' not initialized");
        }

        return moduleToTest;
    }

    @NotNull public File getWorkingDirectory()
    {
        return workingDirectory;
    }

    public List<TestReport> getReports()
    {
        return getModule().reports();
    }

    @NotNull public File getReportsDir()
    {
        return reportsDir;
    }

    @NotNull public File getCoverageDir()
    {
        return coverageDir;
    }

    @NotNull public List<File> getClassesToTest()
    {
        if (moduleToTest == null) {
            return Collections.emptyList();
        }
        else {
            return Collections.singletonList(moduleToTest.getOutput());
        }
    }

    public boolean isCoverageEnabled()
    {
        return getCoverageInfo().enable;
    }

    public List<File> getSourcesToTest()
    {
        return moduleToTest.getSourceDirs();
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

    void activate(@NotNull ProjectElement activatedTestModule)
    {
        super.activate(activatedTestModule);
        TestModule m = getModule();
        workingDirectory = env.fileFromBase(m.workingDirectory);
        reportsDir = env.fileFromBase(m.reportsDir);
        coverageDir = env.fileFromBase(m.coverage.output);
    }
}
