

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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import static java.util.Collections.singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import javax.tools.FileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import apb.Environment;
import apb.utils.StandaloneEnv;
import apb.utils.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class that allows invoking 'javac' and generating an 'in-memory' representation
 * of the compiled classes.
 * It also keeps a cache of already compiled classes by file name
 */
public class InMemJavaC
{
    //~ Instance fields ......................................................................................

    @NotNull private final Map<File, Class> classesByFile;

    @NotNull private final JavaCompiler          compiler;
    @NotNull private final Environment           env;
    @NotNull private final MemoryJavaFileManager fileManager;
    @NotNull private final MemoryClassLoader     memoryClassLoader;

    //~ Constructors .........................................................................................

    /**
     * Creates the Compiler
     * @param environment
     */
    public InMemJavaC(@NotNull Environment environment) throws MalformedURLException {
        env = environment;
        compiler = ToolProvider.getSystemJavaCompiler();
        memoryClassLoader = new MemoryClassLoader(FileUtils.toUrl(environment.getExtClassPath()),
                getClass().getClassLoader());

        fileManager = new MemoryJavaFileManager(compiler, memoryClassLoader);
        classesByFile = new HashMap<File, Class>();
    }

    //~ Methods ..............................................................................................

    /**
     * Compile the file and invoke the main method of the compiled class
     * Mainly a test method.
     */
    public static void main(String[] args)
            throws ClassNotFoundException, MalformedURLException {
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

        InMemJavaC javac = new InMemJavaC(new StandaloneEnv());

        invokeMain(javac.compileToClass(null, source), args);
    }

    public long sourceLastModified(@NotNull Class clazz)
    {
        return memoryClassLoader.equals(clazz.getClassLoader())
               ? memoryClassLoader.sourceLastModified(clazz.getName()) : 0;
    }

    /**
     * Compile the source in the specified File and return the associated class
     * @param sourcePath The (optional) sourcePath where to find the source for he class
     * @param source the file to be compiled
     * @return The compiled class
     * @throws ClassNotFoundException if the compilation fails or the class cannot be loaded
     */
    @NotNull public Class<?> compileToClass(@Nullable File sourcePath, @NotNull File source)
        throws ClassNotFoundException
    {
        String className = memoryClassLoader.classNameFromSource(source);
        if (className != null)
            return memoryClassLoader.loadClass(className);

        List<String> options = new ArrayList<String>();
        // Set the options apropiately
        // Sourcepath
        if (sourcePath != null) {
            options.add("-sourcepath");
            options.add(sourcePath.getAbsolutePath());
        }

        String extClassPath = FileUtils.makePath(env.getExtClassPath());
        if (!extClassPath.isEmpty()) {
            options.add("-classpath");
            options.add(System.getProperty("java.class.path") + File.pathSeparator + extClassPath);
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

    /**
     * Compile the source in the specified File load it and return the associated class.
     * It also keeps a cache of already loaded classes
     * @param sourcePath The (optional) sourcePath where to find the source for he class
     * @param source the file to be compiled
     * @return The compiled class
     */
    @NotNull public Class<?> loadClass(@Nullable File sourcePath, @NotNull File source)
        throws ClassNotFoundException
    {
        Class<?> clazz = classesByFile.get(source);

        if (clazz == null) {
            env.logVerbose("Loading: %s\n", source);
            clazz = compileToClass(sourcePath, source);
            classesByFile.put(source, clazz);
        }

        return clazz;
    }

    /**
     * Invoke the main Method over the compiled class
     * @param clazz The clazz to invoke main over
     * @param args  The arguments to pass to the main method
     */
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

    /**
     * This class is an implementation of a {@link javax.tools.SimpleJavaFileObject} that stores the generated classes in
     * an in-memory classloader {@link MemoryClassLoader }
     */
    static class MemoryJavaOutput
        extends SimpleJavaFileObject
    {
        private String            className;
        private long              lastModified;
        private MemoryClassLoader memoryClassLoader;

        public MemoryJavaOutput(FileObject fileObject, String className, MemoryClassLoader memoryClassLoader)
        {
            super(fileObject.toUri(), Kind.CLASS);
            this.className = className;
            this.memoryClassLoader = memoryClassLoader;
            lastModified = fileObject.getLastModified();
        }

        public OutputStream openOutputStream()
            throws IOException
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            memoryClassLoader.addClass(className, outputStream, lastModified);
            return outputStream;
        }
    }

    /**
     * A simpe data object structure that keeps track of the bytes of a class and when the corresponding source was modified
     */
    private static class ClassInfo
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

    /**
     * An in-memory Classoader to load classes generated by the {@link MemoryJavaOutput}
     */
    private static class MemoryClassLoader
        extends URLClassLoader
    {
        @NotNull private final HashMap<String, ClassInfo> classMap;

        MemoryClassLoader(URL[] urls, ClassLoader parent)
        {
            super(urls, parent);
            classMap = new HashMap<String, ClassInfo>();
        }

        protected Class<?> findClass(String className)
            throws ClassNotFoundException
        {
            ClassInfo classInfo = classMap.get(className);

            if (classInfo == null) {
                return  super.findClass(className);
            }

            final byte[] bytes = classInfo.getBytes();
            return defineClass(className, bytes, 0, bytes.length);
        }

        /**
         * Return the class that corresponds to a given source file
         * @param source The source file
         * @return The class that corresponds to the source file
         * @throws ClassNotFoundException If the class is not found
         */
        @NotNull Class<?> getClassFromSource(@NotNull File source)
            throws ClassNotFoundException
        {
            String className = classNameFromSource(source);
            if (className == null)
                throw new ClassNotFoundException(source.getPath());
            return loadClass(className);
        }
        /**
         * Return the class name that corresponds to a given source file
         * @param source The source file
         * @return The classname that corresponds to the source file or null if the class was not found
         */
        @Nullable
        String classNameFromSource(@NotNull File source)
            throws ClassNotFoundException
        {
            String path = source.getPath();
            int    lastDot = path.lastIndexOf('.');

            if (lastDot != -1) {
                path = path.substring(0, lastDot);
            }

            path = path.replace(File.separatorChar, '.');

            for (String className : classMap.keySet()) {
                if (path.endsWith(className)) {
                    return className;
                }
            }

            return null;
        }

        void addClass(final String name, ByteArrayOutputStream outputStream, final long lastModified)
        {
            classMap.put(name, new ClassInfo(outputStream, lastModified));
        }

        long sourceLastModified(String className)
        {
            ClassInfo classInfo = classMap.get(className);
            return classInfo == null ? 0 : classInfo.lastModified;
        }
    }

    /**
     * This class is an implementation of a {@link javax.tools.JavaFileManager} that stores the generated classes in
     * an in-memory classloader {@link MemoryClassLoader }
     */
    private static class MemoryJavaFileManager
        extends DefaultJavaFileManager
    {
        private MemoryClassLoader memoryClassLoader;

        public MemoryJavaFileManager(JavaCompiler stdFileManager, MemoryClassLoader memoryClassLoader)
        {
            super(stdFileManager);
            this.memoryClassLoader = memoryClassLoader;
        }

        public JavaFileObject getJavaFileForOutput(Location location, String s, JavaFileObject.Kind kind,
                                                   FileObject fileObject)
            throws IOException
        {
            assert kind == JavaFileObject.Kind.CLASS;
            return new MemoryJavaOutput(fileObject, s, memoryClassLoader);
        }
    }
}
