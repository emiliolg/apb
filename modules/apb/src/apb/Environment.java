

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


package apb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import apb.compiler.InMemJavaC;

import apb.index.ArtifactsCache;
import apb.index.DefinitionsIndex;

import apb.metadata.DependencyList;
import apb.metadata.Module;
import apb.metadata.ProjectElement;

import apb.utils.DebugOption;
import apb.utils.FileUtils;
import apb.utils.PropertyExpansor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.lang.Character.isJavaIdentifierPart;

import static apb.utils.FileUtils.JAVA_EXT;
import static apb.utils.StringUtils.isEmpty;
import static apb.utils.StringUtils.nChars;

/**
 * This class represents an Environment that includes common services like:
 * - Properties
 * - Options
 * - Logging
 * - Error handling
 */

public abstract class Environment
{
    //~ Instance fields ......................................................................................

    /**
     * The apb home directory
     */
    @NotNull private final File apbDir;
    private ArtifactsCache      artifactsCache;

    /**
     * The base directory of the current project
     */
    @Nullable private File basedir;

    /**
     * Base properties are enviroment or argument properties
     * They take preference over the properties defined in project elements
     */
    @NotNull private final Map<String, String> baseProperties;

    /**
     * The running time of the current build
     */
    private long clock;

    /**
     * The stack of executions
     */
    @NotNull private final LinkedList<Context> contextStack;

    /**
     * The current element being run
     */
    @Nullable private ProjectElementHelper currentElement;

    /**
     * The name of the current module being processed
     */
    @NotNull private String currentName;

    /**
     * Control what to show when logging
     */
    @NotNull private final EnumSet<DebugOption> debugOptions;

    /**
     * The index of definitions
     * It is initialized in a lazy way in the getter
     */
    @Nullable private DefinitionsIndex definitionsIndex;

    /**
     * The set of jars that comprise the extension class path
     */
    @NotNull private final Set<File> extClassPath;
    private boolean                  failOnError;
    private boolean                  forceBuild;

    /**
     * A Map that contains all constructed Helpers
     */
    private final Map<String, ProjectElementHelper> helpersByElement;
    private InMemJavaC                              javac;
    private boolean                                 nonRecursive;

    /**
     * An abstract representation of the Operating System
     */
    @NotNull private final Os os;

    @NotNull private final Set<File> projectPath;

    /**
     * Project properties are the properties defined in project elements
     */
    @NotNull private final Map<String, String> projectProperties;

    /**
     * The directory where the project definition files are stored
     */
    @Nullable private File projectsHome;

    /**
     * Processing and messaging options
     */
    private boolean quiet;
    private boolean showStackTrace;

    //~ Constructors .........................................................................................

    /**
     * Crate an Environment
     */
    public Environment()
    {
        apbDir = new File(System.getProperty("user.home"), APB_DIR);
        os = Os.getInstance();
        baseProperties = new TreeMap<String, String>();
        projectProperties = new TreeMap<String, String>();
        currentName = "";
        contextStack = new LinkedList<Context>();

        helpersByElement = new HashMap<String, ProjectElementHelper>();
        artifactsCache = new ArtifactsCache(this);

        // Read Environment
        //        for (Map.Entry<String,String> entry : System.getenv().entrySet()) {
        //            baseProperties.put(entry.getKey(), entry.getValue());
        //        }
        loadUserProperties();

        // Read System Properties
        copyProperties(System.getProperties());

        extClassPath = loadExtensionsPath(baseProperties);
        resetJavac();
        projectPath = new LinkedHashSet<File>();
        debugOptions = EnumSet.noneOf(DebugOption.class);

        // Assign the singleton
        environment = this;
    }

    //~ Methods ..............................................................................................

    /**
     * Log items with INFO Level using the specified format string and
     * arguments.
     *
     * @param  msg
     *         A <a href="../util/Formatter.html#syntax">format string</a>
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     */
    public abstract void logInfo(String msg, Object... args);

    /**
     * Log items with WARNING Level using the specified format string and
     * arguments.
     *
     * @param  msg
     *         A <a href="../util/Formatter.html#syntax">format string</a>
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     */
    public abstract void logWarning(String msg, Object... args);

    /**
     * Log items with SEVERE Level using the specified format string and
     * arguments.
     *
     * @param  msg
     *         A <a href="../util/Formatter.html#syntax">format string</a>
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     */
    public abstract void logSevere(String msg, Object... args);

