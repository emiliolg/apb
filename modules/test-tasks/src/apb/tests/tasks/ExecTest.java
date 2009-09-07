

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
import java.io.FileWriter;
import java.io.IOException;

import apb.Apb;
import apb.Environment;
import apb.tasks.CoreTasks;
import static apb.tasks.CoreTasks.delete;
import static apb.tasks.CoreTasks.exec;
import static apb.tests.utils.FileAssert.assertDoesNotExist;
import static apb.tests.utils.FileAssert.assertExists;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class ExecTest
    extends TaskTestCase
{
    //~ Instance fields ......................................................................................

    private File          dir1;
    private File          dir2;

    //~ Methods ..............................................................................................

    public void testRmDirectory()
        throws IOException
    {
        createFiles();

        exec("rm", "-rf", "$basedir/dir1").execute();
        assertExists(dir2);
        assertDoesNotExist(dir1);
        exec("rm", "-rf", "$basedir").execute();
        assertDoesNotExist(dir2);
        assertDoesNotExist(basedir);
    }
    public void testExpr()
        throws IOException
    {
        createFiles();

        exec("expr", "49", "/", "7").execute();
        assertExists(dir2);
        assertDoesNotExist(dir1);
        exec("rm", "-rf", "$basedir").execute();
        assertDoesNotExist(dir2);
        assertDoesNotExist(basedir);
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
            createFile(dir, file, DATA);
        }
    }

    private File mkdir(String name)
    {
        File result = new File(basedir, name);
        result.mkdir();
        return result;
    }

    //~ Static fields/initializers ...........................................................................

    private static final String[] DATA = new String[] { "// line 1", "// line 2", "// line 3" };
}