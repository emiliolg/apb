

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


package apb.testrunner;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestListener;
import junit.framework.TestResult;

class TestResultWrapper
    extends TestResult
{
    //~ Instance fields ......................................................................................

    private final List<JUnit3TestSet.TestListenerAdaptor> listeners =
        new ArrayList<JUnit3TestSet.TestListenerAdaptor>();

    //~ Methods ..............................................................................................

    @Override public void addListener(TestListener testListener)
    {
        super.addListener(testListener);

        if (testListener instanceof JUnit3TestSet.TestListenerAdaptor) {
            listeners.add((JUnit3TestSet.TestListenerAdaptor) testListener);
        }
    }

    public void addSkipped()
    {
        for (JUnit3TestSet.TestListenerAdaptor listener : listeners) {
            listener.addSkipped();
        }
    }
}
