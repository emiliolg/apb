

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

import apb.tests.testutils.FileAssert;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class ProjectTest
    extends ApbTestCase
{
    //~ Methods ..............................................................................................

    public void testPackage()
        throws DefinitionException
    {
        final File jarFile1 = new File(tmpFile("output/lib/tests-math-1.0.jar"));
        final File jarFile2 = new File(tmpFile("output/lib/tests-play-with-math-1.0.jar"));
        final File classFile = new File(tmpFile("output/classes/math/Fraction.class"));
        final File srcJarFile = new File(tmpFile("output/lib/tests-play-with-math-1.0-src.jar"));

        build("Samples", "package");
        checkOutput(SKIPPING_EMPTY_DIR + dataFile("projects/hello-world/src"),  //
                    COMPILING_1_FILE,  //
                    BUILDING + jarFile1.getPath(),  //
                    COMPILING_1_FILE,  //
                    BUILDING + jarFile2.getPath(),  //
                    BUILDING + srcJarFile.getPath());

        FileAssert.assertExists(jarFile1);
        FileAssert.assertExists(jarFile2);

        build("Samples", "package");
        checkOutput(SKIPPING_EMPTY_DIR + dataFile("projects/hello-world/src"));

        build("Samples", "clean");
        checkOutput(DELETING_DIRECTORY + classFile.getParentFile().getParent(),  //
                    DELETING_DIRECTORY + classFile.getParentFile(),  //
                    DELETING_FILE + jarFile1.getPath(),  //
                    DELETING_DIRECTORY + tmpFile("output/reports"),  //
                    DELETING_DIRECTORY + tmpFile("output/coverage"),  //

                    DELETING_FILE + jarFile2.getPath(),  //
                    DELETING_FILE + srcJarFile.getPath());

        FileAssert.assertDoesNotExist(jarFile1);
        FileAssert.assertDoesNotExist(jarFile2);
        FileAssert.assertDoesNotExist(classFile);
    }

    public void testJavadoc()
        throws DefinitionException
    {
        final File docDir = new File(tmpdir, "output/javadoc").getAbsoluteFile();

        build("Samples", "javadoc");
        checkOutput(SKIPPING_EMPTY_DIR + dataFile("projects/hello-world/src"),  //
                    "Generating documentation for: " + dataFile("projects/math/src"));

        FileAssert.assertExists(docDir);

        build("Samples", "javadoc");
        checkOutput(SKIPPING_EMPTY_DIR + dataFile("projects/hello-world/src"));
    }
}
