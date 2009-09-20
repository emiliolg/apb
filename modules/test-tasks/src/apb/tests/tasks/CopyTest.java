

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

import apb.tasks.FileSet;

import apb.tests.utils.FileAssert;

import static apb.tasks.CoreTasks.copy;
import static apb.tasks.CoreTasks.copyFiltering;
import static apb.tasks.CoreTasks.mkdir;

import static apb.tests.utils.FileAssert.assertDirEquals;
import static apb.tests.utils.FileAssert.assertDoesNotExist;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class CopyTest
    extends TaskTestCase
{
    //~ Instance fields ......................................................................................

    private File dir1, dir2;

    //~ Methods ..............................................................................................

    public void testDirectory()
        throws IOException
    {
        final File dir3 = new File(basedir, "dir2");

        // Empty copy
        copy("dir2").to("$basedir/dir3").execute();
        assertDoesNotExist(dir3);

        copy("dir1").to("$basedir/dir2").execute();
        assertDirEquals(dir1, dir2);

        // Copy again (Must skip copy)
        copy("dir1").to("$basedir/dir2").execute();
        assertDirEquals(dir1, dir2);

        copy(dir2).to(dir3).execute();
        assertDirEquals(dir2, dir3);
    }

    public void testFile()
        throws IOException
    {
        final File file1 = new File(dir1, "A.java");
        final File file2 = new File(dir1, "A2.java");
        copy("$basedir/dir1/A.java").to("$basedir/dir1/A2.java").execute();
        FileAssert.assertFileEquals(file1, file2);
        mkdir("dir2").execute();
        copy("$basedir/dir1/A.java").to("$basedir/dir2").execute();
        FileAssert.assertFileEquals(file1, new File(dir2, "A.java"));
    }

    public void testSimpleFileSet()
        throws IOException
    {
        final File file1 = new File(dir1, "A.java");
        final File file2 = new File(dir1, "A2.java");
        copy(FileSet.fromFile("$basedir/dir1/A.java")).to("$basedir/dir1/A2.java").execute();
        FileAssert.assertFileEquals(file1, file2);
        mkdir("dir2").execute();
        copy(FileSet.fromFile(file1)).to("$basedir/dir2").execute();
        FileAssert.assertFileEquals(file1, new File(dir2, "A.java"));
    }

    public void testPatternBased()
        throws IOException
    {
        final FileSet fileSet =
            FileSet.fromDir("$basedir/dir1")  //
                   .including("*.java")  //
                   .excluding("**/C.java");

        copy(fileSet).to("$basedir/dir2")  //
                     .execute();

        FileAssert.assertFileEquals(new File(dir1, "A.java"), new File(dir2, "A.java"));
        FileAssert.assertFileEquals(new File(dir1, "B.java"), new File(dir2, "B.java"));
        assertDoesNotExist(new File(dir2, "C.java"));
        assertDoesNotExist(new File(dir2, "a.txt"));
    }

    public void testFilterFile()
        throws IOException
    {
        createFile(dir1, "c.txt", DATA1);
        copyFiltering("$basedir/dir1").to("$basedir/dir2").execute();

        FileAssert.assertFileEquals(new File(dir1, "a.txt"), new File(dir2, "a.txt"));
        FileAssert.assertFileEquals(new File(dir1, "a.txt"), new File(dir2, "c.txt"));
    }

    @Override protected void tearDown()
        throws Exception
    {
        //delete(basedir).execute();
    }

    @Override protected void setUp()
        throws IOException
    {
        super.setUp();
        env.putProperty("l", "line");

        mkdir("dir1").execute();
        dir1 = new File(basedir, "dir1");
        addFiles(dir1, "A.java", "B.java", "C.java");
        addFiles(dir1, "a.txt", "b.txt");

        dir2 = new File(basedir, "dir2");
    }

    private void addFiles(File dir, String... files)
        throws IOException
    {
        for (String file : files) {
            createFile(dir, file, DATA);
        }
    }

    //~ Static fields/initializers ...........................................................................

    private static final String[] DATA = new String[] { "// line 1", "// line 2", "// line 3" };
    private static final String[] DATA1 = new String[] { "// $l 1", "// $l 2", "// $l 3" };
}
