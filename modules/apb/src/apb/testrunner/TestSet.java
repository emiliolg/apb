
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

package apb.testrunner;

import apb.testrunner.output.TestReport;

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Nov 7, 2008
// Time: 10:29:25 AM

//
public abstract class TestSet<T>
{
    //~ Instance fields ......................................................................................

    private Class<T> testClass;

    //~ Constructors .........................................................................................

    protected TestSet(@NotNull Class<T> testClass)
    {
        this.testClass = testClass;
    }

    //~ Methods ..............................................................................................

    public abstract void execute(TestReport report, ClassLoader classLoader)
        throws TestSetFailedException;

    public String getName()
    {
        return testClass.getName();
    }

    public Class<T> getTestClass()
    {
        return testClass;
    }

    public void run(ClassLoader testsClassLoader, TestReport report)
        throws TestSetFailedException
    {
        try {
            try {
                report.startSuite(getName());
                execute(report, testsClassLoader);
            }
            finally {
                report.endSuite();
            }
        }
        catch (Exception t) {
            t.printStackTrace(System.err);
        }
    }
}
