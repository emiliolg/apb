
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

package apb.testrunner.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import apb.utils.StringUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//
// User: emilio
// Date: Nov 11, 2008
// Time: 5:00:38 PM

//
public class SimpleReport
    extends BaseTestReport
{
    //~ Instance fields ......................................................................................

    @Nullable private transient List<Failure> failures;
    @NotNull private transient PrintWriter    output;
    private boolean                           showDetail;

    //~ Constructors .........................................................................................

    public SimpleReport(boolean showOutput, boolean showDetail)
    {
        super(showOutput, "");
        output = new PrintWriter(System.out, true);
        this.showDetail = showDetail;
    }

    public SimpleReport(boolean showOutput, @NotNull String fileName)
    {
        super(showOutput, fileName);

        try {
            output = new PrintWriter(reportFile("", ".txt"));
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    //~ Methods ..............................................................................................

    public void startSuite(@NotNull String suiteName)
    {
        super.startSuite(suiteName);

        if (showDetail) {
            output.printf("Suite (%2d/%2d): %-50s ", getSuitesRun() + 1, getTotalSuites(), suiteName);
        }
    }

    public void startRun(int n)
    {
        super.startRun(n);

        if (!showDetail) {
            output.printf("Test Execution: ");
        }
    }

    public void stopRun()
    {
        super.stopRun();

        if (!showDetail) {
            output.printf("%d suites and %d tests run in %.3f seconds.", getSuitesRun(), getTotalTestsRun(),
                          getTimeEllapsed() / ONE_SECOND);
            printFailures(getTotalFailures());
        }
    }

    public void endSuite()
    {
        if (suiteOpen) {
            super.endSuite();

            if (showDetail) {
                output.printf("%5d tests run in %6.3f seconds.", getSuiteTestsRun(),
                              getSuiteTimeEllapsed() / ONE_SECOND);
                printFailures(getSuiteTestFailures());
            }
        }
    }

    public void startTest(@NotNull String testName)
    {
        super.startTest(testName);
    }

    public synchronized void failure(@NotNull Throwable t)
    {
        super.failure(t);
        endTest();

        final List<Failure> fs = failures == null ? (failures = new ArrayList<Failure>()) : failures;
        fs.add(new Failure(getCurrentTest(), t));
    }

    @NotNull public SimpleReport init(@NotNull File dir)
    {
        SimpleReport result =
            fileName.isEmpty() ? new SimpleReport(showOutput, showDetail)
                               : new SimpleReport(showOutput, fileName);
        result.reportsDir = dir;
        return result;
    }

    protected void printOutput(String title, String content)
    {
        if (content != null && content.length() > 0) {
            printTitle(title.equals(SYSTEM_ERR) ? "Standard Error" : "Standard Output");
            output.print(content);
            printSeparator();
        }
    }

    private void printFailures(int f)
    {
        if (f > 0) {
            output.printf(" %5d failures.", f);
        }

        output.println();
        appendOutAndErr();

        // print failures
        final List<Failure> fs = failures;

        if (fs != null && !fs.isEmpty()) {
            printTitle("Tests Failed");

            for (Failure failure : fs) {
                failure.print(output);
            }

            fs.clear();
            printSeparator();
        }
    }

    private void printSeparator()
    {
        output.println(StringUtils.nChars(LINE_LENGTH, '-'));
    }

    private void printTitle(String title)
    {
        int n = (LINE_LENGTH - 2 - title.length()) / 2;
        output.print(StringUtils.nChars(n, '-'));
        output.print(' ');
        output.print(title);
        output.print(' ');
        output.println(StringUtils.nChars(n, '-'));
    }

    //~ Static fields/initializers ...........................................................................

    private static final long serialVersionUID = 3848922421378433745L;

    private static final int LINE_LENGTH = 80;

    //~ Inner Classes ........................................................................................

    private static class Failure
    {
        private Throwable cause;
        private String    test;

        public Failure(String test, Throwable cause)
        {
            this.test = test;
            this.cause = cause;
        }

        public void print(PrintWriter output)
        {
            output.println("Test: " + test + ":" + " FAILED");
            output.println(cause.getMessage());
            output.println(StringUtils.getStackTrace(cause));
            output.println();
        }
    }
}
