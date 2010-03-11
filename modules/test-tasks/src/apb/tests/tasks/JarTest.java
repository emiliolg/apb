

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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;

import apb.tasks.FileSet;

import apb.tests.testutils.FileAssert;

import static java.util.Arrays.asList;

import static apb.tasks.CoreTasks.jar;
import static apb.tasks.CoreTasks.mkdir;

//
public class JarTest
    extends TaskTestCase
{
    //~ Instance fields ......................................................................................

    private File                classes;
    private Map<String, String> expectedContent;

    //~ Methods ..............................................................................................

    public void testSimple()
        throws IOException
    {
        final String jarName = "lib/testSimple.jar";

        jar("$basedir/" + jarName).from(classes)  //
                                  .mainClass("A.class")  //
                                  .version("1.1")  //
                                  .withClassPath("$basedir/lib2/test2.jar", "$basedir/lib3/test3.jar")
                                  .withManifestAttribute("color", "red")  //
                                  .withComment("Test jar").execute();

        verifyJar(jarName);
    }

    public void testManifest()
        throws IOException
    {
        final Map<Attributes.Name, String> mf = new HashMap<Attributes.Name, String>();
        mf.put(Attributes.Name.MAIN_CLASS, "A.class");
        mf.put(Attributes.Name.IMPLEMENTATION_VERSION, "1.1");
        mf.put(new Attributes.Name("color"), "red");

        final String jarName = "lib/testManifest.jar";
        jar("$basedir/" + jarName).from(classes)  //
                                  .withClassPath("$basedir/lib2/test2.jar", "$basedir/lib3/test3.jar")
                                  .withManifestAttributes(mf)  //
                                  .withComment("Test jar").execute();

        verifyJar(jarName);
    }

    public void testServices()
        throws IOException
    {
        final File jarFile = new File(basedir, "lib/testServices.jar");

        Map<String, Set<String>> services = new HashMap<String, Set<String>>();
        services.put("service", new HashSet<String>(asList("impl1", "impl2")));

        jar(jarFile).from(FileSet.fromDir(classes))  //
                    .withServices(services).execute();

        String expectedManifest =
            makeManifestString(  //
                               "Manifest-Version: 1.0",  //
                               "Created-By: APB",  //
                               "");
        String expectedServices = makeManifestString("impl1", "impl2");

        expectedContent.put("META-INF/", null);
        expectedContent.put("META-INF/services/", null);
        expectedContent.put("META-INF/services/service", expectedServices);
        expectedContent.put("META-INF/MANIFEST.MF", expectedManifest);
        expectedContent.put("A.class", null);
        expectedContent.put("B.class", null);
        expectedContent.put("C.class", null);

        FileAssert.assertJarContent(jarFile, expectedContent);
    }

    @Override protected void setUp()
        throws IOException
    {
        super.setUp();
        expectedContent = new HashMap<String, String>();

        mkdir("classes").execute();
        classes = new File(basedir, "classes");
        addFiles(classes, "A.class", "B.class", "C.class");
    }

    private void verifyJar(String jarName)
        throws IOException
    {
        String expectedManifest =
            makeManifestString(  //
                               "Manifest-Version: 1.0",  //
                               "Implementation-Version: 1.1",  //
                               "color: red",  //
                               "Class-Path: ../lib2/test2.jar ../lib3/test3.jar",  //
                               "Created-By: APB",  //
                               "Main-Class: A.class",  //
                               "");

        expectedContent.put("META-INF/MANIFEST.MF", expectedManifest);
        expectedContent.put("A.class", null);
        expectedContent.put("B.class", null);
        expectedContent.put("C.class", null);

        FileAssert.assertJarContent(new File(basedir, jarName), expectedContent);
    }

    private void addFiles(File dir, String... files)
        throws IOException
    {
        for (String file : files) {
            FileAssert.createFile(dir, file, DATA);
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

    private static final String[] DATA = { "\uCAFE\uBABE" };
}
