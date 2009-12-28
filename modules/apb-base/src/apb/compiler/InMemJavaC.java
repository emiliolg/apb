

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import apb.BuildException;
import apb.Environment;

import apb.utils.FileUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.io.File.pathSeparator;
import static java.util.Collections.singleton;

/**
 * A class that allows invoking the 'javac' compiler and generating an 'in-memory' representations
 * of the compiled classes.
 * It also keeps a cache of already compiled classes by file name
 */
public class InMemJavaC
{
    //~ Instance fields ......................................................................................

    @NotNull private final Environment env;

    @NotNull private final JavaCompiler compiler;

    @NotNull private final Map<File, Class>      classesByFile;
    @NotNull private final MemoryClassLoader     memoryClassLoader;
    @NotNull private final MemoryJavaFileManager fileManager;

    //~ Constructors .........................................................................................

    /**
     * Creates the Compiler
     * @param environment
     */
    public InMemJavaC(@NotNull Environment environment)
    {
        env = environment;
        compiler = ToolProvider.getSystemJavaCompiler();

        try {
            memoryClassLoader =
                new MemoryClassLoader(FileUtils.toUrl(environment.getExtClassPath()),
                                      getClass().getClassLoader());
        }
        catch (MalformedURLException e) {
            throw new BuildException(e);
        }

        fileManager = new MemoryJavaFileManager(compiler, memoryClassLoader);
        classesByFile = new HashMap<File, Class>();
    }

    //~ Methods ..............................................................................................

    /**
     * Finds the given class in the cache
     *
     * @param clazz The class to find
     * @return a File pointing to that class or null if the class does not exist in the cache
     */
    @Nullable public File sourceFile(@NotNull Class clazz)
    {
        return memoryClassLoader.equals(clazz.getClassLoader())
               ? memoryClassLoader.sourceFile(clazz.getName()) : null;
    }

    /**
     * Compile the source in the specified File load it and return the associated class.
     * It also keeps a cache of already loaded classes
     * @param sourcePath The (optional) sourcePath where to find the source for he class
     * @param source the file to be compiled
     * @return The compiled class
     * @throws ClassNotFoundException if the compilation fails or the class cannot be loaded
     */
    @NotNull public Class<?> loadClass(@Nullable File sourcePath, @NotNull File source)
        throws ClassNotFoundException
    {
        Class<?> clazz = classesByFile.get(source);

        if (clazz == null) {
            clazz = compileToClass(sourcePath, source);
            classesByFile.put(source, clazz);
        }

        return clazz;
    }

    /**
     * Compile the source in the specified File and return the associated class
     * @param sourcePath The (optional) sourcePath where to find the source for he class
     * @param source the file to be compiled
     * @return The compiled class
     * @throws ClassNotFoundException if the compilation fails or the class cannot be loaded
     */
    @NotNull Class<?> compileToClass(@Nullable File sourcePath, @NotNull File source)
        throws ClassNotFoundException
    {
        String className = memoryClassLoader.classNameFromSource(source);

        if (className != null) {
            return memoryClassLoader.loadClass(className);
        }

        List<String> options = new ArrayList<String>();

        // Set the options apropiately
        // Sourcepath
        if (sourcePath != null) {
            options.add("-sourcepath");
            options.add(sourcePath.getAbsolutePath());
        }

        options.add("-implicit:class");

        String extClassPath = FileUtils.makePath(env.getExtClassPath());

        if (!extClassPath.isEmpty()) {
            options.add("-classpath");
            options.add(System.getProperty("java.class.path") + pathSeparator + extClassPath);
        }

        // Get the compilation task and invoke it
        boolean result =
            compiler.getTask(null, fileManager, null, options, null,
                             fileManager.getJavaFileObjects(singleton(source))).call();
        fileManager.close();

        if (!result) {
            throw new ClassNotFoundException("Compilation Error");
        }

        // If the compilation was successfull load the compiled class and return it

        return memoryClassLoader.getClassFromSource(source);
    }

    //~ Inner Classes ........................................................................................

    //    /**
    //     * Invoke the main Method over the compiled class
    //     * @param clazz The clazz to invoke main over
    //     * @param args  The arguments to pass to the main method
    //     */
    //    private static void invokeMain(Class<?> clazz, String[] args)
    //    {
    //        try {
    //            Method         m = clazz.getMethod("main", String[].class);
    //            final Object[] a = { args };
    //            m.invoke(null, a);
    //        }
    //        catch (NoSuchMethodException e) {
    //            throw new RuntimeException(e);
    //        }
    //        catch (InvocationTargetException e) {
    //            throw new RuntimeException(e);
    //        }
    //        catch (IllegalAccessException e) {
    //            throw new RuntimeException(e);
    //        }
    //    }

