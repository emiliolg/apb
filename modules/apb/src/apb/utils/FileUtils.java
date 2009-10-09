

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


package apb.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import apb.Apb;
import apb.BuildException;
import apb.Os;

import apb.tasks.FileSet;

import org.jetbrains.annotations.NotNull;

import static apb.utils.StringUtils.isEmpty;
import static apb.utils.StringUtils.isNotEmpty;
//
// User: emilio
// Date: Sep 8, 2008
// Time: 6:42:15 PM

//
public class FileUtils
{
    //~ Constructors .........................................................................................

    private FileUtils() {}

    //~ Methods ..............................................................................................

    public static Set<File> listDirsWithFiles(@NotNull File dir, @NotNull String ext)
    {
        Set<File> result = new TreeSet<File>();

        if (dir.exists()) {
            addDirsWithFiles(result, dir, ext);
        }

        return result;
    }

    public static List<File> listAllFilesWithExt(@NotNull File dir, @NotNull String ext)
    {
        List<File> result = new ArrayList<File>();

        if (dir.exists()) {
            addAll(result, dir, ext);
        }

        return result;
    }

    public static List<File> listAllFilesWithExt(@NotNull Collection<File> dirs, @NotNull String ext)
    {
        List<File> result = new ArrayList<File>();

        for (File dir : dirs) {
            if (dir != null && dir.exists()) {
                addAll(result, dir, ext);
            }
        }

        return result;
    }

    public static List<File> listAllFiles(@NotNull File dir)
    {
        List<File> result = new ArrayList<File>();

        if (dir.exists()) {
            addAll(result, dir, null);
        }

        return result;
    }

    /** Check that all files in the directory are older than
    * the timestamp specified as a parameter
    * @param dir The directory where to check files
    * @param timestamp The reference timestamp
    * @return true if all files are older thatn the specified timestamp, false otherwise
    */
    public static boolean uptodate(@NotNull File dir, long timestamp)
    {
        return uptodate(dir, "", timestamp);
    }

