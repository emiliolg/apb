

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


package apb.tests.testutils;

import java.util.List;

import apb.Logger;
//
// User: emilio
// Date: Sep 22, 2009
// Time: 11:07:54 AM

//
public class TestLogger
    implements Logger
{
    //~ Instance fields ......................................................................................

    protected List<String> output;

    //~ Constructors .........................................................................................

    public TestLogger(List<String> output)
    {
        this.output = output;
    }

    //~ Methods ..............................................................................................

    public void log(Level level, String msg, Object... args)
    {
        output.add(String.format(msg, args));
    }

    public void setLevel(Level warning) {}
}
