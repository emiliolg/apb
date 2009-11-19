

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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import apb.compiler.InMemJavaC;

import apb.metadata.DependencyList;
import apb.metadata.ProjectElement;

import apb.utils.ClassUtils;
import apb.utils.DebugOption;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static apb.Logger.Level.VERBOSE;
//
// User: emilio
// Date: Aug 21, 2009
// Time: 10:09:03 AM

/**
 * This class is the entry point for the building process
 */
public class ProjectBuilder
{
    //~ Instance fields ......................................................................................

    /**
     * The name of the current module being processed
     */
    @NotNull String currentName;

    /**
     * The cache of downloaded Artifacts
     */
    @NotNull private final ArtifactsCache artifactsCache;

    /**
     * Track execution
     */
    private final boolean track;

    /**
     * The initial Environment
     */
    @NotNull private final Environment baseEnvironment;

    /**
     * The java compiler
     */
    @NotNull private final InMemJavaC javac;

    /**
     * The stack of executions
     */
    @NotNull private final LinkedList<Context> contextStack;
    @NotNull private final Logger              logger;

    /**
     * A Map that contains all constructed Helpers
     */
    @NotNull private final Map<String, ProjectElementHelper> helpers;

    /**
     * The project path where files are searched
     */
    private final Set<File> projectPath;

    //~ Constructors .........................................................................................

    /**
     * Create a new Project Builder
     * @param env  The base Environment for this builder
     * @param projectPath  The path used to search project definition files
     */
    public ProjectBuilder(Environment env, Set<File> projectPath)
    {
        baseEnvironment = env;
        logger = env.getLogger();
        javac = new InMemJavaC(env);
        helpers = new TreeMap<String, ProjectElementHelper>();
        artifactsCache = new ArtifactsCache(env);
        this.projectPath = projectPath;
        contextStack = new LinkedList<Context>();
        currentName = "";
        track = env.mustShow(DebugOption.TRACK);

        if (env instanceof DefaultEnvironment) {
            ((DefaultEnvironment) env).register(this);
        }
    }

    //~ Methods ..............................................................................................

    /**
     * Forwards a command invocation to a given module
     * @param command The command to be executed
     * @param module  The module to aply the command to
     */
    public static void forward(@NotNull String command, @Nullable ProjectElementHelper module)
    {
        if (module != null) {
            getInstance().build(module, command);
        }
    }

    // @Todo create an accesor
    /**
     * @exclude
     */
    public static ProjectElementHelper register(ProjectElement element)
    {
        return getInstance().findOrCreate(element);
    }

    /**
     * @exclude
     */
    public static File getArtifact(String group, String name, String relativeUrl, File target)
    {
        return getInstance().artifactsCache.getArtifact(group, relativeUrl + "/" + name, target);
    }

    /**
     * Build the project
     * Run the specified command over the given element
     * @param env
     *@param element The Project Element to run the command over
     * @param command The command to be run   @throws DefinitionException if a definition problem is found over the given element
     * @throws BuildException If an exception is raised doing the build
     */
    public void build(Environment env, String element, String command)
        throws DefinitionException, BuildException
    {
        currentName = element;
        File       source = projectElementFile(element);
        final File pdir = projectDir(source);

        ProjectElementHelper projectElement;

        try {
            projectElement = loadProjectElement(env, pdir, source);
        }
        catch (DependencyList.NullDependencyException e) {
            throw new DefinitionException(element, e);
        }
        catch (InstantiationException e) {
            throw new DefinitionException(element,
                                          new InstantiationException("Cannot instatiate Module object"));
        }
        catch (Throwable e) {
            throw new DefinitionException(element, e);
        }

        build(projectElement, command);
    }

    /**
     * Get the current command being run or null if no one
     */
    @NotNull public String getCurrentCommand()
    {
        return contextStack.isEmpty() ? "" : contextStack.getLast().getCommand();
    }

    /**
     * Get the name of the current project or module being built
     */
    @NotNull public String getCurrentName()
    {
        return currentName;
    }

    /**
     * Get the base environment for this builder
     */
    @NotNull public Environment getBaseEnvironment()
    {
        return baseEnvironment;
    }

    @Nullable ProjectElementHelper constructProjectElement(Environment env, @NotNull File path,
                                                           @NotNull File file)
    {
        try {
            return loadProjectElement(env, path, file);
        }
        catch (Throwable e) {
            return null;
        }
    }

    @NotNull File sourceFile(ProjectElement element)
    {
        final File file = javac.sourceFile(element.getClass());

        if (file == null) {
            throw new IllegalStateException("Cannot find source file for: " + element.getClass());
        }

        return file;
    }

    void build(@NotNull ProjectElementHelper element, @NotNull String commandName)
    {
        startExecution(element.getName(), commandName);
        element.build(this, commandName);
        endExecution();
    }

    void execute(@NotNull ProjectElementHelper element, @NotNull String commandName)
    {
        final Environment prev = Apb.setCurrentEnv(element);
        final Command     command = element.findCommand(commandName);

        if (command != null && element.notExecuted(command)) {
            for (Command cmd : command.getDependencies()) {
                if (element.notExecuted(cmd)) {
                    final String cmdName = cmd.getName();
                    startExecution(element.getName(), cmdName);
                    element.markExecuted(cmd);
                    cmd.invoke(element.getElement());
                    endExecution();
                }
            }
        }

        Apb.setCurrentEnv(prev);
    }

