package apb.tests.utils;

import java.io.File;

import junit.framework.Assert;
import apb.utils.FileUtils;// Copyright 2008-2009 Emilio Lopez-Gabeiras
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

// User: emilio
// Date: Sep 5, 2009
// Time: 8:53:25 PM

public class FileAssert {
    public static void assertDoesNotExist(File file)
    {
        Assert.assertFalse("File: '" + file.getAbsolutePath() + " still exists.", file.exists());
    }

    public static void assertExists(File file)
    {
        Assert.assertTrue("File: '" + file.getAbsolutePath() + " does not exist.", file.exists());
    }

    public static void assertEqualsDirs(File dir, File targetDir) {
        File[] l = dir.listFiles();
        for (File file1 : l) {
            final File file2 = new File(targetDir, FileUtils.removePrefix(dir, file1));
            assertEqualsFiles(file1, file2);
        }
    }

    public static void assertEqualsFiles(File file1, File file2) {
        assertExists(file2);
        final String msg = String.format("File content differs between '%s' and '%s'", file1, file2);
        Assert.assertTrue(msg, FileUtils.equalsContent(file1, file2));
    }
}
