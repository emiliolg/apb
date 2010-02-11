

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

import apb.metadata.JavadocInfo;
import apb.tests.testutils.FileAssert;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static apb.tasks.CoreTasks.javadoc;
import static java.util.Arrays.asList;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class JavadocTest
    extends TaskTestCase
{
    //~ Instance fields ......................................................................................

    private File       javadocDir;
    private List<File> classpath;

    //~ Methods ..............................................................................................

    public void test1()
        throws IOException
    {
        javadoc("$module-src").to("$basedir/javadoc")  //
                              .withGroups(asList(new JavadocInfo.Group("Tests", "apb.tests.testutils",
                                                                       "apb.tests.tasks")))  //
                              .withClassPath(classpath)  //
                              .includeDeprecatedInfo(false)  //
                              .additionalOptions(asList("-notimestamp")).execute();
        FileAssert.assertFileEquals(new File(javadocDir, OVERVIEW_SUMMARY), dataFile(OVERVIEW_SUMMARY));
        FileAssert.assertExists(new File(javadocDir, "apb/tests/javas/SumArgs.html"));
        FileAssert.assertDoesNotExist(new File(javadocDir, "apb/tests/tasks/class-use/CopyTest.html"));
    }

    public void test2()
        throws IOException
    {
        javadoc("$module-src").to("$basedir/javadoc")  //
                              .including(asList("apb.tests.testutils", "apb.tests.tasks"))  //
                              .excluding(asList("apb.tests.javas"))  //
                              .createUsePages(true)  //
                              .generateHtmlSource(true)  //
                              .generateIndex(true)  //
                              .generateDeprecatedList(true)  //
                              .generateClassHierarchy(true)  //
                              .withHeader("Example Header")  //
                              .withFooter("Example Footer")  //
                              .withBottom("Example Bottom")  //
                              .withVisibility(JavadocInfo.Visibility.PACKAGE)  //
                              .withTitle("Example Title")  //
                              .withWindowTitle("Example Window Title")  //
                              .splitIndexPerLetter(false)  //
                              .includeAuthorInfo(true)  //
                              .includeSinceInfo(true)  //
                              .includeHelpLinks(true)  //
                              .includeDeprecatedInfo(true)  //
                              .includeVersionInfo(true)  //
                              .maxMemory(128)  //
                              .withClassPath(classpath)  //
                              .withOverview("")  //
                              .additionalOptions(asList("-notimestamp")).execute();

        FileAssert.assertDoesNotExist(new File(javadocDir, "apb/tests/javas/SumArgs.html"));
        FileAssert.assertExists(new File(javadocDir, "apb/tests/tasks/class-use/CopyTest.html"));
        FileAssert.assertFileEquals(new File(javadocDir, OVERVIEW_SUMMARY), dataFile(OVERVIEW_SUMMARY + "2"));
    }

    protected void setUp()
        throws IOException
    {
        super.setUp();
        javadocDir = new File(basedir, "javadoc");

        final File apbJar = env.fileFromBase("$apb-jar");
        classpath = asList(apbJar, new File(apbJar.getParentFile(), "junit-4.7.jar"));
    }

    //~ Static fields/initializers ...........................................................................

    private static final String OVERVIEW_SUMMARY = "overview-summary.html";
}
