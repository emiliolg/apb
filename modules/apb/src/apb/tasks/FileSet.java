

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


package apb.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import apb.Apb;

import apb.utils.DirectoryScanner;

import org.jetbrains.annotations.NotNull;

import static java.util.Arrays.asList;

import static apb.utils.CollectionUtils.expandAll;
//
// User: emilio
// Date: Sep 9, 2009
// Time: 9:51:56 AM

//
public class FileSet
{
    //~ Instance fields ......................................................................................

    boolean singleFile = true;

    private boolean followSymLinks;

    @NotNull private final File         dir;
    @NotNull private final List<String> excludes;
    @NotNull private final List<String> includes;

    //~ Constructors .........................................................................................

    private FileSet(File directory)
    {
        dir = directory;
        includes = new ArrayList<String>();
        excludes = new ArrayList<String>();
        followSymLinks = true;
    }

    //~ Methods ..............................................................................................

    /**
     * The root of the directory tree of this FileSet.
     * @param directoryName The root of the directory tree of this FileSet.
     */
    public static FileSet fromDir(@NotNull String directoryName)
    {
        return fromDir(Apb.getEnv().fileFromBase(directoryName));
    }

    /**
     * The root of the directory tree of this FileSet.
     * @param directory The root of the directory tree of this FileSet.
     */
    public static FileSet fromDir(@NotNull File directory)
    {
        return new FileSet(directory);
    }

    /**
     * Shortcut for specifying a single-file fileset
     * @param fileName The filename to include
     */
    public static FileSet fromFile(@NotNull String fileName)
    {
        return fromFile(Apb.getEnv().fileFromBase(fileName));
    }

    /**
     * Shortcut for specifying a single-file fileset
     * @param fileName The filename to include
     */
    public static FileSet fromFile(@NotNull File fileName)
    {
        if (!fileName.isFile()) {
            throw new IllegalArgumentException("Not a file: '" + fileName + "'");
        }

        return new Single(fileName);
    }

    /**
     * List of patterns of files that must be included.
     * All files are included when omitted.
     * @param patterns List of patterns of files that must be included.
     */
    public FileSet including(@NotNull String... patterns)
    {
        return including(asList(patterns));
    }

    /**
     * List of patterns of files that must be included.
     * All files are included when omitted.
     * @param patterns List of patterns of files that must be included.
     */
    public FileSet including(@NotNull List<String> patterns)
    {
        includes.addAll(expandAll(Apb.getEnv(), patterns));
        return this;
    }

    /**
     * List of patterns of files that must be excluded.
     * No files (except default excludes) are excluded when omitted.
     * @param patterns The List of patterns of files that must be excluded
     */
    public FileSet excluding(@NotNull String... patterns)
    {
        return excluding(asList(patterns));
    }

    /**
     * List of patterns of files that must be excluded.
     * No files (except default excludes) are excluded when omitted.
     * @param patterns The List of patterns of files that must be excluded
     */
    public FileSet excluding(@NotNull List<String> patterns)
    {
        excludes.addAll(expandAll(Apb.getEnv(), patterns));
        return this;
    }

    /**
     * Shall symbolic links be followed? Defaults to true
     */
    public FileSet followSymbolicLinks(boolean b)
    {
        followSymLinks = b;
        return this;
    }

    /**
     * Return the list of files defined by this FileSet
     * @return The list of files defined by this file set
     */
    public List<String> list()
    {
        DirectoryScanner scanner = new DirectoryScanner(dir, includes, excludes, followSymLinks);

        return scanner.scan();
    }

    /**
     * Return the base directory of the file set
     */
    @NotNull public File getDir()
    {
        return dir;
    }

    @Override public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append(dir.getPath());

        if (!includes.isEmpty() || !excludes.isEmpty()) {
            result.append(File.separatorChar);
            append(result, includes);

            if (!excludes.isEmpty()) {
                result.append("-");
                append(result, excludes);
            }
        }

        return result.toString();
    }

    public boolean isFile()
    {
        return false;
    }

    @NotNull public List<String> getIncludes()
    {
        return includes;
    }

    @NotNull public List<String> getExcludes()
    {
        return excludes;
    }

    private void append(StringBuilder result, final List<String> elements)
    {
        if (elements.size() == 1) {
            result.append(elements.get(0));
        }
        else if (elements.size() > 1) {
            result.append("{");

            for (int i = 0; i < elements.size(); i++) {
                if (i > 0) {
                    result.append(",");
                }

                result.append(elements.get(i));
            }

            result.append("}");
        }
    }

    //~ Inner Classes ........................................................................................

    private static class Single
        extends FileSet
    {
        private String name;

        private Single(File fileName)
        {
            super(fileName.getParentFile());
            name = fileName.getName();
        }

        @Override public FileSet excluding(@NotNull List<String> patterns)
        {
            throw unsupported();
        }

        @Override public FileSet including(@NotNull List<String> patterns)
        {
            throw unsupported();
        }

        @Override public boolean isFile()
        {
            return true;
        }

        @Override public List<String> list()
        {
            return Collections.singletonList(name);
        }

        @Override public FileSet followSymbolicLinks(boolean b)
        {
            throw unsupported();
        }

        @Override public String toString()
        {
            return new File(getDir(), name).getPath();
        }

        private UnsupportedOperationException unsupported()
        {
            return new UnsupportedOperationException("Invalid operation for single files.");
        }
    }
}
