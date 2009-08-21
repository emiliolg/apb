

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
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import apb.metadata.Module;
import apb.metadata.ProjectElement;

import apb.utils.DebugOption;
import apb.utils.FileUtils;
import apb.utils.PropertyExpansor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.lang.Character.isJavaIdentifierPart;

import static apb.utils.StringUtils.isEmpty;

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
     * The directory where the project definition files are stored
     */
    @Nullable File projectsHome;

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
     * The current element being run
     */
    @Nullable private ProjectElementHelper currentElement;

    /**
     * Control what to show when logging
     */
    @NotNull private final EnumSet<DebugOption> debugOptions;

    /**
     * The set of jars that comprise the extension class path
     */
    @NotNull private final Set<File> extClassPath;
    private boolean                  failOnError;
    private boolean                  forceBuild;

    private boolean nonRecursive;

    /**
     * An abstract representation of the Operating System
     */
    @NotNull private final Os os;

    /**
     * Project properties are the properties defined in project elements
     */
    @NotNull private final Map<String, String> projectProperties;

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
        os = Os.getInstance();
        baseProperties = new TreeMap<String, String>();
        projectProperties = new TreeMap<String, String>();

        // Read Environment
        //        for (Map.Entry<String,String> entry : System.getenv().entrySet()) {
        //            baseProperties.put(entry.getKey(), entry.getValue());
        //        }
        copyProperties(FileUtils.userProperties());

        // Read System Properties
        copyProperties(System.getProperties());

        extClassPath = loadExtensionsPath(baseProperties);
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
        final ProjectElementHelper helper = ProjectBuilder.findHelper(element);
        currentElement = helper;

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

    public void forward(@NotNull String command, Iterable<? extends Module> modules)
    {
        for (Module module : modules) {
            final ProjectBuilder pb = ProjectBuilder.getInstance();
            pb.build(pb.getHelper(module), command);
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

    public void setDebugOptions(@NotNull EnumSet<DebugOption> options)
    {
        debugOptions.addAll(options);

        if (!options.isEmpty()) {
            setVerbose();
        }
    }

    protected void setVerbose() {}

    /**
     * Do all initializations that needs properties already initialized
     */
    protected void postInit()
    {
        initProxies();
    }

    protected void copyProperties(Map<?, ?> p)
    {
        for (Map.Entry<?, ?> entry : p.entrySet()) {
            baseProperties.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
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

    //~ Static fields/initializers ...........................................................................

    /**
     * A singleton to simplify Task invocations from the Module files
     * (May be in the future this should be a ThreadLocal...)
     */
    @Nullable private static Environment environment;

    private static final String JAR_FILE_URL_PREFIX = "jar:file:";

    static final String GROUP_PROP_KEY = "group";
    static final String VERSION_PROP_KEY = "version";
    static final String PKG_PROP_KEY = "pkg";
    static final String PKG_DIR_KEY = PKG_PROP_KEY + ".dir";
}
