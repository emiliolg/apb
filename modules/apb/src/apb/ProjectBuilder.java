

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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import apb.compiler.InMemJavaC;

import apb.index.ArtifactsCache;

import apb.metadata.DependencyList;
import apb.metadata.Module;
import apb.metadata.ProjectElement;

import apb.utils.DebugOption;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static apb.utils.FileUtils.JAVA_EXT;
//
// User: emilio
// Date: Aug 21, 2009
// Time: 10:09:03 AM

//
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
     * The stack of executions
     */
    @NotNull private final LinkedList<Context> contextStack;

    /**
     * The initial Environment
     */
    @NotNull private final Environment env;

    /**
     * A Map that contains all constructed Helpers
     */
    @NotNull private final Map<String, ProjectElementHelper> helpersByElement;

    /**
     * The java compiler
     */
    @NotNull private final InMemJavaC javac;

    /**
     * The project path where files are searched
     */
    private final Set<File> projectPath;
    /**
     * Track execution
     */
    private final boolean track;

    //~ Constructors .........................................................................................

    public ProjectBuilder(Environment env, Set<File> projectPath)
    {
        this.env = env;
        javac = new InMemJavaC(env);
        helpersByElement = new HashMap<String, ProjectElementHelper>();
        artifactsCache = new ArtifactsCache(env);
        this.projectPath = projectPath;
        contextStack = new LinkedList<Context>();
        currentName = "";
        instance = this;
        track = this.env.mustShow(DebugOption.TRACK);
    }

    //~ Methods ..............................................................................................

    /**
     * Get (Constructing it if necessary) the helper for a given element
     * A Helper is an Object that extends the functionality of a given ProjectElement
     * @param element The Project Element to get the helper from
     * @return The helper for the given element
     */
    @NotNull public static ProjectElementHelper findHelper(@NotNull ProjectElement element)
    {
        return getInstance().getHelper(element);
    }

    @NotNull public static ProjectBuilder getInstance()
    {
        if (instance == null) {
            throw new IllegalStateException();
        }

        return instance;
    }

    public static void forward(@NotNull String command, Iterable<? extends Module> modules)
    {
        for (Module module : modules) {
            final ProjectBuilder pb = getInstance();
            pb.build(pb.getHelper(module), command);
        }
    }

    public String makeStandardHeader()
    {
        StringBuilder result = new StringBuilder();

        if (track) {
            final int depth = getInstance().contextStack.size();
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

    @Nullable public ProjectElementHelper constructProjectElement(@NotNull File path, @NotNull File file)
    {
        try {
            return loadProjectElement(path, file);
        }
        catch (Throwable e) {
            return null;
        }
    }

    /**
     * Build the project
     * Run the specified command over the given element
     * @param element The Project Element to run the command over
     * @param command The command to be run
     * @throws DefinitionException if a definition problem is found over the given element
     * @throws BuildException If an exception is raised doing the build
     */
    public void build(String element, String command)
        throws DefinitionException, BuildException
    {
        File       source = projectElementFile(element);
        final File pdir = projectDir(source);

        ProjectElementHelper projectElement;

        try {
            projectElement = loadProjectElement(pdir, source);
        }
        catch (DependencyList.NullDependencyException e) {
            throw new DefinitionException(element, e);
        }
        catch (Throwable e) {
            throw new DefinitionException(element, e);
        }

        build(projectElement, command);
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

    public synchronized void registerHelper(@NotNull ProjectElementHelper helper)
    {
        helpersByElement.put(helper.getName(), helper);
    }

    public void remove(@NotNull ProjectElementHelper helper)
    {
        helpersByElement.remove(helper.getName());
    }

    @NotNull public File sourceFile(ProjectElement element)
    {
        final File file = javac.sourceFile(element.getClass());

        if (file == null) {
            throw new IllegalStateException("Cannot find source file for: " + element.getClass());
        }

        return file;
    }

    /**
     * Get the current command being run or null if no one
     */
    @NotNull public String getCurrentCommand()
    {
        return contextStack.isEmpty() ? "" : contextStack.getLast().getCommand();
    }

    @NotNull public String getCurrentName()
    {
        return currentName;
    }

    synchronized ProjectElementHelper getHelper(ProjectElement element)
    {
        ProjectElementHelper result = helpersByElement.get(element.getName());

        if (result == null) {
            result = ProjectElementHelper.create(element, env);
        }

        return result;
    }

    void build(@NotNull ProjectElementHelper element, @NotNull String commandName)
    {
        startExecution(element.getName(), commandName);
        element.build(this, commandName);
        endExecution();
    }

    void execute(@NotNull ProjectElementHelper element, @NotNull String commandName)
    {
        Command command = element.findCommand(commandName);

        if (command != null && element.notExecuted(command)) {
            for (Command cmd : command.getDependencies()) {
                if (element.notExecuted(cmd)) {
                    final String cmdName = cmd.getQName();
                    startExecution(element.getName(), cmdName);
                    element.markExecuted(cmd);
                    cmd.invoke(element.getElement());
                    endExecution();
                }
            }
        }
    }

    private ProjectElementHelper loadProjectElement(@NotNull File projectDirectory, @NotNull File file)
        throws Throwable
    {
        env.putProperty(PROJECTS_HOME_PROP_KEY, projectDirectory.getAbsolutePath());

        try {
            final ProjectElement element =
                javac.loadClass(projectDirectory, file).asSubclass(ProjectElement.class).newInstance();
            currentName = element.getName();
            final ProjectElementHelper helper = getHelper(element);
            helper.setTopLevel(true);
            return helper;
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
            env.logVerbose("About to execute '%s.%s'\n", name, command);
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
            env.logVerbose("Execution of '%s.%s'. Finished in %d milliseconds. Memory usage: %dM of %dM\n",
                           ctx.getElement(), ctx.getCommand(), ms, total - free, total);
        }

        contextStack.removeLast();
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
        if (projectElement.endsWith(JAVA_EXT)) {
            projectElement = projectElement.substring(0, projectElement.length() - JAVA_EXT.length());
        }

        File f = new File(projectElement);

        // Replace 'dots' by file separators BUT ONLY IN THE FILE NAME.
        File file = new File(f.getParentFile(), f.getName().replace('.', File.separatorChar) + JAVA_EXT);

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

    //~ Static fields/initializers ...........................................................................

    public static final int HEADER_LENGTH = 30;

    private static ProjectBuilder instance;

    public static final String PROJECTS_HOME_PROP_KEY = "projects-home";
    private static final long  MB = (1024 * 1024);

    //~ Inner Classes ........................................................................................

    private static class Context
    {
        private final String command;
        private final String element;
        private final long   startTime;

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
