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
import libraries.IdeaAnnotations;
import com.sun.tools.javac.resources.compiler;

public final class ApbInstaller
        extends DefaultModule {
    //~ Instance initializers ................................................................................

    {
        description = "APB Installer";

        resources.dir = "../";
        resources.includes("README.textile", //
                           "lib/apb.jar", "lib/junit.jar", "lib/emma.jar", "lib/apb-src.jar", //
                           "antlib/ant-apb.jar",
                           "bin/completion/apb-complete.sh");
        resources.output = "$output/apb-$version";

        pkg.mainClass = "apb.installer.Main";
        pkg.name = "apb-${version}-installer";
        pkg.addClassPath = true;

        compiler.source = "1.4";
    }
}