
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

import apb.metadata.Module;

public final class ApbAnt
    extends Module
{
    //~ Instance initializers ................................................................................

    {
        description = "APB Ant Tasks";
        dependencies(new Apb(), localLibrary("../lib/annotations.jar"), localLibrary("../lib/ant.jar"));

        pkg.dir = "../lib";
        pkg.name = "ant-apb";
        pkg.addClassPath = true;
        compiler.lint = true;
    }
}
