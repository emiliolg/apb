

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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.tools.ForwardingJavaFileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;

// User: emilio
// Date: Mar 9, 2009
// Time: 2:03:01 PM

/**
 * This class wraps the {@link javax.tools.JavaFileManager#list}
 * to return a delegate to the returned {@link JavaFileObject}.
 * This delegates are then used to track if the fileObjects are used.
 *
 */

public class TrackingJavaFileManager
    extends DefaultJavaFileManager
{
    //~ Instance fields ......................................................................................

    private Set<File> usedPathElements;

    //~ Constructors .........................................................................................

    protected TrackingJavaFileManager(JavaCompiler compiler, Set<File> usedPathElements)
    {
        super(compiler);
        this.usedPathElements = usedPathElements;
    }

    //~ Methods ..............................................................................................

    @Override public Iterable<JavaFileObject> list(Location location, String packageName,
                                                   Set<JavaFileObject.Kind> kinds, boolean recurse)
        throws IOException
    {
        Iterable<JavaFileObject> iter = super.list(location, packageName, kinds, recurse);
        List<JavaFileObject>     result = new ArrayList<JavaFileObject>();

        for (JavaFileObject javaFileObject : iter) {
            result.add(new TrackingJavaFileObject(packageName, javaFileObject));
        }

        return result;
    }

    @Override public String inferBinaryName(Location location, JavaFileObject file)
    {
        return super.inferBinaryName(location,
                                     file instanceof TrackingJavaFileObject
                                     ? ((TrackingJavaFileObject) file).getTarget() : file);
    }

    private void track(JavaFileObject fileObject, String packageName)
    {
        String s = fileObject.toUri().getPath();
        s = s.substring(0, s.length() - fileObject.getName().length() - packageName.length() - 2);

        try {
            usedPathElements.add(new File(s).getCanonicalFile());
        }
        catch (IOException e) {
            // This should not happen
            throw new RuntimeException(e);
        }
    }

    //~ Inner Classes ........................................................................................

    class TrackingJavaFileObject
        extends ForwardingJavaFileObject<JavaFileObject>
    {
        private String packageName;

        /**
         * Creates a new instance of ForwardingJavaFileObject.
         *
         * @param packageName
         * @param fileObject delegate to this file object
         */
        protected TrackingJavaFileObject(String packageName, JavaFileObject fileObject)
        {
            super(fileObject);
            this.packageName = packageName;
        }

        @Override public InputStream openInputStream()
            throws IOException
        {
            track(fileObject, packageName);
            return super.openInputStream();
        }

        JavaFileObject getTarget()
        {
            return fileObject;
        }
    }
}
