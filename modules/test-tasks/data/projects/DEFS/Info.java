

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



import java.io.File;
import java.util.Map;

import apb.Command;
import apb.ModuleHelper;

import apb.metadata.BuildTarget;
import apb.metadata.Library;
import apb.metadata.Module;
import apb.metadata.PackageInfo;

import static apb.tasks.CoreTasks.printf;

public final class Info
    extends Module
{
    //~ Methods ..............................................................................................

    @BuildTarget public void info()
    {
        ModuleHelper h = getHelper();
        printf("Helper          '%s'\n", h.toString());
        printf("Is top level    '%b'\n", h.isTopLevel());
        printf("Is test module  '%b'\n", h.isTestModule());
        printf("JDK             '%s'\n", h.getJdkName());
        printf("LastModified    '%d'\n", h.lastModified());
        printf("Dir File        '%s'\n", h.getDirFile());
        printf("Source File     '%s'\n", h.getSourceFile());
        printf("Project dir     '%s'\n", h.getProjectDirectory());
        printf("Output          '%s'\n", h.getOutput());
        printf("Source          '%s'\n", h.getSource());
        printf("Generated       '%s'\n", h.getGeneratedSource());
        printf("Package File    '%s'\n", h.getPackageFile());
        printf("Source Package  '%s'\n", h.getSourcePackageFile());

        printf("Info            '%s'\n", h.getInfoObject("pkg", PackageInfo.class).type);

        printf("--- Commands ---\n");

        for (Map.Entry<String, Command> e : h.listCommands().entrySet()) {
            String name = e.getKey();
            String descr = e.getValue().getDescription();
            printf("%s: %s\n", name, descr);
        }

        printf("--- Modules ---\n");

        for (String m : h.listAllModules()) {
            printf("%s\n", m);
        }

        printf("--- Libraries ---\n");

        for (Library library : h.getAllLibraries()) {
            printf("%s\n", library);
        }

        printf("--- Runtime Path ---\n");

        for (File library : h.runtimePath()) {
            printf("%s\n", library);
        }
    }

    //~ Instance initializers ................................................................................

    {
        dependencies(new HelloWorld(),  //
                     compile(new Math()),  //
                     runtime(localLibrary("../lib/junit-3.8.2.jar")));
    }
}
