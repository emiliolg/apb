

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

//
public class TestSetFailedException
    extends Exception
{
    //~ Constructors .........................................................................................

    public TestSetFailedException(Throwable cause)
    {
        super(cause);
    }

    public TestSetFailedException(String message)
    {
        super(message);
    }

    //~ Static fields/initializers ...........................................................................

    private static final long serialVersionUID = -3419700222353272199L;
}
