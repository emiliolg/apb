
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
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import apb.Environment;
import apb.utils.StringUtils;
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

    private final boolean showDetail;

    //~ Constructors .........................................................................................

    private SimpleReport(boolean showOutput, boolean showDetail, @NotNull String fileName)
    {
        super(showOutput, fileName);
        this.showDetail = showDetail;
    }

    //~ Methods ..............................................................................................

    @Override public void startSuite(@NotNull String suiteName)
    {
        super.startSuite(suiteName);

        if (showDetail) {
            output.printf("Suite (%2d/%2d): %-50s ", getSuitesRun() + 1, getTotalSuites(), suiteName);
        }
    }

    @Override public void startRun(int n)
    {
        super.startRun(n);

        if (!showDetail) {
            output.printf("Test Execution: ");
        }
    }

    @Override public void stopRun()
    {
        super.stopRun();

        if (!showDetail) {
            output.printf("%d suites and %d tests run (%d skipped) in %.3f seconds.", getSuitesRun(),
                          getTotalTestsRun(), getTotalSkipped(), getTimeElapsed() / ONE_SECOND);
            printFailures(getTotalFailures());
        }

        output.flush();

        if (!fileName.isEmpty()) {
            output.close();
        }
    }

    @Override public void endSuite()
    {
        if (suiteOpen) {
            super.endSuite();

            if (showDetail) {
                output.printf("%5d tests run in %6.3f seconds.", getSuiteTestsRun(),
                              getSuiteTimeElapsed() / ONE_SECOND);
                printFailures(getSuiteTestFailures());
            }

            if (showOutput) {
                appendOutAndErr();
            }
        }
    }

    @Override public void startTest(@NotNull String testName)
    {
        super.startTest(testName);
    }

    @Override public synchronized void failure(@NotNull Throwable t)
    {
        super.failure(t);

        getFailures().add(new Failure(getCurrentTest(), t));
    }

    @NotNull @Override public SimpleReport init(@NotNull File dir)
    {
        SimpleReport result = new SimpleReport(showOutput, showDetail, fileName);
        result.initOutput(dir);
        return result;
    }

    @Override protected void printOutput(String title, String content)
    {
        if (content != null && content.length() > 0) {
            output.println();
            printTitle(title.equals(SYSTEM_ERR) ? "Standard Error" : "Standard Output");
            output.print(content);
            printSeparator();
        }
    }

    @NotNull private List<Failure> getFailures()
    {
        List<Failure> result = failures;

        if (result == null) {
            failures = result = new ArrayList<Failure>();
        }

        return result;
    }

    private void initOutput(File dir)
    {
        reportsDir = dir;

        if (fileName.isEmpty()) {
            output = new PrintWriter(System.out, true);
        }
        else {
            try {
                output = new PrintWriter(reportFile("", ".txt"));
            }
            catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void printFailures(int f)
    {
        if (f > 0) {
            output.printf(" %5d failures.", f);
        }

        output.println();

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

    private static final int LINE_LENGTH = 120;

    //~ Inner Classes ........................................................................................

    public static class Builder
        implements TestReport.Builder
    {
        @NotNull private String   output;
        private final boolean     showDetail;
        @Nullable private Boolean showOutput;

        public Builder(boolean showDetail)
        {
            this.showDetail = showDetail;
            output = "";
        }

        public Builder showOutput(boolean b)
        {
            showOutput = b;
            return this;
        }

        @NotNull @Override public TestReport build(@NotNull Environment env)
        {
            boolean show =
                showOutput == null ? env.getBooleanProperty(SHOW_OUTPUT_PROPERTY, false) : showOutput;
            return new SimpleReport(show, showDetail, output);
        }

        public Builder to(@NotNull String outputFileName)
        {
            output = outputFileName;
            return this;
        }
    }

    private static class Failure
    {
        private final Throwable cause;
        private final String    test;

        Failure(String test, Throwable cause)
        {
            this.test = test;
            this.cause = cause;
        }

        public void print(PrintWriter output)
        {
            output.println("Test: " + test + ':' + " FAILED");
            output.println(cause.getMessage());
            output.println(StringUtils.getStackTrace(cause));
            output.println();
        }
    }
}
