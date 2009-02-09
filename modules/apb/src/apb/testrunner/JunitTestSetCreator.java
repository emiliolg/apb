
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

import junit.framework.Test;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//
// User: emilio
// Date: Nov 10, 2008
// Time: 3:05:51 PM

//
public class JunitTestSetCreator
    implements TestSetCreator<junit.framework.Test>
{
    //~ Methods ..............................................................................................

    @Nullable public TestSet<Test> createTestSet(@NotNull Class<Test> testClass)
        throws TestSetFailedException
    {
        return Test.class.isAssignableFrom(testClass) ? new JUnitTestSet(testClass) : null;
    }

    @NotNull public Class<Test> getTestClass()
    {
        return Test.class;
    }

    @NotNull public String getName()
    {
        return "junit";
    }
}