    /**
     * Check that all files in the directory finishing with the given extension are older than
     * the timestamp specified as a parameter
     * @param dir The directory where to check files
     * @param ext The extension to be checked to verify that files should be included in the check
     * @param timestamp The reference timestamp
     * @return true if all files are older thatn the specified timestamp, false otherwise
     */
    public static boolean uptodate(@NotNull File dir, String ext, long timestamp)
    {
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    if (!uptodate(file, ext, timestamp)) {
                        return false;
                    }
                }
                else if (isEmpty(ext) || file.getName().endsWith(ext)) {
                    if (file.lastModified() > timestamp) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Change a filename extension to a new one
     * @param file
     *@param ext the new extension  @return the filename with a new extension
     */
    @NotNull public static File changeExtension(@NotNull File file, @NotNull String ext)
    {
        String name = file.getName();
        int    dot = name.lastIndexOf('.');
        String baseName = dot == -1 ? name : name.substring(0, dot);
        return new File(file.getParentFile(), baseName + (ext.charAt(0) == '.' ? ext : '.' + ext));
    }

    public static List<File> removePrefix(List<File> filePrefixes, List<File> files)
    {
        List<String> prefixes = absolutePaths(filePrefixes);

        List<File> result = new ArrayList<File>();

        for (File file : files) {
            String path = file.getAbsolutePath();

            for (String prefix : prefixes) {
                if (path.startsWith(prefix)) {
                    result.add(new File(path.substring(prefix.length() + 1)));
                    break;
                }
            }
        }

        return result;
    }

    public static String removePrefix(File filePrefix, File file)
    {
        String prefix = filePrefix.getAbsolutePath();

        String path = file.getAbsolutePath();

        if (!path.startsWith(prefix)) {
            throw new IllegalStateException();
        }

        return path.substring(prefix.length() + 1);
    }

    public static String makePath(File... files)
    {
        return makePath(Arrays.asList(files));
    }

    public static String makePath(Collection<File> files)
    {
        return makePath(unique(files), File.pathSeparator);
    }

    public static String makePath(Collection<File> files, String pathSeparator)
    {
        StringBuilder result = new StringBuilder();

        for (File file : files) {
            if (result.length() != 0) {
                result.append(pathSeparator);
            }

            result.append(file.getPath());
        }

        return result.toString();
    }

    public static String makePathFromStrings(Collection<String> files)
    {
        return makePathFromStrings(files, File.pathSeparator);
    }

    public static String makePathFromStrings(Collection<String> files, final String pathSeparator)
    {
        StringBuilder result = new StringBuilder();

        for (String file : files) {
            if (result.length() != 0) {
                result.append(pathSeparator);
            }

            result.append(file);
        }

        return result.toString();
    }

    @NotNull public static File makeRelative(@NotNull File baseDir, @NotNull File file)
    {
        List<String> base = getAbsoluteParts(baseDir);
        List<String> f = getAbsoluteParts(file);
        int          i = 0;

        while (i < base.size() && i < f.size()) {
            if (!base.get(i).equals(f.get(i))) {
                break;
            }

            i++;
        }

        File result = null;

        for (int j = i; j < base.size(); j++) {
            result = new File(result, "..");
        }

        for (int j = i; j < f.size(); j++) {
            result = new File(result, f.get(j));
        }

        return result == null ? new File(".") : result;
    }

    public static List<String> getAbsoluteParts(File file)
    {
        final List<String> parts = getParts(file.getAbsoluteFile());
        final List<String> result = new ArrayList<String>();

        for (String s : parts) {
            if ("..".equals(s)) {
                // remove last
                result.remove(result.size() - 1);
            }
            else {
                result.add(s);
            }
        }

        return result;
    }

    public static String makeRelative(@NotNull File baseDir, @NotNull String filePath)
    {
        return makeRelative(baseDir, new File(filePath)).getPath();
    }

    public static List<String> getParts(final File file)
    {
        LinkedList<String> result = new LinkedList<String>();

        for (File f = file; f != null; f = f.getParentFile()) {
            final String name = f.getName();
            result.addFirst(name.isEmpty() ? "/" : name);
        }

        return result;
    }

    /**
     * Returns the extension portion of a file specification string.
     * This everything after the last dot '.' in the filename (NOT including
     * the dot). If not dot it returns the empty String
     * @param name the filename
     * @return The extension portion of it
     */
    public static String extension(File name)
    {
        String nm = name.getName();
        int    lastDot = nm.lastIndexOf('.');
        return lastDot == -1 ? "" : nm.substring(lastDot + 1);
    }

    public static String removeExtension(File name)
    {
        String nm = name.getName();
        int    lastDot = nm.lastIndexOf('.');
        return lastDot == -1 ? nm : nm.substring(0, lastDot);
    }

    public static void copyFileFiltering(@NotNull File from, @NotNull File to, boolean append,
                                         @NotNull String encoding, @NotNull List<Filter> filters,
                                         List<String> linesToInsert, List<String> linesToAppend)
        throws IOException
    {
        BufferedReader reader = null;
        PrintWriter    writer = null;

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(from), encoding));
            writer = new PrintWriter(new OutputStreamWriter(createOutputStream(to, append), encoding));

            for (String s : linesToInsert) {
                writer.println(s);
            }

            String line;

            while ((line = reader.readLine()) != null) {
                for (Filter filter : filters) {
                    line = filter.filter(line);
                }

                writer.println(line);
            }

