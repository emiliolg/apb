

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


package apb.tests.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import apb.tasks.CoreTasks;

import apb.tests.testutils.FileAssert;
import static apb.tests.testutils.FileAssert.assertSameFiles;

import apb.utils.FileUtils;

import junit.framework.TestCase;

import static java.util.Arrays.asList;

import static apb.tasks.CoreTasks.delete;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class FileTest
    extends TestCase
{
    //~ Instance fields ......................................................................................

    private File basedir;
    private File dir1, dir2;

    //~ Methods ..............................................................................................

    public void testChangeExt()
        throws IOException
    {
        File f = FileUtils.changeExtension(new File("A.B.pepe"), ".class");
        assertEquals(new File("A.B.class"), f);

        f = FileUtils.changeExtension(new File("pepe"), "class");

        assertEquals(new File("pepe.class"), f);
    }

    public void testExtension()
        throws IOException
    {
        String f = FileUtils.extension(new File("A.B.pepe"));
        assertEquals("pepe", f);

        f = FileUtils.extension(new File("pepe"));

        assertEquals("", f);
    }

    public void testRemoveExtension()
        throws IOException
    {
        String f = FileUtils.removeExtension(new File("A.B.pepe"));
        assertEquals("A.B", f);

        f = FileUtils.removeExtension(new File("pepe"));

        assertEquals("pepe", f);
    }

    public void testCwd()
        throws IOException
    {
        String f = FileUtils.getCurrentWorkingDirectory();
        assertTrue(f.endsWith("test-tasks"));
    }

    public void testMakePath()
    {
        File   f1 = new File("a/A.java");
        File   f2 = new File("a/B.java");
        String path = FileUtils.makePath(f1, f2);
        assertEquals("a/A.java:a/B.java", path);
        path = FileUtils.makePath(asList(f1, f2));
        assertEquals("a/A.java:a/B.java", path);

        path = FileUtils.makePath(asList(f1, f2), "->");
        assertEquals("a/A.java->a/B.java", path);

        path = FileUtils.makePathFromStrings(asList(f1.getPath(), f2.getPath()));
        assertEquals("a/A.java:a/B.java", path);
    }

    public void testApbDir()
        throws IOException
    {
        File f = FileUtils.getApbDir();
        assertEquals(new File(System.getProperty("user.home"), ".apb"), f);
    }

    public void testMakeRelative()
        throws IOException
    {
        final File p = new File("a/b/x/");
        File       f = FileUtils.makeRelative(p, new File("a/b/x/y/X.java"));
        assertEquals(new File("y/X.java"), f);

        String path = FileUtils.removePrefix(p.getParentFile(), new File(p, f.getPath()));

        assertEquals("x/y/X.java", path);

        f = FileUtils.makeRelative(new File("a/b/x/"), new File("a/y/X.java"));
        assertEquals(new File("../../y/X.java"), f);

        f = FileUtils.makeRelative(new File("x/"), new File("x"));
        assertEquals(new File("."), f);

        path = FileUtils.makeRelative(new File("a/b/x/"), "a/y/../X.java");
        assertEquals("../../X.java", path);
    }

    public void testList()
        throws IOException
    {
        long ts = System.currentTimeMillis() - 10000;
        createFiles();
        assertSameFiles(asList("tmp/dir1"), FileUtils.listDirsWithFiles(basedir, "txt"));
        assertSameFiles(asList("tmp/dir1", "tmp/dir2"), FileUtils.listDirsWithFiles(basedir, "java"));



        assertSameFiles(ALL_JAVA, FileUtils.listAllFilesWithExt(basedir, "java"));



        assertSameFiles(ALL_JAVA, FileUtils.listAllFilesWithExt(asList(dir1, dir2), "java"));


        assertSameFiles(asList("A.java", "A.java", "B.java", "B.java", "C.java", "C.java"), FileUtils.listJavaSources(asList(dir1, dir2)));

        final Collection<File> collection = FileUtils.listAllFiles(dir1);

        assertSameFiles(ALL_DIR1, collection);

        boolean b = FileUtils.uptodate(collection, System.currentTimeMillis());
        assertTrue(b);

        b = FileUtils.uptodate(collection, ts);
        assertFalse(b);

        b = FileUtils.uptodate(dir1, "java", System.currentTimeMillis());
        assertTrue(b);

        b = FileUtils.uptodate(dir1, "java", ts);
        assertFalse(b);

        final File f = new File(dir1, "A.java");
        ts -= 1000;
        FileUtils.touch(f, ts);
        assertEquals(f.lastModified() / 1000, ts / 1000);

        long l = FileUtils.lastModified(collection);
        assertTrue(l > ts);

        URL[] urls = FileUtils.toUrl(collection);
        assertEquals("A.java", new File(urls[0].getFile()).getName());

        String p = FileUtils.normalizePath(new File(dir1, "../dir2/A.java"));
        assertEquals(new File(dir2, "A.java").getAbsolutePath(), p);
    }

    public void testEquals()
        throws IOException
    {
        createFiles();
        assertTrue(FileUtils.equalsContent(new File(dir1, "a.java"), new File(dir2, "A.java")));
    }

    public void testTopSingleDirectory()
        throws IOException
    {
        createFiles();
        File base = basedir.getAbsoluteFile();
        File classes = new File(base.getParentFile(), "classes");
        assertEquals("apb/tests", FileUtils.topSingleDirectory(classes));
        assertEquals("", FileUtils.topSingleDirectory(base));

        CoreTasks.delete(dir2).execute();

        assertEquals("dir1", FileUtils.topSingleDirectory(base));
    }

    void createFiles()
        throws IOException
    {
        basedir = new File("tmp");

        if (basedir.exists()) {
            delete(basedir).execute();
        }

        dir1 = mkdir("dir1");
        addFiles(dir1, "A.java", "B.java", "C.java");
        addFiles(dir1, "a.txt", "b.txt");
        dir2 = mkdir("dir2");
        addFiles(dir2, "A.java", "B.java", "C.java");
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
        final File file = new File(basedir, name);
        file.mkdirs();
        return file;
    }

    //~ Static fields/initializers ...........................................................................

    private static final List<String> ALL_DIR1 =
        asList("tmp/dir1/A.java", "tmp/dir1/B.java", "tmp/dir1/C.java", "tmp/dir1/a.txt", "tmp/dir1/b.txt");

    private static final List<String> ALL_JAVA =
        asList("tmp/dir1/A.java", "tmp/dir1/B.java", "tmp/dir1/C.java", "tmp/dir2/A.java", "tmp/dir2/B.java", "tmp/dir2/C.java");

    private static final String[] DATA = { "// Line2", "// Line2" };
}
