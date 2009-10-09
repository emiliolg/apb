

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


package apb.tests.testutils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import apb.utils.FileUtils;
import static apb.utils.FileUtils.FILE_COMPARATOR;

import junit.framework.Assert;

import static java.util.Arrays.asList;

public class FileAssert
{
    private static final Comparator<String> STRING_COMPARATOR = new Comparator<String>(){
        @Override
        public int compare(final String o1,final String o2) {
            return o1.compareTo(o2);
        }
    };
    //~ Methods ..............................................................................................

    public static void assertDoesNotExist(File file)
    {
        Assert.assertFalse("File: '" + file.getAbsolutePath() + "' still exists.", file.exists());
    }

    public static void assertExists(File file)
    {
        Assert.assertTrue("File: '" + file.getAbsolutePath() + "' does not exist.", file.exists());
    }

    public static void assertDirEquals(File dir, File targetDir)
    {
        assertDirEquals(dir, targetDir, true);
    }

    public static void assertDirEquals(File dir, File targetDir, boolean compareContent)
    {
        File[] files = dir.listFiles();
        File[] targetFiles = targetDir.listFiles();

        if (files == null) {
            assertExists(dir);
        }
        else if (targetFiles == null) {
            assertExists(targetDir);
        }
        else {
            Set<File> target = new TreeSet<File>(asList(targetFiles));

            for (File file1 : files) {
                final File file2 = new File(targetDir, FileUtils.removePrefix(dir, file1));

                if (compareContent) {
                    assertFileEquals(file1, file2);
                }
                else {
                    Assert.assertEquals(file1.getName(), file2.getName());
                }

                target.remove(file2);
            }

            final boolean allFound = target.isEmpty();

            if (!allFound) {
                Assert.assertTrue("Extra files: " + target, allFound);
            }
        }
    }

    public static void assertFileEquals(File file1, File file2)
    {
        assertExists(file2);
        final String msg =
            String.format("File content differs between '%s'.\n" +  //
                          "                         and '%s'.", file1.getAbsolutePath(),
                          file2.getAbsolutePath());
        Assert.assertTrue(msg, FileUtils.equalsContent(file1, file2));
    }

    public static void createFile(File dir, String file, String[] data)
        throws IOException
    {
        File       f = new File(dir, file);
        FileWriter w = new FileWriter(f);

        for (String s : data) {
            w.write(s + "\n");
        }

        w.close();
    }

    public static void assertSameFiles(final List<String> expected, Collection<File> files) {
        Assert.assertEquals(sort(expected, STRING_COMPARATOR).toString(),  sort(files, FILE_COMPARATOR).toString());
    }

    public static void assertSame(final List<String> expected, Collection<String> files) {
        Assert.assertEquals(sort(expected, STRING_COMPARATOR).toString(),  sort(files, STRING_COMPARATOR).toString());
    }



    public static <T> Collection<T> sort(Collection<T> files, Comparator<T> comparator) {
        final List<T> result = new ArrayList<T>(files);
        Collections.sort(result, comparator);
        return result;
    }

}
