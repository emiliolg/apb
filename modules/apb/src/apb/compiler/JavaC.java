

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


package apb.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;

import static java.io.File.pathSeparator;

import static apb.utils.FileUtils.makePath;
//
// User: emilio
// Date: Sep 8, 2008
// Time: 4:42:07 PM

/**
 * A simple wrapper to JavaCompiler
 */
public class JavaC
{
    //~ Instance fields ......................................................................................

    @NotNull private final DiagnosticReporter diagnostics;

    @NotNull private final JavaCompiler compiler;
    @NotNull private final Set<File>    usedPathElements;

    //~ Constructors .........................................................................................

    /**
     * Construct a JavaC instance
     * Intialize the underlying compiler.
     * @param reporter
     */
    public JavaC(@NotNull DiagnosticReporter reporter)
    {
        compiler = ToolProvider.getSystemJavaCompiler();
        diagnostics = reporter;
        usedPathElements = new HashSet<File>();
    }

    //~ Methods ..............................................................................................

    /**
     * Innvoke the compiler
     * @param files  The files to be compiled
     * @param sourceDirs The Source Directories where the above files reside
     * @param targetDir The target directory where the output will be generated
     * @param classPath The ClassPath where dependencies will be sought
     * @param extraLibraries
     *@param additionalOptions Additional Options for the compiler.
     * @param trackUnusedPathElements Wheter to track unused path elements or not.   @return true if the compilation was succesful, false otherwise
     */
    public boolean compile(@NotNull List<File> files, @NotNull List<File> sourceDirs, @NotNull File targetDir,
                           @NotNull List<File> classPath, List<File> extraLibraries,
                           @NotNull List<String> additionalOptions, boolean trackUnusedPathElements)
    {
        DefaultJavaFileManager fileManager =
            trackUnusedPathElements ? new TrackingJavaFileManager(compiler, usedPathElements)
                                    : new DefaultJavaFileManager(compiler);

        List<String> options = new ArrayList<String>(additionalOptions);
        options.add("-d");
        options.add(targetDir.getPath());
        options.add("-classpath");
        options.add(targetDir + pathSeparator + makePath(classPath) + pathSeparator +
                    makePath(extraLibraries));
        options.add("-sourcepath");
        options.add(makePath(sourceDirs));
        usedPathElements.add(targetDir);

        boolean result =
            compiler.getTask(null, fileManager, diagnostics, options, null,
                             fileManager.getJavaFileObjects(files)).call();
        fileManager.close();
        return result;
    }

    /**
     * Return the list of unused path Elements. For this to be meaningfull the compiler must be called with
     * trackUnusedPathElements in true
     * @param classPath The classPath to check against
     * @return The list of unusedPath elements from the provided path
     */
    @NotNull public List<File> unusedPathElements(@NotNull List<File> classPath)
    {
        List<File> result = new ArrayList<File>();

        for (File file : classPath) {
            final File f = FileUtils.normalizeFile(file);

            if (!usedPathElements.contains(f)) {
                result.add(f);
            }
        }

        return result;
    }
}