    /**
     * This class is an implementation of a {@link javax.tools.SimpleJavaFileObject} that stores the generated classes in
     * an in-memory classloader {@link MemoryClassLoader }
     */
    static class MemoryJavaOutput
        extends SimpleJavaFileObject
    {
        private final long              lastModified;
        private final MemoryClassLoader memoryClassLoader;
        private final String            className;

        /**
        * Constructs a MemoryJavaOutput instance
        * @param fileObject a {@link javax.tools.FileObject} file abstraction
        * @param className the name of the class
        * @param memoryClassLoader the in-memory Classloader
        */
        public MemoryJavaOutput(FileObject fileObject, String className, MemoryClassLoader memoryClassLoader)
        {
            super(fileObject.toUri(), Kind.CLASS);
            this.className = className;
            this.memoryClassLoader = memoryClassLoader;
            lastModified = fileObject.getLastModified();
        }

        /**
         * Gets the time the file object was last modified.  The time is
         * measured in milliseconds since the epoch (00:00:00 GMT, January
         * 1, 1970).
         *
         * @return the time the file object was last modified; or 0 if
         * the file object does not exist
         */
        @Override public long getLastModified()
        {
            return lastModified;
        }

        public OutputStream openOutputStream()
            throws IOException
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            memoryClassLoader.addClass(className, new ClassInfo(this, outputStream));
            return outputStream;
        }
    }

    /**
     * A simple data object structure that keeps track of the bytes of a class and when the corresponding source was modified
     */
    private static class ClassInfo
    {
        private final ByteArrayOutputStream bytes;
        private final MemoryJavaOutput      file;

        /**
        * Constructs a ClassInfo instance
        * @param fileObject a {@link javax.tools.FileObject} file abstraction
        * @param outputStream an output stream buffer
        */
        public ClassInfo(MemoryJavaOutput fileObject, ByteArrayOutputStream outputStream)
        {
            bytes = outputStream;
            file = fileObject;
        }

        public byte[] getBytes()
        {
            return bytes.toByteArray();
        }

        public String getPath()
        {
            return file.getName();
        }

        @Override public String toString()
        {
            return getPath();
        }
    }

    /**
     * An in-memory Classloader to load classes generated by the {@link MemoryJavaOutput}
     */
    private static class MemoryClassLoader
        extends URLClassLoader
    {
        @NotNull private final HashMap<String, ClassInfo> classMap;

        /**
        * Constructs a MemoryClassLoader instance for the given URLs
        * @param urls a {@link java.net.URL} array
        * @param parent the parent ClassLoader
        */
        MemoryClassLoader(URL[] urls, ClassLoader parent)
        {
            super(urls, parent);
            classMap = new HashMap<String, ClassInfo>();
        }

        /**
         * Adds a class to the class map
         * @param name the class name
         * @param classInfo
         */
        public void addClass(String name, ClassInfo classInfo)
        {
            classMap.put(name, classInfo);
        }

        protected Class<?> findClass(String className)
            throws ClassNotFoundException
        {
            ClassInfo classInfo = classMap.get(className);

            if (classInfo == null) {
                return super.findClass(className);
            }

            final byte[] bytes = classInfo.getBytes();
            return defineClass(className, bytes, 0, bytes.length);
        }

        /**
         * Returns the class that corresponds to a given source file
         * @param source The source file
         * @return The class that corresponds to the source file
         * @throws ClassNotFoundException If the class is not found
         */
        @NotNull Class<?> getClassFromSource(@NotNull File source)
            throws ClassNotFoundException
        {
            String className = classNameFromSource(source);

            if (className == null) {
                throw new ClassNotFoundException(source.getPath());
            }

            return loadClass(className);
        }

        /**
         * Returns the class name that corresponds to a given source file
         * @param source The source file
         * @return The classname that corresponds to the source file or null if the class was not found
         */
        @Nullable String classNameFromSource(@NotNull File source)
            throws ClassNotFoundException
        {
            String path = source.getPath();
            int    lastDot = path.lastIndexOf('.');

            if (lastDot != -1) {
                path = path.substring(0, lastDot);
            }

            path = path.replace(File.separatorChar, '.');

            for (String className : classMap.keySet()) {
                if (path.equals(className) || path.endsWith("." + className)) {
                    return className;
                }
            }

            return null;
        }

        @Nullable File sourceFile(String className)
        {
            ClassInfo classInfo = classMap.get(className);
            return classInfo == null ? null : new File(classInfo.getPath());
        }
    }

    /**
     * This class is an implementation of a {@link javax.tools.JavaFileManager} that stores the generated classes in
     * an in-memory classloader {@link MemoryClassLoader }
     */
    private static class MemoryJavaFileManager
        extends DefaultJavaFileManager
    {
        private final MemoryClassLoader memoryClassLoader;

        /**
        * Constructs a MemoryJavaFileManager instance
        * @param stdFileManager a {@link javax.tools.JavaCompiler}
        * @param memoryClassLoader the class loader used to load classes and resources
        */
        public MemoryJavaFileManager(JavaCompiler stdFileManager, MemoryClassLoader memoryClassLoader)
        {
            super(stdFileManager);
            this.memoryClassLoader = memoryClassLoader;
        }

        /**
         * Gets a {@linkplain JavaFileObject file object} for output
         * representing the specified class of the specified kind in the
         * given location.
         *
         * @param location a location
         * @param className the name of a class
         * @param kind the kind of file, must be one of {@link
         * JavaFileObject.Kind#SOURCE SOURCE} or {@link
         * JavaFileObject.Kind#CLASS CLASS}
         * @return a MemoryJavaOutput
         * @throws IOException if an I/O error occurred
         */
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind,
                                                   FileObject fileObject)
            throws IOException
        {
            assert kind == JavaFileObject.Kind.CLASS;
            return new MemoryJavaOutput(fileObject, className, memoryClassLoader);
        }
    }
}
