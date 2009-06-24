

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

    protected final String fileName;

    @Nullable protected transient OutputHandler out;
    protected File                              reportsDir;
    protected final boolean                     showOutput;
    private OutputHandler                       outputHandler;

    //~ Constructors .........................................................................................

    public BaseTestReport(boolean showOutput, @NotNull String fileName)
    {
        this.showOutput = showOutput;
        this.fileName = fileName;
    }

    //~ Methods ..............................................................................................

    public void startRun(int n)
    {
        super.startRun(n);
        outputHandler = OutputHandler.getInstance();
    }

    public void startSuite(@NotNull String suiteName)
    {
        super.startSuite(suiteName);
        outputHandler.init(showOutput);
    }

    public void endSuite()
    {
        super.endSuite();
        outputHandler.restore();
    }

    public void stopRun()
    {
        OutputHandler.reset();
    }

    protected abstract void printOutput(String title, String content);

    protected void appendOutAndErr()
    {
        if (showOutput) {
            printOutput(SYSTEM_OUT, outputHandler.getOutput());
            printOutput(SYSTEM_ERR, outputHandler.getError());
        }
    }

    protected File reportFile(@NotNull String suffix, @NotNull String ext)
    {
        return new File(reportsDir, fileName + suffix + ext);
    }

    //~ Static fields/initializers ...........................................................................

    /** the system-err element */
    protected static final String SYSTEM_ERR = "system-err";

    /** the system-out element */
    protected static final String SYSTEM_OUT = "system-out";

    protected static final double ONE_SECOND = 1000.0;

    private static final long serialVersionUID = 2571953931139067568L;
}
