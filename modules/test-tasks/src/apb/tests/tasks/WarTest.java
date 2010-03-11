

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


package apb.tests.tasks;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import apb.tasks.FileSet;

import apb.tests.testutils.FileAssert;

import static apb.tasks.CoreTasks.mkdir;
import static apb.tasks.CoreTasks.war;

//
public class WarTest
    extends TaskTestCase
{
    //~ Instance fields ......................................................................................

    private File                classes;
    private File                webapp;
    private Map<String, String> expectedContent;

    private final String[] expectedFiles = {
            "index.html", "org/", "org/test/", "org/test/a.jsp", "WEB-INF/", "WEB-INF/web.xml",
            "WEB-INF/classes/", "WEB-INF/classes/A.class", "WEB-INF/classes/B.class",
            "WEB-INF/classes/C.class", "WEB-INF/lib/", "WEB-INF/lib/commons-util-final.jar",
        };

    //~ Methods ..............................................................................................

    public void testSimple()
        throws IOException
    {
        final String jarName = "lib/testSimple.war";

        war("$basedir/" + jarName).from(FileSet.fromDir(webapp))  //
                                  .usingBuildDirectory("war-tmp")  //
                                  .includeClasses(FileSet.fromDir(classes))
                                  .includeJars(new File(datadir, "commons-util-final.jar")).execute();

        verifyJar(jarName);
    }

    @Override protected void setUp()
        throws IOException
    {
        super.setUp();
        expectedContent = new HashMap<String, String>();

        mkdir("classes").execute();
        classes = new File(basedir, "classes");
        addFiles(classes, "A.class", "B.class", "C.class");

        mkdir("webapp").execute();
        webapp = new File(basedir, "webapp");

        addFiles(webapp, "index.html", "org/test/a.jsp");
        addWebXml(new File(webapp, "WEB-INF"));
    }

    private void verifyJar(String jarName)
        throws IOException
    {
        String expectedManifest =
            makeManifestString(  //
                               "Manifest-Version: 1.0",  //
                               "Created-By: APB",  //
                               "");

        expectedContent.put("META-INF/MANIFEST.MF", expectedManifest);

        for (String file : expectedFiles) {
            expectedContent.put(file, null);
        }

        FileAssert.assertJarContent(new File(basedir, jarName), expectedContent);
    }

    private void addWebXml(File dir)
        throws IOException
    {
        FileAssert.createFile(dir, "web.xml", WEB_XML);
    }

    private void addFiles(File dir, String... files)
        throws IOException
    {
        for (String file : files) {
            FileAssert.createFile(dir, file, "CONTENT:", file);
        }
    }

    private String makeManifestString(String... lines)
    {
        StringBuilder result = new StringBuilder();

        for (String line : lines) {
            result.append(line).append("\r\n");
        }

        return result.toString();
    }

    //~ Static fields/initializers ...........................................................................

    private static final String[] WEB_XML = {
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "<web-app>", "<web-app>"
        };
}