    /**
     * Log items with VERBOSE Level using the specified format string and
     * arguments.
     *
     * @param  msg
     *         A <a href="../util/Formatter.html#syntax">format string</a>
     *
     * @param  args
     *         Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     */
    public abstract void logVerbose(String msg, Object... args);

    /**
     * Get an instance of the current Environment
     * @return An instance of the current Environment
     */
    @NotNull public static Environment getInstance()
    {
        if (environment == null) {
            throw new IllegalStateException("Environment not initialized");
        }

        return environment;
    }

    /**
     * Get the Extension Jars to be searched when we compile definitions
     * @return the extension Jars to be searched when we compiled definitions
     */
    @NotNull public Collection<File> getExtClassPath()
    {
        return extClassPath;
    }

    /**
     * Returns the time that the source file correspondong to this class was
     * last modified.
     *
     * @param clazz The class whose source file we want to check
     *
     * @return  A <code>long</code> value representing the time the file was
     *          last modified, measured in milliseconds since the epoch, or <code>0L</code> if the
     *          file does not exist or if an error occurs
     */
    public long sourceLastModified(@NotNull Class clazz)
    {
        File f = sourceFile(clazz);
        return f == null ? 0 : f.lastModified();
    }

    /**
     * Returns the source file correspondong to this class
     *
     * @param clazz The class whose source file we want
     *
     * @return  A <code>File</code>  corresponding to the source file of this class
     *          or <code>null</code> if the file does not exist or if an error occurs
     */
    @Nullable public File sourceFile(@NotNull Class clazz)
    {
        return javac == null ? null : javac.sourceFile(clazz);
    }

    /**
     * Handle an Error. It creates a build Exception with the specified msg.
     * And delegates the handling to {@link #handle(Throwable t)}
     * @param msg The message used to create the build exception
     */
    public void handle(@NotNull String msg)
    {
        handle(new BuildException(msg));
    }

    /**
     * Handle an Error.
     * Either raise the exception or log it depending on the value of the failOnError flag
     * @param e The Exception causing the failure
     */
    public void handle(@NotNull Throwable e)
    {
        if (failOnError) {
            throw (e instanceof BuildException) ? (BuildException) e : new BuildException(e);
        }

        logSevere(e.getMessage());
    }

    /**
     * Abort the build, displaying a message.
     * @param msg Message to be displayed when aborting.
     */
    public void abort(String msg)
    {
        logInfo(msg);
        System.exit(1);
    }

    /**
     * Sets the flags that marks wheter to fail when an exception is raised or try to continue the
     * build
     * @param b
     */
    public void setFailOnError(boolean b)
    {
        failOnError = b;
    }

    /**
     * Returns true if we want the build to proceed unconditionally without checking file timestamps
     * @return true if we want the build to proceed unconditionally without checking file timestamps
     */
    public boolean forceBuild()
    {
        return forceBuild;
    }

    /**
     * Sets the flags that marks to ignore file timestamps and build everything
     * @param b
     */
    public void setForceBuild(boolean b)
    {
        forceBuild = b;
    }

    /**
     * Returns true if log level is verbose
     * @return true if log level is verbose
     */
    public boolean isVerbose()
    {
        return !debugOptions.isEmpty();
    }

    /**
     * Returns true if must show the following option
     */
    public boolean mustShow(DebugOption option)
    {
        return debugOptions.contains(option);
    }

    /**
     * Returns true if log level is quiet
     * @return true if log level is quiet
     */
    public boolean isQuiet()
    {
        return quiet;
    }

    /**
     * Log with the lowest level
     */
    public void setQuiet()
    {
        quiet = true;
    }

    /**
     * Returns true if the stacktrace of the offending exception must be shown when aborting
     */
    public boolean showStackTrace()
    {
        return showStackTrace;
    }

    /**
     * Marks wheter to show or not the stacktrace of the offending exception when aborting
     */
    public void setShowStackTrace()
    {
        showStackTrace = true;
    }

    /**
     * Get the name of the current module/project being processed
     */
    @NotNull public String getCurrentName()
    {
        return currentName;
    }

    /**
     * Returns true if the build must NOT proceed recursive to the module dependecies
     */
    public boolean isNonRecursive()
    {
        return nonRecursive;
    }

    /**
     * Avoid APB to recursively invoke the target in each of the module dependencies
     */
    public void setNonRecursive()
    {
        nonRecursive = true;
    }