    String standardHeader()
    {
        StringBuilder result = new StringBuilder();

        if (track) {
            final int depth = contextStack.size();
            result.append(apb.utils.StringUtils.nChars(depth * 4, ' '));
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

    @NotNull private static ProjectBuilder getInstance()
    {
        ProjectBuilder result = Apb.getCurrentProjectBuilder();

        if (result == null) {
            throw new IllegalStateException();
        }

        return result;
    }

    private ProjectElementHelper createHelper(ProjectElement element)
    {
        try {
            return (ProjectElementHelper) findConstructor(element).newInstance(this, element);
        }
        catch (InstantiationException e) {
            throw new BuildException(e);
        }
        catch (IllegalAccessException e) {
            throw new BuildException(e);
        }
        catch (InvocationTargetException e) {
            throw new BuildException(e.getCause());
        }
    }

    private Constructor findConstructor(ProjectElement element)
    {
        final Class<?> elementClass = element.getClass();

        try {
            Class<?> helperClass = elementClass.getMethod("getHelper").getReturnType();
            return ClassUtils.findConstructor(helperClass, this, element);
        }
        catch (NoSuchMethodException e) {
            throw new BuildException(e);
        }
    }

    private void initHelpers()
    {
        final Set<ProjectElementHelper> processed = new HashSet<ProjectElementHelper>();
        final Set<ProjectElementHelper> hs = new HashSet<ProjectElementHelper>(helpers.values());

        while (!hs.isEmpty()) {
            for (ProjectElementHelper helper : hs) {
                helper.init();
                processed.add(helper);
            }

            hs.addAll(helpers.values());
            hs.removeAll(processed);
        }
    }

    private ProjectElementHelper loadProjectElement(Environment env, @NotNull File projectDirectory,
                                                    @NotNull File file)
        throws Throwable
    {
        env.putProperty(PROJECTS_HOME_PROP_KEY, projectDirectory.getAbsolutePath());

        try {
            final Class<? extends ProjectElement> aClass =
                javac.loadClass(projectDirectory, file).asSubclass(ProjectElement.class);
            final ProjectElement                  element = aClass.newInstance();
            currentName = element.getName();
            initHelpers();
            element.getHelper().setTopLevel(true);
            return element.getHelper();
        }
        catch (ExceptionInInitializerError e) {
            throw e.getException();
        }
    }

    private void startExecution(@NotNull final String name, @NotNull String command)
    {
        contextStack.add(new Context(name, command));
        currentName = name;

        if (track) {
            logger.log(VERBOSE, "About to execute '%s.%s'\n", name, command);
        }
    }

    private void endExecution()
    {
        if (track) {
            Context       ctx = contextStack.getLast();
            long          ms = System.currentTimeMillis() - ctx.startTime;
            final Runtime runtime = Runtime.getRuntime();
            long          free = runtime.freeMemory() / MB;
            long          total = runtime.totalMemory() / MB;
            logger.log(VERBOSE,
                       "Execution of '%s.%s'. Finished in %d milliseconds. Memory usage: %dM of %dM\n",
                       ctx.getElement(), ctx.getCommand(), ms, total - free, total);
        }

        contextStack.removeLast();
        currentName = contextStack.isEmpty() ? "" : contextStack.getLast().element;
    }

    @NotNull private File projectDir(File projectElementFile)
    {
        File   parent = projectElementFile.getAbsoluteFile().getParentFile();
        String p = parent.getPath();

        while (p != null) {
            final File file = new File(p);

            for (File pathElement : projectPath) {
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

    @NotNull private File projectElementFile(String projectElement)
        throws DefinitionException
    {
        // Strip JAVA_EXT
        if (projectElement.endsWith(Constants.JAVA_EXT)) {
            projectElement =
                projectElement.substring(0, projectElement.length() - Constants.JAVA_EXT.length());
        }

        File f = new File(projectElement);

        // Replace 'dots' by file separators BUT ONLY IN THE FILE NAME.
        File file =
            new File(f.getParentFile(), f.getName().replace('.', File.separatorChar) + Constants.JAVA_EXT);

        try {
            return file.exists() ? file : searchInProjectPath(file.getPath());
        }
        catch (FileNotFoundException e) {
            throw new DefinitionException(projectElement, e);
        }
    }

    @NotNull private File searchInProjectPath(String projectElement)
        throws FileNotFoundException
    {
        for (File dir : projectPath) {
            File file = new File(dir, projectElement);

            if (file.exists()) {
                return file;
            }
        }

        throw new FileNotFoundException(projectElement);
    }

    private ProjectElementHelper findOrCreate(ProjectElement element)
    {
        final String         name = element.getName();
        ProjectElementHelper result = helpers.get(name);

        if (result == null) {
            result = createHelper(element);
            helpers.put(name, result);
        }

        return result;
    }

    //~ Static fields/initializers ...........................................................................

    private static final int HEADER_LENGTH = 30;

    private static final String PROJECTS_HOME_PROP_KEY = "projects-home";
    private static final long   MB = (1024 * 1024);

    //~ Inner Classes ........................................................................................

    private static class Context
    {
        private final long   startTime;
        private final String command;
        private final String element;

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
