
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

package apb.testrunner.output;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import apb.Environment;

/**
 * The class that distribute output to different 'TestOutput' Objects
 */

public class TestReportBroadcaster
    extends DefaultTestReport
{
    //~ Instance fields ......................................................................................

    @NotNull private final List<TestReport> reports;

    //~ Constructors .........................................................................................

    public TestReportBroadcaster(@NotNull List<TestReport> reports)
    {
        this.reports = reports;
    }

    public TestReportBroadcaster(@NotNull Environment env, @NotNull List<Builder> builders)
    {
        reports = new ArrayList<TestReport>(builders.size());

        for (Builder builder : builders) {
            reports.add(builder.build(env));
        }
    }

    //~ Methods ..............................................................................................

    @Override public void startRun(int n)
    {
        super.startRun(n);

        for (TestReport report : reports) {
            report.startRun(n);
        }
    }

    @Override public void stopRun()
    {
        super.stopRun();

        for (TestReport report : reports) {
            report.stopRun();
        }
    }

    @Override public void coverage(int clazz, int method, int block, int line)
    {
        super.coverage(clazz, method, block, line);

        for (TestReport report : reports) {
            report.coverage(clazz, method, block, line);
        }
    }

    @NotNull @Override public TestReport init(@NotNull File reportsDir)
    {
        List<TestReport> reportList = new ArrayList<TestReport>();

        for (TestReport report : reports) {
            reportList.add(report.init(reportsDir));
        }

        return new TestReportBroadcaster(reportList);
    }

    @Override public void startSuite(@NotNull String suiteName)
    {
        super.startSuite(suiteName);

        for (TestReport report : reports) {
            report.startSuite(suiteName);
        }
    }

    @Override public void endSuite()
    {
        super.endSuite();

        for (TestReport report : reports) {
            report.endSuite();
        }
    }

    @Override public void startTest(@NotNull String testName)
    {
        super.startTest(testName);

        for (TestReport report : reports) {
            report.startTest(testName);
        }
    }

    @Override public void endTest()
    {
        super.endTest();

        for (TestReport report : reports) {
            report.endTest();
        }
    }

    @Override public void skip()
    {
        super.skip();

        for (TestReport report : reports) {
            report.skip();
        }
    }

    @Override public void failure(@NotNull Throwable t)
    {
        super.failure(t);

        for (TestReport report : reports) {
            report.failure(t);
        }
    }

    //~ Static fields/initializers ...........................................................................

    private static final long serialVersionUID = -7676644903283190123L;
}
