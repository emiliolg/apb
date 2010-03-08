
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
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import apb.Apb;
import apb.Environment;

import apb.utils.CollectionUtils;

import org.jetbrains.annotations.NotNull;

import static java.util.Arrays.asList;

import static apb.utils.CollectionUtils.filesFromBase;

public class CoreTasks
{
    //~ Methods ..............................................................................................

    /**
     * Copy a given file
     * @param from The File or Directory to copy from
     */
    @NotNull public static CopyTask.Builder copy(@NotNull String from)
    {
        return copy(Apb.getEnv().fileFromBase(from));
    }

    /**
     * Copy a given file
     * @param from The File or Directory to copy from
     */
    @NotNull public static CopyTask.Builder copy(@NotNull File from)
    {
        return new CopyTask.Builder(from);
    }

    /**
     * Copy one or more filesets to the given file
     * @param fileSets The FileSets to copy from
     * @see apb.tasks.FileSet
     */
    @NotNull public static CopyTask.Builder copy(@NotNull FileSet... fileSets)
    {
        return new CopyTask.Builder(fileSets);
    }

    /**
     * Copy a given file doing filtering (keyword expansion)
     * @param from The File or Directory to copy from
     */
    @NotNull public static FilterTask.Builder copyFiltering(@NotNull String from)
    {
        return copyFiltering(Apb.getEnv().fileFromBase(from));
    }

    /**
     * Copy a given file doing filtering (keyword expansion)
     * @param from The File or Directory to copy from
     */
    @NotNull public static FilterTask.Builder copyFiltering(@NotNull File from)
    {
        return new FilterTask.Builder(from);
    }

    /**
     * A convenience method to write a formatted string to the apb output as a log with INFO level.
     *
     * @param  format
     *         A format string as described in {@link java.util.Formatter}.
     *         Properties in the string will be expanded.
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.
     */
    public static void printf(String format, Object... args)
    {
        final Environment env = Apb.getEnv();
        env.logInfo(env.expand(format), args);
    }

    /**
     * Deletes a file or a directory
     * @param name The File or Directory to copy from
     *             Properties in the name will be expanded.
     */
    @NotNull public static DeleteTask delete(@NotNull String name)
    {
        return delete(Apb.getEnv().fileFromBase(name));
    }

    /**
     * Deletes a file or a directory
     * @param file The File or Directory to copy from
     */
    @NotNull public static DeleteTask delete(@NotNull File file)
    {
        return new DeleteTask(file);
    }

    /**
     * Delete one or more filesets
     * @param fileSets The FileSets to delete
     * @see apb.tasks.FileSet
     */
    @NotNull public static DeleteTask delete(@NotNull FileSet... fileSets)
    {
        return new DeleteTask(Arrays.asList(fileSets));
    }

    /**
     * Downloads a remote file
     * @param url The URL to download from
     */
    @NotNull public static DownloadTask.Builder download(@NotNull String url)
    {
        return new DownloadTask.Builder(url);
    }

    /**
     * Downloads a remote file
     * @param url The URL to download from
     */
    @NotNull public static DownloadTask.Builder download(@NotNull URL url)
    {
        return new DownloadTask.Builder(url);
    }

    /**
     * Executes a system command.
     * @param args The command to execute
     */
    @NotNull public static ExecTask exec(@NotNull String cmd, @NotNull String... args)
    {
        return exec(cmd, Arrays.asList(args));
    }

    /**
     * Executes a system command.
     * @param args The command to execute
     */
    @NotNull public static ExecTask exec(@NotNull String cmd, @NotNull List<String> args)
    {
        final Environment env = Apb.getEnv();
        return new ExecTask(env.expand(cmd), CollectionUtils.expandAll(env, args));
    }

    /**
     * Launches a java application
     * @param className The class file to launch
     * @param args Argument passed to the main function.
     */
    public static JavaTask java(@NotNull String className, @NotNull String... args)
    {
        return java(className, Arrays.asList(args));
    }

    /**
     * Launches a java application
     * @param className The class file to launch
     * @param args Argument passed to the main function.
     */
    public static JavaTask java(@NotNull String className, @NotNull List<String> args)
    {
        final Environment env = Apb.getEnv();
        return new JavaTask(false, className, CollectionUtils.expandAll(env, args));
    }