    /**
     * Get the running time of the current build
     */
    public long getClock()
    {
        return clock;
    }

    /**
     * Returns The directory where the project definition files are stored
     */
    @NotNull public File getProjectsHome()
    {
        if (projectsHome == null) {
            throw new IllegalStateException("Projects Home variable not initialized");
        }

        return projectsHome;
    }

    /**
     * Get the base directory of the current Module
     * @return the base directory of the current Module
     * @throws IllegalStateException If there is no current module
     */
    @NotNull public File getBaseDir()
    {
        if (basedir == null) {
            throw new IllegalStateException("Not current Module");
        }

        return basedir;
    }

    /**
     * Get current module output directory
     * @return current module output directory
     * @throws IllegalStateException If there is no current module
     */
    @NotNull public File getOutputDir()
    {
        return getModuleHelper().getOutput();
    }

    /**
     * Get the current command being run or null if no one
     */
    @NotNull public String getCurrentCommand()
    {
        return contextStack.isEmpty() ? "" : contextStack.getLast().getCommand();
    }

    /**
     * Returns a representation of the current Operating System
     */
    @NotNull public Os getOs()
    {
        return os;
    }

    /**
     * Return the value of the specified property
     * @param id The property to search
     * @return The value of the property or the empty String if the Property is not found and failOnError is not set
     */
    @NotNull public String getProperty(@NotNull String id)
    {
        if (!hasProperty(id)) {
            handle(new PropertyException(id));
            return "";
        }

        String result = baseProperties.get(id);
        return result != null ? result : projectProperties.get(id);
    }

    /**
     * Return the value of the specified property
     * @param id The property to search
     * @param defaultValue The default value to return in case the property is not set
     * @return The value of the property
     */
    @NotNull public String getProperty(@NotNull String id, @NotNull String defaultValue)
    {
        String result = baseProperties.get(id);
        return result != null ? result : (result = projectProperties.get(id)) != null ? result : defaultValue;
    }

    public boolean hasProperty(@NotNull String id)
    {
        return baseProperties.containsKey(id) || projectProperties.containsKey(id);
    }

    /**
     * Return the value of the specified boolean property
     * @param id The property to search
     * @param defaultValue The default value
     * @return The value of the property or false if the property is not set
     */
    public boolean getBooleanProperty(@NotNull String id, boolean defaultValue)
    {
        return Boolean.parseBoolean(getProperty(id, Boolean.toString(defaultValue)));
    }

    /**
     * Process the string expanding property values.
     * The `$' character introduces property expansion.
     * The property  name  or  symbol  to  be expanded  may be enclosed in braces,
     * which are optional but serve to protect the variable to be expanded from characters
     * immediately following it which could be interpreted as part of the name.
     * When braces are used, the matching ending brace is the first `}' not escaped by a backslash
     *
     * @param string The string to be expanded.
     * @return An String with properties expanded.
     */
    @NotNull public String expand(@Nullable String string)
    {
        if (isEmpty(string)) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        StringBuilder id = new StringBuilder();

        boolean insideId = false;
        boolean closeWithBrace = false;

        for (int i = 0; i < string.length(); i++) {
            char chr = string.charAt(i);

            if (insideId) {
                insideId =
                    closeWithBrace ? chr != '}' : isJavaIdentifierPart(chr) || chr == '.' || chr == '-';

                if (insideId) {
                    id.append(chr);
                }
                else {
                    result.append(getProperty(id.toString()));
                    id.setLength(0);

                    if (!closeWithBrace) {
                        result.append(chr);
                    }
                }
            }
            else if (chr == '$') {
                insideId = true;

                if (i + 1 < string.length() && string.charAt(i + 1) == '{') {
                    i++;
                    closeWithBrace = true;
                }
            }
            else if (chr == '\\' && i + 1 < string.length() && string.charAt(i + 1) == '$') {
                result.append('$');
                i++;
            }
            else {
                result.append(chr);
            }
        }

        if (insideId) {
            result.append(getProperty(id.toString()));
        }

        return result.toString();
    }

    /**
     * Get (Constructing it if necessary) the helper for a given element
     * A Helper is an Object that extends the functionality of a given ProjectElement
     * @param element The Project Element to get the helper from
     * @return The helper for the given element
     */
    @NotNull public ProjectElementHelper getHelper(@NotNull ProjectElement element)
    {
        synchronized (helpersByElement) {
            ProjectElementHelper result = helpersByElement.get(element.getName());

            if (result == null) {
                result = ProjectElementHelper.create(element, this);
            }

            return result;
        }
    }

