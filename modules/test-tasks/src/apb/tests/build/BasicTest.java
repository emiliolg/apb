

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


package apb.tests.build;

import apb.DefinitionException;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class BasicTest
    extends ApbTestCase
{
    //~ Methods ..............................................................................................

    public void testHello()
        throws DefinitionException
    {
        build("HelloWorld", "hello");
        checkOutput("Hello World !");

        build("HelloWorld", "bye");
        checkOutput("Hello World !",  //
                    "Good Bye World !");
    }

    public void testChat()
        throws DefinitionException
    {
        build("Chat", "bye");
        checkOutput("Hello World !",  //
                    "How are you doing !",  //
                    "Good Bye World !");
    }
}
