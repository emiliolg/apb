
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

public final class Apb
    extends Module
{
    //~ Instance initializers ................................................................................

    {
        description = "APB Project Builder";
        dependencies(localLibrary("../lib/annotations.jar"), localLibrary("../lib/junit3.jar"));

        pkg.mainClass = "apb.Main";
        pkg.dir = "../lib";
        pkg.name = "apb";
        pkg.addClassPath = true;
        pkg.generateSourcesJar = true;

        compiler.lint = true;

        javadoc.deprecatedList = false;
        javadoc.links("http://java.sun.com/javase/6/docs/api");
    }
}