    /**
     * Returns a File object whose path is relative to the basedir
     * @param name The (Usually relative to the basedir) file name.
     * @return A file whose path is relative to the basedir.
     */
    @NotNull public File fileFromBase(@NotNull String name)
    {
        final File child = new File(expand(name));
        return FileUtils.normalizeFile(child.isAbsolute() ? child : new File(basedir, child.getPath()));
    }

    /**
     * Returns a File object whose path is relative to the source directory of the current module
     * @param name The (Usually relative to the source directory of the module) file name.
     * @return A file whose path is relative to the source directory of the current module.
     */
    @NotNull public File fileFromSource(@NotNull String name)
    {
        final File child = new File(expand(name));
        return child.isAbsolute() ? child : new File(getModuleHelper().getSource(), child.getPath());
    }

    /**
     * Returns a File object whose path is relative to the generated source directory of the current module
     * @param name The (Usually relative to the generated source directory of the module) file name.
     * @return A file whose path is relative to the generated source directory of the current module.
     */
    @NotNull public File fileFromGeneratedSource(@NotNull String name)
    {
        final File child = new File(expand(name));
        return child.isAbsolute() ? child : new File(getModuleHelper().getGeneratedSource(), child.getPath());
    }

    @NotNull public ProjectElement activate(@NotNull ProjectElement element)
    {
        final ProjectElementHelper helper = getHelper(element);
        currentElement = helper;
        currentName = helper.getName();

        // @todo this can be optimize if currentElement.element != null
        // but I'll need to reset the properties in env
        final ProjectElement result = new PropertyExpansor(this).expand(element);

        basedir = new File(result.basedir);
        helper.activate(result);
        return result;
    }

    /**
     * Return current ModuleHelper
     * @return current Module Helper
     */
    @NotNull public ModuleHelper getModuleHelper()
    {
        if (currentElement == null || !(currentElement instanceof ModuleHelper)) {
            throw new IllegalStateException("Not current Module");
        }

        return (ModuleHelper) currentElement;
    }

    /**
     * Return current TestModuleHelper
     * @return current TestModule Helper
     */
    @NotNull public TestModuleHelper getTestModuleHelper()
    {
        if (currentElement == null || !(currentElement instanceof TestModuleHelper)) {
            throw new IllegalStateException("Not current Module");
        }

        return (TestModuleHelper) currentElement;
    }

    /**
     * Return current ModuleHelper
     * @return current Module Helper
     */
    @NotNull public ProjectHelper getProjectHelper()
    {
        if (currentElement == null || !(currentElement instanceof ProjectHelper)) {
            throw new IllegalStateException("Not current Project");
        }

        return (ProjectHelper) currentElement;
    }

    public ProjectElementHelper getCurrent()
    {
        return currentElement;
    }

    @NotNull public Set<File> getProjectPath()
    {
        return projectPath;
    }

    public void forward(@NotNull String command, Iterable<? extends Module> modules)
    {
        for (Module module : modules) {
            getHelper(module).build(command);
        }
    }

    public File applicationJarFile()
    {
        String url = getClass().getResource("").toExternalForm();
        int    ind = url.lastIndexOf('!');

        if (ind == -1 || !url.startsWith(JAR_FILE_URL_PREFIX)) {
            handle("Can't not find 'apb' jar " + url);
        }

        return new File(url.substring(JAR_FILE_URL_PREFIX.length(), ind));
    }

    public void putProperty(String name, String value)
    {
        if (mustShow(DebugOption.PROPERTIES)) {
            logVerbose("property %s=%s\n", name, value);
        }

        projectProperties.put(name, value);
    }

    public void setProperties(Map<String, String> values)
    {
        System.out.println("values = " + values);
    }

    public String getBaseProperty(String propertyName)
    {
        return baseProperties.get(propertyName);
    }

    /**
     * Construct a Helper for the file
     * @param file The file containing the module definition
     * @return A project helper asociated to the file
     */
    @NotNull public ProjectElementHelper constructProjectElement(File file)
        throws Throwable
    {
        final File pdir = projectDir(file);
        projectsHome = pdir;
        putProperty(PROJECTS_HOME_PROP_KEY, pdir.getAbsolutePath());

        final ProjectElement element;

        try {
            element = javac.loadClass(pdir, file).asSubclass(ProjectElement.class).newInstance();
        }
        catch (ExceptionInInitializerError e) {
            throw e.getException();
        }

        currentName = element.getName();
        return getHelper(element);
    }

