

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
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

/**
 * A Default {@link javax.tools.JavaFileManager} that just forward everything to {@link javax.tools.StandardJavaFileManager}
 * Plus add some simple defaults.
 */
public class DefaultJavaFileManager
    extends ForwardingJavaFileManager<StandardJavaFileManager>
{
    //~ Constructors .........................................................................................

    protected DefaultJavaFileManager(JavaCompiler compiler)
    {
        super(compiler.getStandardFileManager(null, null, null));
    }

    //~ Methods ..............................................................................................

    /**
     * Forward the invocation
     *
     * @param files The files to be loaded
     * @return The loaded files
     */
    public Iterable<? extends JavaFileObject> getJavaFileObjects(Iterable<File> files)
    {
        return fileManager.getJavaFileObjectsFromFiles(files);
    }

    /**
     * Wrap the IOException from close in a runtime one.
     */
    @Override public void close()
    {
        try {
            super.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
