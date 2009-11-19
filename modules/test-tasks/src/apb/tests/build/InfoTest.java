

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

import java.io.File;

import apb.DefinitionException;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class InfoTest
    extends ApbTestCase
{
    //~ Methods ..............................................................................................

    public void testInfo()
        throws DefinitionException
    {
        final File source = new File(dataFile("projects/DEFS/Info.java"));
        String     dir = dataFile("projects/info");
        String     outputBase = dataFile("projects/output/info");

        build("Info", "info");
        checkOutput("Helper          'Info'",  //
                    "Is top level    'true'",  //
                    "Is test module  'false'",  //
                    "Dir File        '" + dir + "'",  //
                    "Source File     '" + source.getPath() + "'",  //
                    "Project dir     '" + source.getParent() + "'",  //
                    "Output          '" + outputBase + "/classes'",  //
                    "Source          '" + dir + "/src'",  //
                    "Generated       '" + outputBase + "/gsrc'",  //
                    "Package File    '" + dataFile("projects/lib/info.jar") + "'",  //
                    "Source Package  '" + dataFile("projects/lib/info-src.jar") + "'",  //
                    "Info            'JAR'",  //
                    "--- Commands ---",  //
                    "clean: Deletes all output directories (compiled code and packages).",  //
                    "compile: Compile classes and place them in the output directory.",  //
                    "compile-tests: Compile test classes.",  //
                    "help: List the available commands with a brief description.",  //
                    "idegen:eclipse: Generate eclipse project and module files.",  //
                    "idegen:idea: Generate idea project and module files.",  //
                    "info: ",  //
                    "javadoc: Generates the Java Documentation (Javadoc) for the module.",  //
                    "module:clone: Generate a new Module based on a specified one.",  //
                    "package: Creates a jar file with the module classes and resources.",  //
                    "resources: Copy (eventually filtering) resources to the output directory.",  //
                    "run-minimal-tests: Run test with the annotation @Test(group=\"minimal\")",  //
                    "run-tests: Test the module (generating reports and  coverage info).",  //
                    "showdeps:graphviz: Output the dependency graph using graphviz",//
                    "--- Modules ---",  //
                    "hello-world",  //
                    "info",  //
                    "math",  //
                    "tests.math",  //
                    "--- Libraries ---",  //
                    "../lib/junit-3.8.2.jar",  //
                    "--- Runtime Path ---",  //
                    outputBase + "/classes",  //
                    new File(outputDir, "hello-world/classes").getPath(),  //
                    dataFile("lib/junit-3.8.2.jar"),  //
                    "");
    }
}