    /**
     * Return the apb home directory
     * @return the apb home directory
     */
    @NotNull public File getApbDir()
    {
        return apbDir;
    }

    /**
     * Return (Initializing it if necessary) an instance of the DefinitionsIndex
     * that contains informatio avoid the modules in th eproject path
     * @return The DefinitionIndex
     */
    @NotNull public synchronized DefinitionsIndex getDefinitionsIndex()
    {
        if (definitionsIndex == null) {
            definitionsIndex = new DefinitionsIndex(this);
        }

        return definitionsIndex;
    }

    /**
     * Return an instance of the ArtifactsCache
     * that contains information avoid library Artifacts.
     * @return The ArtifactCache
     */
    @NotNull public ArtifactsCache getArtifactsCache()
    {
        return artifactsCache;
    }

    /**
     * Reset the state of the compiler.
     * This should NOT be a public method
     */
    public void resetJavac()
    {
        try {
            javac = new InMemJavaC(this);
        }
        catch (MalformedURLException e) {
            handle(e);
        }
    }

    public void setDebugOptions(@NotNull EnumSet<DebugOption> options)
    {
        debugOptions.addAll(options);

        if (!options.isEmpty()) {
            setVerbose();
        }
    }

    public String makeStandardHeader()
    {
        StringBuilder result = new StringBuilder();

        if (mustShow(DebugOption.TRACK)) {
            result.append(nChars(contextStack.size() * 4, ' '));
        }

        final String current = getCurrentName();

        if (!current.isEmpty()) {
            int n = result.length();
            result.append('[');
            result.append(current);

            final String cmd = getCurrentCommand();

            if (!cmd.isEmpty()) {
                result.append('.');
                result.append(cmd);
            }

            int maxLength = HEADER_LENGTH + n;

            if (result.length() > maxLength) {
                result.setLength(maxLength);
            }

            result.append(']');
            n = result.length() - n;

            for (int i = HEADER_LENGTH + 1 - n; i >= 0; i--) {
                result.append(' ');
            }
        }

        return result.toString();
    }

    protected void setVerbose() {}

    /**
     * Do all initializations that needs properties already initialized
     */
    protected void postInit()
    {
        loadProjectPath();
        initProxies();
    }

