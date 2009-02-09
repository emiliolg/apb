
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
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import apb.compiler.InMemJavaC;

import apb.metadata.Module;
import apb.metadata.Project;
import apb.metadata.ProjectElement;

import apb.utils.FileUtils;
import apb.utils.NameUtils;

import org.jetbrains.annotations.NotNull;

import static java.lang.Character.isJavaIdentifierPart;

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

    private File             basedir;
    private Map<File, Class> classesByFile;
    private long             clock;

    private Command currentCommand;

    private ProjectElementHelper              currentElement;
    private boolean                           failOnError;
    private boolean                           forceBuild;
    private Map<String, ProjectElementHelper> helpersByElement;
    private InMemJavaC                        javac;
    private Os                                os;

    private Set<File>           projectPath;
    private File                projectsHome;
    private Map<String, String> properties;
    private boolean             quiet;
    private boolean             showStackTrace;
    private boolean             verbose;

    //~ Constructors .........................................................................................

    public Environment()
    {
        os = Os.getInstance();
        properties = new TreeMap<String, String>();
        helpersByElement = new HashMap<String, ProjectElementHelper>();
        classesByFile = new HashMap<File, Class>();
        javac = new InMemJavaC();

        // Read Environment
        //        for (Map.Entry<String,String> entry : System.getenv().entrySet()) {
        //            properties.put(entry.getKey(), entry.getValue());
        //        }
        loadUserProperties();

        // Read System Properties
        copyProperties(System.getProperties());

        loadProjectPath();
    }

    //~ Methods ..............................................................................................

    public abstract void logInfo(String msg, Object... args);

    public abstract void logWarning(String msg, Object... args);

    public abstract void logSevere(String msg, Object... args);

    public abstract void logVerbose(String msg, Object... args);

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

    public String getProperty(String id)
    {
        String result = properties.get(id);

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

        return result;
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
        properties.put(PROJECTS_HOME_PROP_KEY, file.getAbsolutePath());
    }

    public ProjectElementHelper getHelper(ProjectElement element)
    {
        synchronized (helpersByElement) {
            final String         className = element.getClass().getName();
            ProjectElementHelper result = helpersByElement.get(className);

            if (result == null) {
                result = ProjectElementHelper.create(element, this);
                helpersByElement.put(className, result);
            }

            return result;
        }
    }

    public void removeHelper(ProjectElement element)
    {
        synchronized (helpersByElement) {
            helpersByElement.remove(element.getClass().getName());
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

    public File fileFromBase(String name)
    {
        final File child = new File(expand(name));
        return child.isAbsolute() ? child : new File(basedir, child.getPath());
    }

    public ProjectElement activate(@NotNull ProjectElement element)
    {
        currentElement = getHelper(element);

        final String prop = element instanceof Project ? PROJECT_NAME_PROP_KEY : MODULE_NAME_PROP_KEY;
        putProperty(prop, getCurrentName());
        putProperty(prop.toLowerCase(), NameUtils.idFromJavaId(getCurrentName()));

        ProjectElement result = expandProperties("", element);

        try {
            basedir = new File(getProperty(BASEDIR_PROP_KEY)).getCanonicalFile();
        }
        catch (IOException e) {
            throw new BuildException(e);
        }

        currentElement.activate();
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

    public Collection<String> listModules(String last)
    {
        SortedSet<String> result = new TreeSet<String>();

        for (File f : FileUtils.listAllFilesWithExt(getProjectPath(), FileUtils.JAVA_EXT)) {
            final String fileName = f.getName();

            if (fileName.startsWith(last)) {
                result.add(fileName.substring(0, fileName.length() - FileUtils.JAVA_EXT.length()));
            }
        }

        if (result.size() == 1) {
            return listCommands(result.first(), "");
        }

        return result;
    }

    public Collection<String> listCommands(String module, String command)
    {
        Set<String>          result = new TreeSet<String>();
        ProjectElementHelper helper = constructProjectElement(module);

        for (String cmd : Command.listCommands(helper.getElement().getClass())) {
            if (cmd.startsWith(command)) {
                result.add(module + "." + cmd);
            }
        }

        return result;
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

    void putProperty(String name, String value)
    {
        // Special handling to canonize basedir
        if (name.equals(BASEDIR_PROP_KEY)) {
            try {
                value = new File(value).getCanonicalPath();
            }
            catch (IOException e) {}
        }

        if (isVerbose()) {
            logVerbose("property %s=%s\n", name, value);
        }

        properties.put(name, value);
    }

    /**
     * Expand the property attributes
     * The expansion order is done usinf Bread First Sequence.
     * So first the primitive fields are expanded and then the fields
     * with complex objects.
     *
     * @param parent
     * @param object
     */
    <T> T expandProperties(String parent, T object)
    {
        T result = newInstance(object);

        final Map<FieldHelper, Object> innerMap = new HashMap<FieldHelper, Object>();

        // Expand the top level
        // add other fields to the map
        for (FieldHelper field : FieldHelper.publicAndDeclaredfields(this, parent, object)) {
            expandProperty(field, object, result, innerMap);
        }

        // Expand the inner level
        for (Map.Entry<FieldHelper, Object> entry : innerMap.entrySet()) {
            final FieldHelper field = entry.getKey();
            Object            inner = expandProperties(field.getCompoundName(), entry.getValue());
            field.setFieldValue(result, inner);
        }

        return result;
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
        File   file = new File(projectElement);
        String name = file.getName();
        int    ext = name.lastIndexOf('.');

        if (ext == -1) {
            projectElement += FileUtils.JAVA_EXT;
            file = new File(projectElement);
        }

        return file.exists() ? file : searchInProjectPath(projectElement);
    }

    File projectDir(File projectElementFile)
    {
        File   parent = projectElementFile.getParentFile();
        String p = parent.getAbsolutePath();

        while (p != null) {
            final File file = new File(p);

            for (File pathElement : getProjectPath()) {
                if (parent.equals(pathElement)) {
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
    ProjectElementHelper constructProjectElement(String name)
    {
        ProjectElementHelper result = null;

        try {
            File       source = projectElementFile(name);
            final File pdir = projectDir(source);

            Class<?> clazz = loadClass(pdir, source);

            setProjectsHome(pdir);
            ProjectElement projectElement = (ProjectElement) clazz.newInstance();
            result = getHelper(projectElement);
        }
        catch (IllegalAccessException e) {
            handle(e);
        }
        catch (InstantiationException e) {
            handle(e);
        }
        catch (IOException e) {
            handle("Cannot find definition file for: '" + name + "'");
        }

        return result;
    }

    private Class<?> loadClass(File pdir, File source)
    {
        Class<?> clazz = classesByFile.get(source);

        if (clazz == null) {
            logVerbose("Loading: %s\n", source);

            clazz = javac.compileToClass(pdir, source);

            if (clazz == null || !ProjectElement.class.isAssignableFrom(clazz)) {
                abort("Invalid file: " + source);
            }

            classesByFile.put(source, clazz);
        }

        return clazz;
    }

    private void loadProjectPath()
    {
        projectPath = new LinkedHashSet<File>();

        String path = System.getenv("APB_PROJECT_PATH");

        String path2 = properties.get("project.path");

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

    private void loadUserProperties()
    {
        final String home = System.getProperty("user.home");
        File         propFile = new File(new File(home, APB_DIR), APB_PROPERTIES);

        if (propFile.exists()) {
            try {
                Properties p = new Properties();
                p.load(new FileReader(propFile));
                copyProperties(p);
            }
            catch (IOException ignore) {}
        }
    }

    private void copyProperties(Properties p)
    {
        for (Map.Entry<Object, Object> entry : p.entrySet()) {
            properties.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
    }

    @SuppressWarnings({ "unchecked" })
    private <T> T newInstance(T object)
    {
        final Class<? extends T> c = (Class<? extends T>) object.getClass();

        try {
            return c.newInstance();
        }
        catch (InstantiationException e) {
            handle(e);
        }
        catch (IllegalAccessException e) {
            handle(e);
        }

        return object;
    }

    private <T> void expandProperty(FieldHelper field, T object, T result, Map<FieldHelper, Object> innerMap)
    {
        Object fieldValue = field.getFieldValue(object);
        field.setFieldValue(result, fieldValue);

        if (field.isProperty()) {
            final Class<?> type = field.getType();

            if (type.equals(String.class) || type.isPrimitive() || type.isEnum()) {
                final String value = field.expand(fieldValue);
                field.setFieldValue(result, convert(value, type));
                putProperty(field.getCompoundName(), value);
            }
            else if (fieldValue != null) {
                innerMap.put(field, fieldValue);
            }
        }
    }

    private Object convert(String value, Class<?> type)
    {
        Object result = value;

        if (type != String.class) {
            if (type == Boolean.TYPE) {
                result = Boolean.parseBoolean(value);
            }
            else if (type == Integer.TYPE) {
                result = Integer.parseInt(value);
            }
            else {
                handle("Unsuported conversion to " + type.getName());
            }
        }

        return result;
    }

    //~ Static fields/initializers ...........................................................................

    private static final String JAR_FILE_URL_PREFIX = "jar:file:";

    private static final String APB_DIR = ".apb";
    private static final String APB_PROPERTIES = "apb.properties";

    static final String BASEDIR_PROP_KEY = "basedir";

    static final String PROJECT_NAME_PROP_KEY = "Project";
    static final String PROJECT_PROP_KEY = "project";

    static final String MODULE_NAME_PROP_KEY = "Module";
    static final String MODULE_PROP_KEY = "module";

    //
    private static final String PROJECTS_HOME_PROP_KEY = "projects-home";

    static final String GROUP_PROP_KEY = "group";
    static final String VERSION_PROP_KEY = "version";
    static final String PKG_PROP_KEY = "pkg";
    static final String PKG_DIR_KEY = PKG_PROP_KEY + ".dir";
}
