

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import apb.utils.FileUtils;
//
// User: emilio
// Date: Sep 8, 2008
// Time: 4:42:07 PM

//
public class JavaC
{
    //~ Instance fields ......................................................................................

    private JavaCompiler       compiler;
    private DiagnosticReporter diagnostics;
    private Set<File>          usedPathElements;

    //~ Constructors .........................................................................................

    public JavaC(DiagnosticReporter reporter)
    {
        compiler = ToolProvider.getSystemJavaCompiler();
        diagnostics = reporter;
        usedPathElements = new HashSet<File>();
    }

    //~ Methods ..............................................................................................

    public boolean compile(List<File> files, List<File> sourceDirs, File targetDir, List<File> classPath,
                           List<String> additionalOptions, boolean trackUnusedPathElements)
    {
        DefaultJavaFileManager fileManager =
            trackUnusedPathElements ? new TrackingJavaFileManager(compiler, usedPathElements)
                                    : new DefaultJavaFileManager(compiler);

        List<String> options = new ArrayList<String>(additionalOptions);
        options.add("-d");
        options.add(targetDir.getPath());
        options.add("-classpath");
        options.add(targetDir + File.pathSeparator + FileUtils.makePath(classPath));
        options.add("-sourcepath");
        options.add(FileUtils.makePath(sourceDirs));

        boolean result =
            compiler.getTask(null, fileManager, diagnostics, options, null,
                             fileManager.getJavaFileObjects(files)).call();
        fileManager.close();
        return result;
    }

    public List<File> unusedPathElements(List<File> classPath)
    {
        List<File> result = new ArrayList<File>();

        for (File file : classPath) {
            try {
                final File f = file.getCanonicalFile();

                if (!usedPathElements.contains(f)) {
                    result.add(f);
                }
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return result;
    }
}