    protected void copyProperties(Map<?, ?> p)
    {
        for (Map.Entry<?, ?> entry : p.entrySet()) {
            baseProperties.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
    }

    void resetClock()
    {
        clock = System.currentTimeMillis();
    }

    @NotNull File searchInProjectPath(String projectElement)
        throws FileNotFoundException
    {
        for (File dir : getProjectPath()) {
            File file = new File(dir, projectElement);

            if (file.exists()) {
                return file;
            }
        }

        throw new FileNotFoundException(projectElement);
    }

    @NotNull File projectElementFile(String projectElement)
        throws IOException
    {
        // Strip JAVA_EXT
        if (projectElement.endsWith(JAVA_EXT)) {
            projectElement = projectElement.substring(0, projectElement.length() - JAVA_EXT.length());
        }

        File f = new File(projectElement);

        // Replace 'dots' by file separators BUT ONLY IN THE FILE NAME.
        File file = new File(f.getParentFile(), f.getName().replace('.', File.separatorChar) + JAVA_EXT);

        return file.exists() ? file : searchInProjectPath(file.getPath());
    }

    File projectDir(File projectElementFile)
    {
        File   parent = projectElementFile.getAbsoluteFile().getParentFile();
        String p = parent.getPath();

        while (p != null) {
            final File file = new File(p);

            for (File pathElement : getProjectPath()) {
                if (file.equals(pathElement)) {
                    return file;
                }
            }

            p = file.getParent();
        }

        // If the project element file is not located into the projects home just return the
        // containing directory of it
        return parent;
    }

    /**
     * Construct a Helper for the element
     * @param name The module or project name
     * @return
     */
    @NotNull ProjectElementHelper constructProjectElement(String name)
        throws DefinitionException
    {
        try {
            File source = projectElementFile(name);
            return constructProjectElement(source);
        }
        catch (DependencyList.NullDependencyException e) {
            throw new DefinitionException(name, e);
        }
        catch (Throwable e) {
            throw new DefinitionException(name, e);
        }
    }

    /**
     * Return the map of helpers by element.
     * This is a package only method
     */
    Map<String, ProjectElementHelper> getHelpers()
    {
        return helpersByElement;
    }

    void startExecution(@NotNull final String name, @NotNull String command)
    {
        contextStack.add(new Context(name, command));

        if (mustShow(DebugOption.TRACK)) {
            currentName = name;
            logVerbose("About to execute '%s.%s'\n", name, command);
        }
    }

    void endExecution(@NotNull final String name, @NotNull String command)
    {
        if (mustShow(DebugOption.TRACK)) {
            Context       ctx = contextStack.getLast();
            long          ms = System.currentTimeMillis() - ctx.startTime;
            final Runtime runtime = Runtime.getRuntime();
            long          free = runtime.freeMemory() / MB;
            long          total = runtime.totalMemory() / MB;
            logVerbose("Execution of '%s.%s'. Finished in %d milliseconds. Memory usage: %dM of %dM\n", name,
                       command, ms, total - free, total);
        }

        contextStack.removeLast();
    }

    private static Set<File> loadExtensionsPath(Map<String, String> baseProperties)
    {
        String path = System.getenv("APB_EXT_PATH");

        String path2 = baseProperties.get("ext.path");

        if (path2 != null) {
            path = path == null ? path2 : path + File.pathSeparator + path2;
        }

        Set<File> jars = new LinkedHashSet<File>();

        if (path != null) {
            for (String p : path.split(File.pathSeparator)) {
                jars.addAll(FileUtils.listAllFilesWithExt(new File(p), ".jar"));
            }
        }

        return jars;
    }

    private void initProxies()
    {
        Proxy proxy = Proxy.getDefaultProxy(this);

        if (proxy != null && !proxy.getHost().isEmpty()) {
            System.setProperty("http.proxyHost", proxy.getHost());

            if (proxy.getPort() > 0) {
                System.setProperty("http.proxyPort", String.valueOf(proxy.getPort()));
            }

            if (!proxy.getNonProxyHosts().isEmpty()) {
                System.setProperty("http.nonProxyHosts", proxy.getNonProxyHosts());
            }
        }
    }

    /**
     * Load the projectpath list.
     * This method must be called after construction to ensure the properties are
     * initialized
     */
    private void loadProjectPath()
    {
        String path = System.getenv("APB_PROJECT_PATH");
        String path2 = baseProperties.get("project.path");

        if (path2 != null) {
            path = path == null ? path2 : path + File.pathSeparator + path2;
        }

        if (path == null) {
            path = "./project-definitions";
        }

        for (String p : path.split(File.pathSeparator)) {
            File dir = new File(p);

            if (dir.isAbsolute() && !dir.isDirectory()) {
                logWarning(Messages.INV_PROJECT_DIR(dir));
            }

            projectPath.add(dir);
        }
    }

    private void loadUserProperties()
    {
        File propFile = new File(apbDir, APB_PROPERTIES);

        try {
            Properties p = new Properties();
            p.load(new FileReader(propFile));
            copyProperties(p);
        }
        catch (IOException ignore) {
            // Ignore
        }
    }

    //~ Static fields/initializers ...........................................................................

    private static final long MB = (1024 * 1024);

    /**
     * A singleton to simplify Task invocations from the Module files
     * (May be in the future this should be a ThreadLocal...)
     */
    @Nullable private static Environment environment;

    private static final String JAR_FILE_URL_PREFIX = "jar:file:";

    private static final String APB_DIR = ".apb";
    private static final String APB_PROPERTIES = "apb.properties";

    //
    private static final String PROJECTS_HOME_PROP_KEY = "projects-home";

    static final String      GROUP_PROP_KEY = "group";
    static final String      VERSION_PROP_KEY = "version";
    static final String      PKG_PROP_KEY = "pkg";
    static final String      PKG_DIR_KEY = PKG_PROP_KEY + ".dir";
    private static final int HEADER_LENGTH = 30;

    //~ Inner Classes ........................................................................................

    private static class Context
    {
        private String command;
        private String element;
        private long   startTime;

        public Context(String element, String command)
        {
            this.element = element;
            this.command = command;
            startTime = System.currentTimeMillis();
        }

        public String getCommand()
        {
            return command;
        }

        public String getElement()
        {
            return element;
        }
    }
}
