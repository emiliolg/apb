

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
import java.util.Collections;
import java.util.List;

import apb.tasks.FileSet;

import apb.tests.testutils.FileAssert;
import static apb.tests.testutils.FileAssert.assertSame;

import static java.util.Arrays.asList;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class FileSetTest
    extends TaskTestCase
{
    //~ Instance fields ......................................................................................

    private File dir1;

    //~ Methods ..............................................................................................

    public void testPatternBased()
        throws IOException
    {
        dir1 = mkdir("dir1");
        addFiles(dir1, "A.java", "B.java", "C.java");
        addFiles(dir1, "a.txt", "b.txt");
        addFiles(dir1, "a.xml");

        final FileSet fileSet =
            FileSet.fromDir("dir1")  //
                   .including("*.java", "*.xml")  //
                   .excluding("**/C.java")  //
                   .followSymbolicLinks(false);

        assertFalse("Single file!", fileSet.isFile());
        assertEquals(asList("*.java", "*.xml"), fileSet.getIncludes());
        assertEquals(asList("**/C.java"), fileSet.getExcludes());
        assertEquals(new File(basedir, "dir1").getAbsoluteFile(), fileSet.getDir());
        assertEquals(basedir.getAbsolutePath() + "/dir1/{*.java,*.xml}-**/C.java", fileSet.toString());

        FileAssert.assertSame(asList("A.java", "a.xml", "B.java"), fileSet.list());
    }

    public void testSingleFile()
        throws IOException
    {
        dir1 = mkdir("dir1");
        addFiles(dir1, "A.java", "B.java", "C.java");

        final FileSet fileSet = FileSet.fromFile("dir1/A.java");

        assertTrue("Not a single file!", fileSet.isFile());
        assertTrue(fileSet.getIncludes().isEmpty());
        assertTrue(fileSet.getExcludes().isEmpty());
        assertEquals(new File(basedir, "dir1").getAbsoluteFile(), fileSet.getDir());
        assertEquals(basedir.getAbsolutePath() + "/dir1/A.java", fileSet.toString());

        List<String> l = fileSet.list();
        assertEquals(asList("A.java"), l);
    }

    public void testExceptions()
        throws IOException
    {
        dir1 = mkdir("dir1");
        addFiles(dir1, "A.java");

        boolean exceptionThrown;

        try {
            FileSet.fromFile("dir1");
            exceptionThrown = false;
        }
        catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);

        try {
            FileSet.fromFile("dir1/A.java").including("*.java");
            exceptionThrown = false;
        }
        catch (UnsupportedOperationException e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);

        try {
            FileSet.fromFile("dir1/A.java").excluding("*.java");
            exceptionThrown = false;
        }
        catch (UnsupportedOperationException e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);

        try {
            FileSet.fromFile("dir1/A.java").followSymbolicLinks(false);
            exceptionThrown = false;
        }
        catch (UnsupportedOperationException e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
    }

    public void testSingleDir()
        throws IOException
    {
        dir1 = mkdir("dir1");
        addFiles(dir1, "A.java", "B.java", "C.java");

        final FileSet fileSet = FileSet.fromDir("dir1");

        assertFalse("Single file!", fileSet.isFile());
        assertTrue(fileSet.getIncludes().isEmpty());
        assertTrue(fileSet.getExcludes().isEmpty());
        assertEquals(new File(basedir, "dir1").getAbsoluteFile(), fileSet.getDir());
        assertEquals(basedir.getAbsolutePath() + "/dir1", fileSet.toString());

        FileAssert.assertSame(asList("A.java", "B.java", "C.java"), fileSet.list());
    }

    public void testEmpty()
        throws IOException
    {
        final FileSet fileSet =
            FileSet.fromDir("dir2")  //
                   .including("*.java", "*.xml")  //
                   .excluding("**/C.java");

        assertEquals(basedir.getAbsolutePath() + "/dir2/{*.java,*.xml}-**/C.java", fileSet.toString());

        List<String> l = fileSet.list();
        assertEquals(Collections.<String>emptyList(), l);
    }

    private void addFiles(File dir, String... files)
        throws IOException
    {
        for (String file : files) {
            FileAssert.createFile(dir, file, DATA);
        }
    }

    private File mkdir(String name)
    {
        File result = new File(basedir, name);

        if (!result.exists()) {
            result.mkdir();
        }

        return result;
    }

    //~ Static fields/initializers ...........................................................................

    private static final String[] DATA = new String[] { "// line 1", "// line 2", "// line 3" };
}
