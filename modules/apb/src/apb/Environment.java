

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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import apb.compiler.InMemJavaC;

import apb.index.DefinitionsIndex;

import apb.metadata.Module;
import apb.metadata.ProjectElement;

import apb.utils.FileUtils;
import apb.utils.PropertyExpansor;

import org.jetbrains.annotations.NotNull;

import static java.lang.Character.isJavaIdentifierPart;

import static apb.utils.FileUtils.JAVA_EXT;

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

    private File basedir;

    /**
     * Base properties are enviroment or argument properties
     * They take preference over the properties defined in project elements
     */
    private Map<String, String> baseProperties;
    private long                clock;

    private Command currentCommand;

    private ProjectElementHelper currentElement;
    private DefinitionsIndex     definitionsIndex;

    /**
     * The set of jars that comprise the extension class path
     */
    @NotNull final private Set<File>                      extClassPath;
    private boolean                                 failOnError;
    private boolean                                 forceBuild;
    private final Map<String, ProjectElementHelper> helpersByElement;
    private InMemJavaC                              javac;
    private boolean                                 nonRecursive;
    private Os                                      os;

    private Set<File> projectPath;

    /**
     * Project properties are the properties defined in project elements
     */
    private Map<String, String> projectProperties;
    private File                projectsHome;
    private boolean             quiet;
    private boolean             showStackTrace;
    private boolean             verbose;

    //~ Constructors .........................................................................................

    public Environment()
    {
        apbDir = new File(System.getProperty("user.home"), APB_DIR);
        os = Os.getInstance();
        baseProperties = new TreeMap<String, String>();
        projectProperties = new TreeMap<String, String>();

        helpersByElement = new HashMap<String, ProjectElementHelper>();

        // Read Environment
        //        for (Map.Entry<String,String> entry : System.getenv().entrySet()) {
        //            baseProperties.put(entry.getKey(), entry.getValue());
        //        }
        loadUserProperties();

        // Read System Properties
        copyProperties(System.getProperties());

        extClassPath = loadExtensionsPath(baseProperties);
        resetJavac();
    }

    //~ Methods ..............................................................................................

    public abstract void logInfo(String msg, Object... args);

    public abstract void logWarning(String msg, Object... args);

    public abstract void logSevere(String msg, Object... args);

    public abstract void logVerbose(String msg, Object... args);

    /**
     * Get the Extension Jars to be searched when we compile definitions
     * @return the extension Jars to be searched when we compiled definitions
     */
    @NotNull public Collection<File> getExtClassPath()
    {
        return extClassPath;
    }

    public long sourceLastModified(@NotNull Class clazz)
    {
        return javac.sourceLastModified(clazz);
    }

    public void setCurrentCommand(Command currentCommand)
    {
        this.currentCommand = currentCommand;
    }

    public void handle(String msg)
    {
        handle(new BuildException(msg));
    }

    public void setFailOnError(boolean b)
    {
        failOnError = b;
    }

    public void setForceBuild(boolean b)
    {
        forceBuild = b;
    }

    public void handle(Throwable e)
    {
        if (failOnError) {
            throw (e instanceof BuildException) ? (BuildException) e : new BuildException(e);
        }

        logSevere(e.getMessage());
    }

    public void setVerbose()
    {
        verbose = true;
    }

    public void setQuiet()
    {
        quiet = true;
    }

    public boolean forceBuild()
    {
        return forceBuild;
    }

    /**
     * Return the value of the specified property
     * @param id The property to search
     * @return The value of the property or the empty String if the Property is not found and failOnError is not set
     */
    @NotNull public String getProperty(@NotNull String id)
    {
        String result = baseProperties.get(id);

        if (result == null) {
            result = projectProperties.get(id);

            if (result == null) {
                PropertyException e = new PropertyException(id);

                if (isVerbose()) {
                    //                StringBuilder additionalInfo = new StringBuilder();
                    //                additionalInfo.append("\nDefined properties are: \n");
                    //
                    //                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    //                    additionalInfo.append("  ").append(entry.getKey()).append(" = ")
                    //                      .append(StringUtils.encode(entry.getValue())).append("\n");
                    //                }
                    //
                    //                e.setAdditionalInfo(additionalInfo.toString());
                }

                handle(e);
                result = "";
            }
        }

        return result;
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

    public boolean isVerbose()
    {
        return verbose;
    }

    public boolean isQuiet()
    {
        return quiet;
    }

    public String expand(String string)
    {
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

    public void setProjectsHome(File file)
    {
        projectsHome = file;
        putProperty(PROJECTS_HOME_PROP_KEY, file.getAbsolutePath());
    }

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

    public void removeHelper(ProjectElement element)
    {
        synchronized (helpersByElement) {
            helpersByElement.remove(element.getName());
        }
    }

    public File getProjectsHome()
    {
        return projectsHome;
    }

    public void abort(String msg)
    {
        logInfo(msg);
        System.exit(1);
    }

    public void resetClock()
    {
        clock = System.currentTimeMillis();
    }

    public void completedMessage(boolean ok)
    {
        logInfo(ok ? Messages.BUILD_COMPLETED(System.currentTimeMillis() - clock) : Messages.BUILD_FAILED);
    }

    public File getBaseDir()
    {
        return basedir;
    }

    @NotNull public File fileFromBase(@NotNull String name)
    {
        final File child = new File(expand(name));
        return child.isAbsolute() ? child : new File(basedir, child.getPath());
    }

    @NotNull public File fileFromSource(@NotNull String name)
    {
        final File child = new File(expand(name));
        return child.isAbsolute() ? child : new File(getModuleHelper().getSource(), child.getPath());
    }

    @NotNull public File fileFromGeneratedSource(@NotNull String name)
    {
        final File child = new File(expand(name));
        return child.isAbsolute() ? child : new File(getModuleHelper().getGeneratedSource(), child.getPath());
    }

    @NotNull public ProjectElement activate(@NotNull ProjectElement element)
    {
        currentElement = getHelper(element);

        final ProjectElement result =
            currentElement.element != null ? currentElement.element
                                           : new PropertyExpansor(this).expand(element);

        basedir = new File(result.basedir);
        currentElement.activate(result);
        return result;
    }

    public String getCurrentName()
    {
        return currentElement == null ? null : currentElement.getName();
    }

    public void deactivate()
    {
        currentElement = null;
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

    public Set<File> getProjectPath()
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

    public void setShowStackTrace()
    {
        showStackTrace = true;
    }

    public boolean showStackTrace()
    {
        return showStackTrace;
    }

    public Command getCurrentCommand()
    {
        return currentCommand;
    }

    public Os getOs()
    {
        return os;
    }

    public void putProperty(String name, String value)
    {
        if (isVerbose()) {
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

    public void setNonRecursive()
    {
        nonRecursive = true;
    }

    public boolean isNonRecursive()
    {
        return nonRecursive;
    }

    public void resetJavac()
    {
        javac = new InMemJavaC(this);
    }

    /**
     * Construct a Helper for the file
     * @param file The file containing the module definition
     * @return A project helper asociated to the file
     */
    @NotNull public ProjectElementHelper constructProjectElement(File file)
        throws Exception
    {
        final File pdir = projectDir(file);
        setProjectsHome(pdir);

        return getHelper(javac.loadClass(pdir, file).asSubclass(ProjectElement.class).newInstance());
    }

    @NotNull public File getApbDir()
    {
        return apbDir;
    }

    @NotNull public synchronized DefinitionsIndex getDefinitionsIndex()
    {
        if (definitionsIndex == null) {
            definitionsIndex = new DefinitionsIndex(this);
        }

        return definitionsIndex;
    }

    protected void loadProjectPath()
    {
        projectPath = new LinkedHashSet<File>();

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

            if (dir.isAbsolute() && (!dir.exists() || !dir.isDirectory())) {
                logWarning(Messages.INV_PROJECT_DIR(dir));
            }

            projectPath.add(dir);
        }
    }

    static private Set<File> loadExtensionsPath(Map<String, String> baseProperties)
    {
        String path = System.getenv("APB_EXT_PATH");

        String path2 = baseProperties.get("ext.path");

        if (path2 != null) {
            path = path == null ? path2 : path + File.pathSeparator + path2;
        }

        Set<File> jars = new LinkedHashSet<File>();
        for (String p : path.split(File.pathSeparator)) {
            jars.addAll(FileUtils.listAllFilesWithExt(new File(p), ".jar"));
        }
        return jars;
    }

    protected void copyProperties(Map<?, ?> p)
    {
        for (Map.Entry<?, ?> entry : p.entrySet()) {
            baseProperties.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
    }

    void addHelper(@NotNull ProjectElementHelper helper)
    {
        synchronized (helpersByElement) {
            helpersByElement.put(helper.getName(), helper);
        }
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
        catch (Exception e) {
            throw new DefinitionException(name, e);
        }
    }

    private void loadUserProperties()
    {
        File propFile = new File(apbDir, APB_PROPERTIES);

        if (propFile.exists()) {
            try {
                Properties p = new Properties();
                p.load(new FileReader(propFile));
                copyProperties(p);
            }
            catch (IOException ignore) {}
        }
    }

    //~ Static fields/initializers ...........................................................................

    private static final String JAR_FILE_URL_PREFIX = "jar:file:";

    private static final String APB_DIR = ".apb";
    private static final String APB_PROPERTIES = "apb.properties";

    //
    private static final String PROJECTS_HOME_PROP_KEY = "projects-home";

    static final String GROUP_PROP_KEY = "group";
    static final String VERSION_PROP_KEY = "version";
    static final String PKG_PROP_KEY = "pkg";
    static final String PKG_DIR_KEY = PKG_PROP_KEY + ".dir";
}
