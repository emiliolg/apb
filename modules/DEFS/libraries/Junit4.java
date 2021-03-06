

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


package libraries;

import apb.metadata.RemoteLibrary;

public class Junit4
    extends RemoteLibrary
{
    //~ Constructors .........................................................................................

    private Junit4()
    {
        super("junit", "junit", "4.7");
        setTargetDir("../lib");
    }

    //~ Static fields/initializers ...........................................................................

    public static final Junit4 LIB = new Junit4();
}
