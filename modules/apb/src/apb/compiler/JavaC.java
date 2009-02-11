
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

package apb.compiler;

import apb.utils.FileUtils;

import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
//
// User: emilio
// Date: Sep 8, 2008
// Time: 4:42:07 PM

//
public class JavaC
{
    //~ Methods ..............................................................................................

    public boolean compile(List<File> files, List<File> sourceDirs, File targetDir, String classPath,
                           List<String> additionalOptions, DiagnosticListener<JavaFileObject> diagnostics)
    {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        List<String>  options = new ArrayList<String>(additionalOptions);
        options.add("-d");
        options.add(targetDir.getPath());
        options.add("-classpath");
        options.add(classPath);
        options.add("-sourcepath");
        options.add(FileUtils.makePath(sourceDirs));

        boolean result =
            compiler.getTask(null, fileManager, diagnostics, options, null,
                             fileManager.getJavaFileObjectsFromFiles(files)).call();
        close(fileManager);
        return result;
    }

    protected void close(JavaFileManager fileManager)
    {
        try {
            fileManager.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
