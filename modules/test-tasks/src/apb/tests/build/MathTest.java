

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
public class MathTest
    extends ApbTestCase
{
    //~ Methods ..............................................................................................

    public void testMath()
        throws DefinitionException
    {
        final File jarFile = new File(tmpdir, "output/lib/tests-math-1.0.jar").getAbsoluteFile();
        final File classFile = new File(tmpdir, "output/classes/math/Fraction.class").getAbsoluteFile();

        build("Math", "package");
        checkOutput(COMPILING_1_FILE,  //
                    BUILDING + jarFile.getPath());

        FileAssert.assertExists(jarFile);
        FileAssert.assertExists(classFile);

        build("Math", "package");
        checkOutput();

        build("Math", "clean");
        checkOutput(DELETING_DIRECTORY + classFile.getParentFile().getParent(),  //
                    DELETING_DIRECTORY + classFile.getParent(),  //
                    DELETING_FILE + jarFile.getPath(),  //
                    DELETING_DIRECTORY + new File(tmpdir, "output/reports").getAbsolutePath(),  //
                    DELETING_DIRECTORY + new File(tmpdir, "output/coverage").getAbsolutePath());

        FileAssert.assertDoesNotExist(jarFile);
        FileAssert.assertDoesNotExist(classFile);
    }

    public void testPlay()
        throws DefinitionException
    {
        final File jarFile1 = new File(tmpdir, "output/lib/tests-math-1.0.jar").getAbsoluteFile();
        final File jarFile2 = new File(tmpdir, "output/lib/tests-play-with-math-1.0.jar").getAbsoluteFile();
        final File srcJarFile =
            new File(tmpdir, "output/lib/tests-play-with-math-1.0-src.jar").getAbsoluteFile();
        final File classFile = new File(tmpdir, "output/classes/Play.class").getAbsoluteFile();

        build("PlayWithMath", "package");
        checkOutput(COMPILING_1_FILE,  //
                    BUILDING + jarFile1.getPath(),  //
                    COMPILING_1_FILE,  //
                    BUILDING + jarFile2.getPath(),  //
                    BUILDING + srcJarFile.getPath());

        FileAssert.assertExists(jarFile1);
        FileAssert.assertExists(jarFile2);
        FileAssert.assertExists(srcJarFile);
        FileAssert.assertExists(classFile);
    }
}
