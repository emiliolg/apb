

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

import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;

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

    /**
     * The set of used path elements (jars & dirs)
     */
    @NotNull private final Set<File> usedPathElements;

    //~ Constructors .........................................................................................

   /**
    * Construct a TrackingJavaFileManager instance
    * @param compiler the Java Compiler
    * @param usedPathElements The set of used path elements (jars & dirs)
    */
    protected TrackingJavaFileManager(@NotNull JavaCompiler compiler, @NotNull Set<File> usedPathElements)
    {
        super(compiler);
        this.usedPathElements = usedPathElements;
    }

    //~ Methods ..............................................................................................

    /**
     * Implementation of {@link javax.tools.JavaFileManager#list} that wraps every JavaFileObject
     * returned to be able to track if the file was used.
     *
     * @param location     a location
     * @param packageName  a package name
     * @param kinds        return objects only of these kinds
     * @param recurse      if true include sub packages
     * @return an Iterable of file objects matching the given criteria
     * @throws IOException if an I/O error occurred
     */
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

    /**
     * Implements {@link javax.tools.JavaFileManager#inferBinaryName} invoking the default implementation
     * over the original JavaFileObject (Not the wrapped one).
     * 
     * @param location a location
     * @param file a file object
     * @return a binary name or {@code null} the file object is not
     */
    @Override public String inferBinaryName(Location location, JavaFileObject file)
    {
        return super.inferBinaryName(location,
                                     file instanceof TrackingJavaFileObject
                                     ? ((TrackingJavaFileObject) file).getTarget() : file);
    }

    //~ Inner Classes ........................................................................................

    /**
     * Wrapper to {@link JavaFileObject} that tracks when a file object was used
     */
    class TrackingJavaFileObject
        extends ForwardingJavaFileObject<JavaFileObject>
    {
        /**
         * The package name of the file object
         */
        @NotNull private final String packageName;

        /**
         * Creates a new instance of ForwardingJavaFileObject.
         *
         * @param packageName  The package name of the file object
         * @param fileObject delegate to this file object
         */
        protected TrackingJavaFileObject(String packageName, JavaFileObject fileObject)
        {
            super(fileObject);
            this.packageName = packageName;
        }

        /**
         * Override {@link javax.tools.JavaFileObject#openInputStream()} to track when a JavaFileObject was actually used
         */

        @Override public InputStream openInputStream()
            throws IOException
        {
            track();
            return super.openInputStream();
        }

        /**
         * Return the original file Object
         */
        JavaFileObject getTarget()
        {
            return fileObject;
        }

        /**
         * Track the usage of this file Object
         * It extracts the jar or directory name of the class and adds it to the set of
         * used path elements.
         */
        private void track()
        {
            String s = fileObject.toUri().getPath();
            s = s.substring(0, s.length() - fileObject.getName().length() - packageName.length() - 2);

            usedPathElements.add(FileUtils.normalizeFile(new File(s)));
        }
    }
}
