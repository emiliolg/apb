

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



import apb.metadata.BuildTarget;
import apb.metadata.Module;

import static apb.tasks.CoreTasks.printf;

public final class HelloWorld
    extends Module
{
    //~ Methods ..............................................................................................

    @BuildTarget(description = "Greetings from APB")
    public void hello()
    {
        printf("Hello World !\n");
    }

    @BuildTarget(
                 depends = "hello",
                 description = "Good Bye from APB"
                )
    public void bye()
    {
        printf("Good Bye World !\n");
    }
}