            for (String s : linesToAppend) {
                writer.println(s);
            }
        }
        finally {
            close(reader);
            close(writer);
        }
    }

    public static void copyFile(@NotNull File from, @NotNull File to, boolean append)
        throws IOException
    {
        FileInputStream  in = null;
        FileOutputStream out = null;

        try {
            out = createOutputStream(to, append);

            in = new FileInputStream(from);

            FileChannel readChannel = in.getChannel();
            FileChannel writeChannel = out.getChannel();

            long size = readChannel.size();

            for (long position = 0; position < size;) {
                position += readChannel.transferTo(position, MB, writeChannel);
            }

            if (from.length() != to.length()) {
                throw new IOException("Failed to copy full contents from " + from + " to " + to);
            }
        }
        finally {
            close(in);
            close(out);
        }
    }

    /**
     * Create a FileOutputStream, creates the intermediate directories if necessary
     * @param file The file to open
     * @return A FileOutputStream
     * @throws FileNotFoundException
     */
    public static FileOutputStream createOutputStream(File file)
        throws FileNotFoundException
    {
        return createOutputStream(file, false);
    }

    /**
     * Create a FileOutputStream, creates the intermediate directories if necessary
     * @param file The file to open
     * @param append append to the output file
     * @return A FileOutputStream
     * @throws FileNotFoundException
     */
    public static FileOutputStream createOutputStream(File file, boolean append)
        throws FileNotFoundException
    {
        final File parentFile = file.getParentFile();

        parentFile.mkdirs();

        try {
            return new FileOutputStream(file, append);
        }
        catch (FileNotFoundException e) {
            if (!append && file.exists()) {
                final BuildException be = new BuildException("Can not recreate: '" + file + "'.");
                be.initCause(e);
                throw be;
            }

            throw e;
        }
    }

    /**
     * Create a FileWriter, creates the intermediate directories if necessary
     * @param file The file to open
     * @return A FileWriter
     * @throws IOException
     */
    public static FileWriter createWriter(File file)
        throws IOException
    {
        final File parentFile = file.getParentFile();

        parentFile.mkdirs();

        return new FileWriter(file);
    }

    public static void copyFile(@NotNull InputStream from, @NotNull File to)
        throws IOException
    {
        FileOutputStream writer = null;

        try {
            writer = createOutputStream(to);

            byte[] buffer = new byte[4092];

            int n;

            while ((n = from.read(buffer)) > 0) {
                writer.write(buffer, 0, n);
            }
        }
        finally {
            close(writer);
        }
    }

    public static void validateDirectory(File dir)
    {
        String msg = validateDir(dir);

        if (isNotEmpty(msg)) {
            throw new BuildException(msg);
        }
    }

    public static String validateDir(File dir)
    {
        String msg = "";

        if (!dir.isDirectory()) {
            if (dir.exists()) {
                msg = dir + " is not a directory.";
            }
            else if (!dir.mkdirs()) {
                msg = "Cannot create directory: " + dir;
            }
        }

        return msg;
    }

    /**
     * List all Java Sources under a given set of directories
     * Return the RELATIVE file name
     * @param sourceDirs The directories that can contain java sources
     * @return The relative list of file names
     */
    public static List<File> listJavaSources(Collection<File> sourceDirs)
    {
        List<File> result = new ArrayList<File>();
        Set<File>  files = new HashSet<File>();

        for (File dir : sourceDirs) {
            if (dir != null && dir.exists()) {
                List<File> abs = new ArrayList<File>();
                addAll(abs, dir, JAVA_EXT);

                for (File ab : abs) {
                    if (!alreadyProcesses(ab, files)) {
                        result.add(makeRelative(dir, ab));
                    }
                }
            }
        }

        return result;
    }

    public static void close(Closeable closeable)
    {
        if (closeable != null) {
            try {
                closeable.close();
            }
            catch (IOException e) {
                // Ignore
            }
        }
    }

    @NotNull public static URL[] toURLArray(@NotNull Iterable<File> urls)
        throws MalformedURLException
    {
        final Set<File> set = new HashSet<File>();

        for (File url : urls) {
            if (url != null) {
                set.add(normalizeFile(url));
            }
        }

        return toUrl(set);
    }

    public static PrintStream nullOutputStream()
    {
        return new PrintStream(new OutputStream() {
                public void write(int b) {}
            });
    }

    public static String addExecutableExtension(String cmd)
    {
        final Os os = Os.getInstance();
        return os.isWindows() || os.isOs2() ? cmd + ".exe" : cmd;
    }

    public static String findJavaExecutable(@NotNull final String cmd)
    {
        final String javaCmd = addExecutableExtension(cmd);

        // Try with '$java.home/bin

        String result = findCmdInDir(new File(java_home), javaCmd);

        if (result == null) {
            // Try with '$java.home/../bin
            result = findCmdInDir(new File(java_home, ".."), javaCmd);
        }

        if (result == null) {
            // Try with environment JAVA_HOME
            if (JAVA_HOME == null) {
                Apb.getEnv().logInfo("JAVA_HOME environment variable not set.\n");
            }
            else {
                result = findCmdInDir(new File(JAVA_HOME), cmd);

                if (result == null) {
                    Apb.getEnv().logInfo("Invalid value for JAVA_HOME environment variable: %s\n", JAVA_HOME);
                }
            }
        }

        if (result == null) {
            Apb.getEnv().logInfo("Looking for '%s' in the PATH.\n", cmd);
            result = javaCmd;
        }

        return result;
    }

    public static String findCmdInDir(@NotNull File dir, @NotNull String javaCmd)
    {
        final File java = new File(new File(dir, "bin"), javaCmd);

        return java.exists() ? java.getPath() : null;
    }

    /**
     * Return the last modification time from a set of files
     * @param files The files to be analyzed
     * @return The latest modification time from the set
     */
    public static long lastModified(Iterable<File> files)
    {
        long result = Long.MIN_VALUE;

        for (File file : files) {
            result = Math.max(result, file.lastModified());
        }

        return result;
    }

    /**
     * Returns true if any of the files is newer than <code>targetTime</code>
     * @param files to iterate
     * @param targetTime threshold modification time
     * @return <code>true</code> is all files are older than <code>targetTime</code>, <code>false</code> otherwise
     */
    public static boolean uptodate(Iterable<File> files, long targetTime)
    {
        for (File file : files) {
            if (file.lastModified() > targetTime) {
                return false;
            }
        }

        return true;
    }

    public static String getCurrentWorkingDirectory()
    {
        return System.getProperty("user.dir");
    }

    public static URL[] toUrl(Collection<File> cp)
        throws MalformedURLException
    {
        final URL[] urls = new URL[cp.size()];

        int i = 0;

        for (File file : cp) {
            urls[i++] = file.toURI().toURL();
        }

        return urls;
    }

    /**
     * Makes file absolute and removes "." ".." segments
     * @param file File to be normalized
     * @return normalized file
     */
    public static String normalizePath(File file)
    {
        return normalizeFile(file).getPath();
    }

    /**
     * Makes file absolute and removes "." ".." segments
     * @param file File to be normalized
     * @return normalized file
     */
    public static File normalizeFile(File file)
    {
        final File normalized = new File(file.toURI().normalize());

        // if already normalized, return original file;
        return normalized.getPath().equals(file.getPath()) ? file : normalized;
    }

    /**
     * Return the apb home directory
     * @return the apb home directory
     */
    @NotNull public static File getApbDir()
    {
        return new File(System.getProperty("user.home"), APB_DIR);
    }

    @NotNull public static Properties userProperties()
    {
        File propFile = new File(getApbDir(), APB_PROPERTIES);

        Properties p = new Properties();

        try {
            p.load(new FileReader(propFile));
        }
        catch (IOException ignore) {
            // Ignore
        }

        return p;
    }

    public static boolean equalsContent(File file1, File file2)
    {
        final boolean present1 = file1.exists();
        final boolean present2 = file2.exists();

        if (!present1 && !present2) {
            return true;
        }

        if (present1 != present2) {
            return false;
        }

        try {
            FileInputStream i1 = new FileInputStream(file1);
            FileInputStream i2 = new FileInputStream(file2);
            byte[]          buff1 = new byte[1024];
            byte[]          buff2 = new byte[1024];
            int             n;

            while ((n = i1.read(buff1)) >= 0) {
                if (i2.read(buff2) != n) {
                    return false;
                }

                for (int i = 0; i < buff1.length; i++) {
                    if (buff1[i] != buff2[i]) {
                        return false;
                    }
                }
            }

            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    public static void touch(@NotNull File f, long time)
        throws IOException
    {
        if (!f.createNewFile() && f.isDirectory()) {
            for (File childFile : f.listFiles()) {
                touch(childFile, time);
            }
        }

        f.setLastModified(time);
    }

    /**
     * An utility method to list all files from a group of FileSets,
     * returning a map of each file mapped to a target directory
     * @param filesets The filesets to be listed
     * @param target The target directory to map the original file
     * @param checkTimestamp Wheter to check the timestamp of the target file before adding it to the map or not.print an info message if any fileset is empty
     */
    public static Map<File, File> listAllMappingToTarget(List<FileSet> filesets, File target,
                                                         boolean checkTimestamp)
    {
        Map<File, File> result = new LinkedHashMap<File, File>();

        for (FileSet fileset : filesets) {
            final List<String> fileNames = fileset.list();

            if (!fileNames.isEmpty()) {
                for (String f : fileNames) {
                    final File source = new File(fileset.getDir(), f);
                    final File dest = new File(target, f);

                    if (!checkTimestamp || !dest.exists() || source.lastModified() > dest.lastModified()) {
                        result.put(source, dest);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Find the innermost single directory inside the one specified.
     * For example, with the following directory layouts
     * <p><blockquote><pre>
     *     base1 --- a -- b +--- c1
     *                     |
     *                     +--- c2
     *
     *     base2 --- a +-- b +--- c1
     *                 |
     *                 +--- c2
     *
     *     base3 +--- a +-- b +--- c1
     *           |
     *           +--- c2
     *
     * </pre></blockquote><p>
     * It will return:
     * <p><blockquote><pre>
     *     topSingleDirectory(new File("base1")) --> "a/b"
     *     topSingleDirectory(new File("base2")) --> "a"
     *     topSingleDirectory(new File("base3")) --> ""
     * </pre></blockquote><p>
     *
     * @param baseDir The directory used as the base
     * @return The path to the inner most single directory starting from basDir
     */
    public static String topSingleDirectory(@NotNull File baseDir)
    {
        if (!baseDir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + baseDir);
        }

        String result = "";
        File[] inner;

        while ((inner = baseDir.listFiles()).length == 1 && inner[0].isDirectory()) {
            baseDir = inner[0];
            final String part = baseDir.getName();
            result = result.isEmpty() ? part : result + File.separator + part;
        }

        return result;
    }

    static boolean isSymbolicLink(File file)
    {
        try {
            return !file.getAbsolutePath().equals(file.getCanonicalPath());
        }
        catch (IOException e) {
            return true;
        }
    }

    private static List<File> unique(Collection<File> files)
    {
        final Set<File>  sets = new HashSet<File>(files.size());
        final List<File> result = new ArrayList<File>(files.size());

        for (File file : files) {
            if (sets.add(file)) {
                result.add(file);
            }
        }

        return result;
    }

    private static boolean alreadyProcesses(File f, Set<File> files)
    {
        File canonical = normalizeFile(f);

        boolean result = files.contains(canonical);

        if (!result) {
            files.add(canonical);
        }

        return result;
    }

    private static List<String> absolutePaths(List<File> sourceDirs)
    {
        List<String> sourcePrefixes = new ArrayList<String>();

        for (File sourceDir : sourceDirs) {
            sourcePrefixes.add(sourceDir.getAbsolutePath());
        }

        return sourcePrefixes;
    }

    private static void addAll(final List<File> files, File dir, final String ext)
    {
        dir.listFiles(new FileFilter() {
                public boolean accept(File file)
                {
                    if (file.isDirectory()) {
                        if (file.getName().charAt(0) != '.' && !file.isHidden()) {
                            addAll(files, file, ext);
                        }
                    }
                    else if (ext == null || file.getName().endsWith(ext)) {
                        files.add(file);
                    }

                    return true;
                }
            });
    }

    private static void addDirsWithFiles(final Set<File> files, File dir, final String ext)
    {
        dir.listFiles(new FileFilter() {
                public boolean accept(File file)
                {
                    if (file.isDirectory()) {
                        addDirsWithFiles(files, file, ext);
                    }
                    else if (ext == null || file.getName().endsWith(ext)) {
                        files.add(file.getParentFile());
                    }

                    return true;
                }
            });
    }

    //~ Static fields/initializers ...........................................................................

    public static final Comparator<File> FILE_COMPARATOR =
        new Comparator<File>() {
            @Override public int compare(File o1, File o2)
            {
                return o1 == o2
                       ? 0
                       : o1 == null
                         ? -1
                         : o2 == null ? 1  //
                                      : o1.equals(o2) ? 0  //
                                                      : o1.getPath().compareTo(o2.getPath());
            }
        };

    public static final String APB_PROPERTIES = "apb.properties";

    private static final String APB_DIR = ".apb";

    public static final String JAVA_HOME = System.getenv("JAVA_HOME");
    public static final String java_home = System.getProperty("java.home");

    public static final String JAVA_EXT = ".java";

    private static final int MB = 1024 * 1024;

    public static final List<String> DEFAULT_SRC_EXCLUDES = Arrays.asList(

                                                                          //Oracle ADE
                                                                          ".ade_path");

    public static final List<String> DEFAULT_EXCLUDES =
        Arrays.asList(

                      // Miscellaneous typical temporary files
                      "**/*~", "**/#*#", "**/.#*", "**/%*%", "**/._*",

                      // CVS
                      "**/CVS", "**/CVS/**", "**/.cvsignore",

                      // SCCS
                      "**/SCCS", "**/SCCS/**",

                      // Visual SourceSafe
                      "**/vssver.scc",

                      // Subversion
                      "**/.svn", "**/.svn/**",

                      // Oracle ADE
                      "**/.ade_path", "**/.ade_path/**",

                      // Arch
                      "**/.arch-ids", "**/.arch-ids/**",

                      //Bazaar
                      "**/.bzr", "**/.bzr/**",

                      //SurroundSCM
                      "**/.MySCMServerInfo",

                      // Mac
                      "**/.DS_Store");
}
