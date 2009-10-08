

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

import apb.StandaloneLogger;
//
// User: emilio
// Date: Sep 22, 2009
// Time: 3:51:31 PM

//
public class TestStandaloneLogger
    extends StandaloneLogger
{
    //~ Instance fields ......................................................................................

    protected List<String> output;

    //~ Constructors .........................................................................................

    public TestStandaloneLogger(List<String> output)
    {
        this.output = output;
        setColor(false);
    }

    //~ Methods ..............................................................................................

    @Override public void log(Level level, String msg, Object... args)
    {
        output.add(format(args == null || args.length == 0 ? msg : String.format(msg, args)));
    }
}
