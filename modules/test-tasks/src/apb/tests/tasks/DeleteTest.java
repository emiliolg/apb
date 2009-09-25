

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

import apb.tasks.CoreTasks;
import apb.tasks.FileSet;

import apb.tests.testutils.FileAssert;

import static apb.tasks.CoreTasks.delete;

import static apb.tests.testutils.FileAssert.assertDoesNotExist;
import static apb.tests.testutils.FileAssert.assertExists;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class DeleteTest
    extends TaskTestCase
{
    //~ Instance fields ......................................................................................

    private File dir1;
    private File dir2;

    //~ Methods ..............................................................................................

    public void testDirectory()
        throws IOException
    {
        createFiles();

        delete("$basedir/dir1").execute();
        File dir = dir1;
        assertDoesNotExist(dir);
        assertExists(dir2);
        delete(dir2).execute();
        assertDoesNotExist(dir1);
        assertDoesNotExist(dir2);
    }

    public void testSingleFiles()
        throws IOException
    {
        createFiles();

        delete("$basedir/dir1/A.java").execute();
        delete("$basedir/dir1/B.java").execute();
        assertDoesNotExist(new File(dir1, "A.java"));
        assertDoesNotExist(new File(dir1, "B.java"));
        final File file = new File(dir1, "C.java");

        assertExists(file);

        delete(file).execute();
        assertDoesNotExist(file);
    }

    public void testPatternBased()
        throws IOException
    {
        createFiles();
        delete(FileSet.fromDir("$basedir/dir1").including("*.java")).execute();

        assertDoesNotExist(new File(dir1, "A.java"));
        assertDoesNotExist(new File(dir1, "B.java"));
        assertExists(new File(dir1, "a.txt"));
    }

    @Override protected void tearDown()
        throws Exception
    {
        delete(basedir).execute();
    }

    void createFiles()
        throws IOException
    {
        dir1 = mkdir("dir1");
        addFiles(dir1, "A.java", "B.java", "C.java");
        addFiles(dir1, "a.txt", "b.txt");
        dir2 = mkdir("dir2");
        addFiles(dir2, "A.java", "B.java", "C.java");
        addFiles(dir2, "a.txt", "b.txt");
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
        CoreTasks.mkdir(name).execute();
        return new File(basedir, name);
    }

    //~ Static fields/initializers ...........................................................................

    private static final String[] DATA = new String[] { "// line 1", "// line 2", "// line 3" };
}