    /**
     * Launches a java application based on a jar file
     * @param jarName The jar to execute
     * @param args Argument passed to the main function.
     */
    public static JavaTask javaJar(@NotNull String jarName, @NotNull String... args)
    {
        return javaJar(jarName, Arrays.asList(args));
    }

    /**
     * Launches a java application
     * @param jarName The jar to execute
     * @param args Argument passed to the main function.
     */
    public static JavaTask javaJar(@NotNull String jarName, @NotNull List<String> args)
    {
        final Environment env = Apb.getEnv();
        return new JavaTask(true, jarName, CollectionUtils.expandAll(env, args));
    }

    /**
     * Compile the specified sources using javac
     * @param sourceDirectory The directory  to scan for sources
     */
    public static JavacTask.Builder javac(@NotNull String sourceDirectory)
    {
        return new JavacTask.Builder(sourceDirectory);
    }

    /**
     * Compile the specified sources using javac
     * @param fileSets the filesets defining the file to be compiled
     */
    public static JavacTask.Builder javac(@NotNull FileSet... fileSets)
    {
        return javac(asList(fileSets));
    }

    /**
     * Compile the specified sources using javac
     * @param fileSets the filesets defining the file to be compiled
     */
    public static JavacTask.Builder javac(@NotNull List<FileSet> fileSets)
    {
        return new JavacTask.Builder(fileSets);
    }

    /**
     * Create a jarfile
     * @param jarFileName  The jarfile to be created
     */
    public static JarTask.Builder jar(@NotNull String jarFileName)
    {
        return jar(Apb.getEnv().fileFromBase(jarFileName));
    }

    /**
     * Create a jarfile
     * @param jarFile  The jarfile to be created
     */
    public static JarTask.Builder jar(@NotNull File jarFile)
    {
        return new JarTask.Builder(jarFile);
    }

    /**
     * Create a web application
     * @param webAppDir  The directory where the web application will be created
     */
    public static WarTask.Builder war(@NotNull String webAppDir)
    {
        return war(Apb.getEnv().fileFromBase(webAppDir));
    }

    /**
     * Create a web application
     * @param webAppDir  The directory where the web application will be created
     */
    public static WarTask.Builder war(@NotNull File webAppDir)
    {
        return new WarTask.Builder(webAppDir);
    }

    /**
     * generate documentation for the specified source directories
     * @param sources The directories containing the source files to document
     */
    public static JavadocTask.Builder javadoc(final String... sources)
    {
        return javadoc(filesFromBase(Apb.getEnv(), sources));
    }

    /**
     * generate documentation for the specified source directories
     * @param sources The directories containing the source files to document
     */
    public static JavadocTask.Builder javadoc(final List<File> sources)
    {
        return new JavadocTask.Builder(sources);
    }

    /**
     * Creates a directory
     * @param name The Directory to be created
     *             Properties in the name will be expanded.
     */
    @NotNull public static MkdirTask mkdir(@NotNull String name)
    {
        return mkdir(Apb.getEnv().fileFromBase(name));
    }

    /**
     * CIdereates a directory
     * @param file The Directory to be created
     */
    @NotNull public static MkdirTask mkdir(@NotNull File file)
    {
        return new MkdirTask(file);
    }

    /**
     * Transform a file using XSLT
     * @param from The File or Directory to transform
     */
    @NotNull public static XsltTask.Builder xslt(@NotNull String from)
    {
        return xslt(Apb.getEnv().fileFromBase(from));
    }

    /**
     * Transform a file using XSLT
     * @param from The File or Directory to transform
     */
    @NotNull public static XsltTask.Builder xslt(@NotNull File from)
    {
        return new XsltTask.Builder(from);
    }

    /**
     * Transform one or more filesets to the given directory
     * @param fileSets The FileSets to transform
     * @see apb.tasks.FileSet
     */
    @NotNull public static XsltTask.Builder xslt(@NotNull FileSet... fileSets)
    {
        return new XsltTask.Builder(fileSets);
    }

    public static XjcTask xjc(@NotNull String... schemas)
    {
        final File[] files = new File[schemas.length];

        for (int i = 0, length = schemas.length; i < length; i++) {
            files[i] = Apb.getEnv().fileFromBase(schemas[i]);
        }

        return xjc(files);
    }

    public static XjcTask xjc(@NotNull File... schemas)
    {
        return new XjcTask(schemas);
    }
}
