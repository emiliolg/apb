

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


package apb.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Oct 1, 2008
// Time: 6:03:10 PM

//
public class DirectoryScanner
{
    //~ Instance fields ......................................................................................

    private final boolean caseSensitive = true;

    private boolean       everythingIncluded;
    private final boolean followSymlinks;

    private final File         baseDir;
    private final List<String> excludes;
    private List<String>       filesIncluded;
    private final List<String> includes;

    //~ Constructors .........................................................................................

    public DirectoryScanner(@NotNull File baseDir, @NotNull Collection<String> includes,
                            @NotNull Collection<String> excludes)
    {
        this(baseDir, includes, excludes, true);
    }

    public DirectoryScanner(@NotNull File baseDir, @NotNull Collection<String> includes,
                            @NotNull Collection<String> excludes, boolean followSymlinks)
    {
        this.baseDir = baseDir;
        this.includes = StringUtils.normalizePaths(includes);
        this.excludes = StringUtils.normalizePaths(excludes);
        this.excludes.addAll(StringUtils.normalizePaths(FileUtils.DEFAULT_EXCLUDES));
        this.followSymlinks = followSymlinks;

        if (baseDir.isFile()) {
            throw new IllegalStateException("baseDir " + baseDir + " is not a directory");
        }
    }

    //~ Methods ..............................................................................................

    public boolean isEverythingIncluded()
    {
        return everythingIncluded;
    }

    public List<String> scan()
        throws IllegalStateException, IOException
    {
        filesIncluded = new ArrayList<String>();

        if (baseDir.exists()) {
            scandir(baseDir, "");
        }

        return filesIncluded;
    }

    public List<String> getIncludedFiles()
    {
        return filesIncluded;
    }

    boolean couldHoldIncluded(String name)
    {
        if (includes.isEmpty()) {
            return true;
        }

        for (String include : includes) {
            if (StringUtils.matchPatternStart(include, name, caseSensitive)) {
                return true;
            }
        }

        return false;
    }

    boolean isIncluded(String name)
    {
        if (includes.isEmpty()) {
            return true;
        }

        for (String include : includes) {
            if (StringUtils.matchPath(include, name, caseSensitive)) {
                return true;
            }
        }

        return false;
    }

    boolean isExcluded(String name)
    {
        for (String include : excludes) {
            if (StringUtils.matchPath(include, name, caseSensitive)) {
                return true;
            }
        }

        return false;
    }

    private static List<String> filterSymbolicLinks(File dir, String relativePath, final List<String> files)
        throws IOException
    {
        List<String> result = new ArrayList<String>();

        for (String file : files) {
            if (!FileUtils.isSymbolicLink(new File(dir, relativePath + file))) {
                result.add(file);
            }
        }

        return result;
    }

    private static List<String> listFiles(File dir)
    {
        final String[] fs = dir.list();
        return fs == null ? Collections.<String>emptyList() : Arrays.asList(fs);
    }

    private void scandir(File dir, String relativePath)
        throws IOException
    {
        List<String> files = listFiles(dir);

        if (!followSymlinks) {
            files = filterSymbolicLinks(dir, relativePath, files);
        }

        for (String nm : files) {
            String fileName = relativePath + nm;
            File   file = new File(baseDir, fileName);

            if (isIncluded(fileName) && !isExcluded(fileName)) {
                if (file.isDirectory()) {
                    scandir(file, fileName + File.separator);
                }
                else if (file.isFile()) {
                    filesIncluded.add(fileName);
                }
            }
            else {
                everythingIncluded = false;

                if (file.isDirectory() && couldHoldIncluded(fileName)) {
                    scandir(file, fileName + File.separator);
                }
            }
        }
    }
}
