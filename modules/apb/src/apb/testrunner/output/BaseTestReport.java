
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//
// User: emilio
// Date: Nov 14, 2008
// Time: 5:24:49 PM

//
public abstract class BaseTestReport
    extends DefaultTestReport
{
    //~ Instance fields ......................................................................................

    @Nullable protected transient SimpleReport.OutputHandler err, out;

    protected final String  fileName;
    protected File          reportsDir;
    protected final boolean showOutput;

    //~ Constructors .........................................................................................

    public BaseTestReport(boolean showOutput, @NotNull String fileName)
    {
        this.showOutput = showOutput;
        this.fileName = fileName;
    }

    //~ Methods ..............................................................................................

    public void startSuite(@NotNull String suiteName)
    {
        super.startSuite(suiteName);
        setupOutAndErr();
    }

    public void endSuite()
    {
        super.endSuite();
        restoreOutAndErr();
    }

    protected abstract void printOutput(String title, String content);

    protected void setupOutAndErr()
    {
        final OutputHandler o = new OutputHandler(false);
        final OutputHandler e = new OutputHandler(true);

        if (showOutput) {
            o.outputToMemory();
            e.outputToMemory();
        }
        else {
            o.nullOutput();
            e.nullOutput();
        }

        out = o;
        err = e;
    }

    protected void appendOutAndErr()
    {
        // append the err and output streams to the log
        if (out != null) {
            printOutput(SYSTEM_OUT, out.consumeOutput());
        }

        if (err != null) {
            printOutput(SYSTEM_ERR, err.consumeOutput());
        }
    }

    protected File reporFile(@NotNull String suffix, @NotNull String ext)
    {
        return new File(reportsDir, fileName + suffix + ext);
    }

    private void restoreOutAndErr()
    {
        if (out != null) {
            out.restore();
            out = null;
        }

        if (err != null) {
            err.restore();
            err = null;
        }
    }

    //~ Static fields/initializers ...........................................................................

    /** the system-err element */
    protected static final String SYSTEM_ERR = "system-err";

    /** the system-out element */
    protected static final String SYSTEM_OUT = "system-out";

    protected static final double ONE_SECOND = 1000.0;

    private static final long serialVersionUID = 2571953931139067568L;

    //~ Inner Classes ........................................................................................

    static class OutputHandler
    {
        private ByteArrayOutputStream byteStream;
        private boolean               isErrorOutput;
        private PrintStream           prev;
        private PrintStream           printStream;

        public OutputHandler(boolean err)
        {
            isErrorOutput = err;
            prev = null;
        }

        public void outputToMemory()
        {
            byteStream = new ByteArrayOutputStream();
            push(new PrintStream(byteStream));
        }

        public void nullOutput()
        {
            push(FileUtils.nullOutputStream());
        }

        public void restore()
        {
            if (prev != null) {
                if (isErrorOutput) {
                    System.setErr(prev);
                }
                else {
                    System.setOut(prev);
                }

                prev = null;
            }
        }

        public String consumeOutput()
        {
            if (printStream != null) {
                printStream.close();
                printStream = null;
            }

            String result = byteStream == null ? "" : byteStream.toString();
            byteStream = null;
            return result;
        }

        private void push(PrintStream ps)
        {
            if (isErrorOutput) {
                prev = System.err;
                System.setErr(ps);
            }
            else {
                prev = System.out;
                System.setOut(ps);
            }

            printStream = ps;
        }
    }
}
