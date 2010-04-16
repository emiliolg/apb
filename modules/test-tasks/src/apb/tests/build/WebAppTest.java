

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
public class WebAppTest
    extends ApbTestCase
{
    //~ Methods ..............................................................................................

    public void testSimpleWebApp()
        throws DefinitionException
    {
        File webAppOutput = new File(outputDir, "simple-web-app/tests-simple-web-app-1.0");
        File war = new File(outputDir, "lib/tests-simple-web-app-1.0.war");

        build("SimpleWebApp", "package");
        checkOutput(SKIPPING_EMPTY_DIR + dataFile("projects/simple-web-app/src"),  //
                    COPYING_FILES_TO(2, webAppOutput),  //
                    BUILDING + war.getPath());

        FileAssert.assertExists(war);

        build("SimpleWebApp", "package");
        checkOutput(SKIPPING_EMPTY_DIR + dataFile("projects/simple-web-app/src"));

        build("SimpleWebApp", "clean");
        checkOutput(DELETING_FILE + war.getPath(),  //
                    DELETING_DIRECTORY + webAppOutput);

        FileAssert.assertDoesNotExist(war);
        FileAssert.assertDoesNotExist(webAppOutput);
    }

    private String COPYING_FILES_TO(int n, File destDir)
    {
        return "Copying  " + n + " files\nto " + destDir.getAbsolutePath();
    }
}
