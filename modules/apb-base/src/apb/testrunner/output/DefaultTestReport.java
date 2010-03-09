
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//
// User: emilio
// Date: Nov 11, 2008
// Time: 6:06:24 PM

//
public abstract class DefaultTestReport
    implements TestReport
{
    //~ Instance fields ......................................................................................

    boolean     suiteOpen;
    private int coverageBlock;

    // Code coverage fields
    private int coverageClass;
    private int coverageLine;
    private int coverageMethod;

    // current Test & Suite name
    @Nullable private String currentSuite;
    @Nullable private String currentTest;
    private long             startTime;

    // Counters for suites
    private int  suitesFailed;
    private int  suitesRun;
    private long suiteStartTime;

    // Counters for the current suite

    private int suiteTestFailures;
    private int suiteTestSkipped;
    private int suiteTestsRun;

    // Cumulative Counters
    private int totalFailures;
    private int totalSkipped;
    private int totalSuites;
    private int totalTestsRun;

    //~ Methods ..............................................................................................

    @Nullable @Override public final String getCurrentSuite()
    {
        return currentSuite;
    }

    @Nullable @Override public final String getCurrentTest()
    {
        return currentTest;
    }

    public int getSuiteTestFailures()
    {
        return suiteTestFailures;
    }

    public int getSuiteTestsRun()
    {
        return suiteTestsRun;
    }

    public int getSuiteTestSkipped()
    {
        return suiteTestSkipped;
    }

    public long getSuiteTimeElapsed()
    {
        return System.currentTimeMillis() - suiteStartTime;
    }

    public long getTimeElapsed()
    {
        return System.currentTimeMillis() - startTime;
    }

    @Override public int getSuitesFailed()
    {
        return suitesFailed;
    }

    @Override public int getSuitesRun()
    {
        return suitesRun;
    }

    @Override public int getTotalSuites()
    {
        return totalSuites;
    }

    public int getTotalFailures()
    {
        return totalFailures;
    }

    public int getTotalSkipped()
    {
        return totalSkipped;
    }

    public int getTotalTestsRun()
    {
        return totalTestsRun;
    }

    @Override public void startRun(int n)
    {
        totalSuites = n;
        startTime = System.currentTimeMillis();
        coverageClass = coverageMethod = coverageBlock = coverageLine = -1;
    }

    @Override public void coverage(int clazz, int method, int block, int line)
    {
        coverageClass = clazz;
        coverageMethod = method;
        coverageBlock = block;
        coverageLine = line;
    }

    @Override public void stopRun() {}

    @Override public void startSuite(@NotNull String suiteName)
    {
        suiteOpen = true;
        currentSuite = suiteName;
        suiteTestsRun = 0;
        suiteTestFailures = 0;
        suiteStartTime = System.currentTimeMillis();
    }

    @Override public void endSuite()
    {
        if (suiteOpen) {
            suitesRun++;

            if (suiteTestFailures > 0) {
                suitesFailed++;
            }

            suiteOpen = false;
        }
    }

    @Override public void startTest(@NotNull String testName)
    {
        currentTest = testName;
    }

    @Override public void endTest()
    {
        suiteTestsRun++;
        totalTestsRun++;
    }

    @Override public void failure(@NotNull Throwable t)
    {
        suiteTestFailures++;
        totalFailures++;
    }

    @Override public void skip()
    {
        suiteTestSkipped++;
        totalSkipped++;
    }

    public int getCoverageClass()
    {
        return coverageClass;
    }

    public int getCoverageMethod()
    {
        return coverageMethod;
    }

    public int getCoverageBlock()
    {
        return coverageBlock;
    }

    public int getCoverageLine()
    {
        return coverageLine;
    }

    //~ Static fields/initializers ...........................................................................

    private static final long serialVersionUID = 2748763729187869689L;
}
