
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.jetbrains.annotations.NotNull;

import static java.util.Arrays.asList;

/**
 * A class that allows invoking 'javac' and generating an 'in-memory' representation
 * of the compiled classes.
 */
public class InMemJavaC
    extends JavaC
{
    //~ Instance fields ......................................................................................

    private JavaCompiler          compiler;
    private MemoryJavaFileManager fileManager;
    private MemoryClassLoader     memoryClassLoader;

    //~ Constructors .........................................................................................

    public InMemJavaC()
    {
        compiler = ToolProvider.getSystemJavaCompiler();
        memoryClassLoader = new MemoryClassLoader(getClass().getClassLoader());
        fileManager = new MemoryJavaFileManager(compiler, memoryClassLoader);
    }

    //~ Methods ..............................................................................................

    /**
     * Compile the file and invoke the main method of the compiled class
     */
    public static void main(String[] args)
        throws ClassNotFoundException
    {
        if (args.length < 1) {
            System.err.println("Usage: InMemJavac file arguments...");
            System.exit(0);
        }

        File source = new File(args[0]);

        if (!source.exists()) {
            System.err.println("Can't open: " + args[0]);
            System.exit(0);
        }

        System.arraycopy(args, 1, args, 0, args.length - 1);

        InMemJavaC javac = new InMemJavaC();

        invokeMain(javac.compileToClass(null, source), args);
    }

    public long sourceLastModified(@NotNull Class clazz)
    {
        return memoryClassLoader.equals(clazz.getClassLoader())
               ? memoryClassLoader.sourceLastModified(clazz.getName()) : 0;
    }

    /**
     * Compile the source in the specified File and return the associated class
     * @param source the file to be compiled
     * @return The compiled class
     */
    public Class<?> compileToClass(File dir, File source)
    {
        List<String> options = dir == null ? null : asList("-sourcepath", dir.getAbsolutePath());
        boolean      result =
            compiler.getTask(null, fileManager, null, options, null, fileManager.getJavaFileObjects(source))
                    .call();
        close(fileManager);

        try {
            return result ? memoryClassLoader.getClassFromSource(source) : null;
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void invokeMain(Class<?> clazz, String[] args)
    {
        try {
            Method         m = clazz.getMethod("main", String[].class);
            final Object[] a = { args };
            m.invoke(null, a);
        }
        catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    //~ Inner Classes ........................................................................................

    static class ClassInfo
    {
        private ByteArrayOutputStream bytes;
        private long                  lastModified;

        public ClassInfo(ByteArrayOutputStream outputStream, long lastModified)
        {
            bytes = outputStream;
            this.lastModified = lastModified;
        }

        public byte[] getBytes()
        {
            return bytes.toByteArray();
        }
    }

    static class MemoryClassLoader
        extends ClassLoader
    {
        private HashMap<String, ClassInfo> classMap;

        MemoryClassLoader(ClassLoader parent)
        {
            super(parent);
            classMap = new HashMap<String, ClassInfo>();
        }

        public HashMap<String, ClassInfo> getClassMap()
        {
            return classMap;
        }

        public Class<?> getClassFromSource(File source)
            throws ClassNotFoundException
        {
            String path = source.getPath();
            int    lastDot = path.lastIndexOf('.');

            if (lastDot != -1) {
                path = path.substring(0, lastDot);
            }

            path = path.replace('/', '.');

            for (String className : getClassMap().keySet()) {
                if (path.endsWith(className)) {
                    return findClass(className);
                }
            }

            throw new ClassNotFoundException();
        }

        public long sourceLastModified(String className)
        {
            ClassInfo classInfo = classMap.get(className);
            return classInfo == null ? 0 : classInfo.lastModified;
        }

        protected Class<?> findClass(String className)
            throws ClassNotFoundException
        {
            ClassInfo classInfo = classMap.get(className);

            if (classInfo == null) {
                throw new ClassNotFoundException(className);
            }

            final byte[] bytes = classInfo.getBytes();
            return defineClass(className, bytes, 0, bytes.length);
        }
    }

    static class MemoryJavaOutput
        extends SimpleJavaFileObject
    {
        private String                 className;
        private Map<String, ClassInfo> compiledClasses;
        private long                   lastModified;

        public MemoryJavaOutput(FileObject fileObject, String className,
                                Map<String, ClassInfo> compiledClasses)
        {
            super(fileObject.toUri(), Kind.CLASS);
            this.compiledClasses = compiledClasses;
            this.className = className;
            lastModified = fileObject.getLastModified();
        }

        public OutputStream openOutputStream()
            throws IOException
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            compiledClasses.put(className, new ClassInfo(outputStream, lastModified));
            return outputStream;
        }
    }

    private static class MemoryJavaFileManager
        extends ForwardingJavaFileManager<StandardJavaFileManager>
    {
        private MemoryClassLoader memoryClassLoader;

        public MemoryJavaFileManager(JavaCompiler stdFileManager, MemoryClassLoader memoryClassLoader)
        {
            super(stdFileManager.getStandardFileManager(null, null, null));
            this.memoryClassLoader = memoryClassLoader;
        }

        public JavaFileObject getJavaFileForOutput(Location location, String s, JavaFileObject.Kind kind,
                                                   FileObject fileObject)
            throws IOException
        {
            assert kind == JavaFileObject.Kind.CLASS;
            return new MemoryJavaOutput(fileObject, s, memoryClassLoader.getClassMap());
        }

        public Iterable<? extends JavaFileObject> getJavaFileObjects(File... files)
        {
            return fileManager.getJavaFileObjects(files);
        }
    }
}
