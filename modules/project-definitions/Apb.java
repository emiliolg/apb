

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



import apb.metadata.Module;

import libraries.Junit3;

public final class Apb
    extends DefaultModule
{
    //~ Static fields/initializers ...........................................................................

    public static final Module MODULE = new Apb();

    //~ Instance initializers ................................................................................

    {
        description = "APB Project Builder";
        dependencies(Junit3.LIB, ApbTest.MODULE);

        pkg.mainClass = "apb.Main";
        pkg.name = "apb";
        pkg.addClassPath = true;
        pkg.generateSourcesJar = true;
        pkg.services("apb.Command", "apb.commands.idegen.Idea", "apb.commands.idegen.Eclipse");
        pkg.services("apb.Command", "apb.commands.module.Clone");
        pkg.services("apb.testrunner.TestSetCreator", "apb.testrunner.JunitTestSetCreator");

        javadoc.deprecatedList = false;
        javadoc.links("http://java.sun.com/javase/6/docs/api");

        compiler.warnExcludes("apb/utils/StringUtils.java");

        tests(TestTasks.MODULE);
    }
}
