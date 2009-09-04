

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
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import apb.utils.DirectoryScanner;

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Sep 4, 2009
// Time: 4:53:20 PM

//
public abstract class FileTask
    extends Task
{
    //~ Instance fields ......................................................................................

    @NotNull protected final Set<String> excludes;
    @NotNull protected final Set<String> includes;

    //~ Constructors .........................................................................................

    public FileTask()
    {
        super();
        includes = new HashSet<String>();
        excludes = new HashSet<String>();
    }

    //~ Methods ..............................................................................................

    /**
     * Specify the list of files to include
     * @param patterns The patterns that define the list of files to include
     */
    @NotNull public FileTask including(@NotNull String... patterns)
    {
        includes.addAll(Arrays.asList(patterns));
        return this;
    }

    /**
     * Specify the list of files to exclude
     * @param patterns The patterns that define the list of files to exclude
     */
    @NotNull public FileTask excluding(@NotNull String... patterns)
    {
        excludes.addAll(Arrays.asList(patterns));
        return this;
    }

    protected Iterable<String> includedFiles(File fromDirectory)
    {
        // Defaults
        if (includes.isEmpty()) {
            includes.add("**/**");
        }

        DirectoryScanner scanner = new DirectoryScanner(fromDirectory, includes, excludes);

        try {
            scanner.scan();
        }
        catch (IOException e) {
            env.handle(e);
        }

        return scanner.getIncludedFiles();
    }
}
