
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import static java.lang.reflect.Modifier.isPublic;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import apb.metadata.BuildTarget;
import apb.metadata.DefaultTarget;
import apb.metadata.ProjectElement;
import apb.utils.IdentitySet;
import apb.utils.NameUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//
// User: emilio
// Date: Sep 4, 2008
// Time: 3:56:42 PM

//
public class Command
{
    //~ Instance fields ......................................................................................

    // The whole list of commands to execute
    private List<Command> commands;
    private List<Command> directDeps;

    private final Method method;
    private final String name;
    private String description;

    //~ Constructors .........................................................................................

    protected Command(String name, Method method, String description)
    {
        this.name = name;
        this.method = method;
        this.description = description;
        commands = Collections.emptyList();
        directDeps = new ArrayList<Command>();
    }

    private Command(Method method, BuildTarget annotation)
    {
        this(annotation.name().isEmpty() ? NameUtils.idFromMethod(method) : annotation.name(), method, annotation.description());
    }

    //~ Methods ..............................................................................................

    public static Collection<Command> listCommands(Class<? extends ProjectElement> projectElementClass)
    {
        return buildCommands(projectElementClass).values();
    }

    @Nullable public static Command findCommand(@NotNull ProjectElement element, @NotNull String commandName)
    {
        Map<String, Command> map = buildCommands(element.getClass());
        Command              result = map.get(commandName);

        if (result != null) {
            // solve dependencies
            result.solveDependencies(map);
        }

        return result;
    }

    public String getName()
    {
        return name;
    }

    public String toString()
    {
        return getName();
    }

    public Iterable<Command> getAllCommands()
    {
        return commands;
    }

    void invoke(ProjectElement element, Environment env)
    {
        try {
            if (Modifier.isStatic(method.getModifiers())) {
                method.invoke(null, element, env);
            }
            else {
                method.invoke(element, env);
            }
        }
        catch (IllegalAccessException e) {
            env.handle(e);
        }
        catch (InvocationTargetException e) {
            final Throwable t = e.getCause();
            env.handle(t);
        }
    }

    private static Map<String, Command> buildCommands(@NotNull final Class<? extends ProjectElement> projectElementClass)
    {
        Map<String, Command> commandsMap = new TreeMap<String, Command>();

        for (Class<?> clazz = projectElementClass; clazz != null; clazz = clazz.getSuperclass()) {
            addCommandsForClass(clazz, commandsMap);
        }

        // Add help
        final Help hlp = new Help();
        commandsMap.put(hlp.getName(), hlp);
        return commandsMap;
    }

    private static void addCommandsForClass(Class<?> clazz, Map<String, Command> commandsMap)
    {
        for (Method method : clazz.getMethods()) {
            BuildTarget a = method.getAnnotation(BuildTarget.class);

            if (a != null && hasRightSignature(method)) {
                Command cmd = new Command(method, a);
                commandsMap.put(cmd.getName(), cmd);
            }
        }

        // Add default if not there
        if (!commandsMap.containsKey(DEFAULT_COMMAND)) {
            DefaultTarget a = clazz.getAnnotation(DefaultTarget.class);

            if (a != null) {
                Command def = commandsMap.get(a.value());

                if (def == null) {
                    throw new IllegalStateException("Cannot assign default command " + a.value());
                }

                commandsMap.put(DEFAULT_COMMAND, def);
            }
        }
    }

    private static boolean hasRightSignature(Method method)
    {
        Class<?>[] pars = method.getParameterTypes();
        return pars.length == 1 && pars[0].equals(Environment.class) && isPublic(method.getModifiers()) &&
               method.getReturnType().equals(Void.TYPE);
    }

    //    private boolean checkTarget(Environment env) {
    //        boolean result = true;
    //        final BuildTarget t = method.getAnnotation(BuildTarget.class);
    //        if (!t.target().isEmpty() && !t.source().isEmpty()) {
    //            File source = env.fileFromBase(t.source());
    //            File target = env.fileFromBase(t.target());
    //            result = source.lastModified() >= target.lastModified();
    //        }
    //        return result;
    //
    //    }

    private void solveDependencies(Map<String, Command> commandsMap)
    {
        for (Command command : commandsMap.values()) {
            command.solveDirectDependencies(commandsMap);
        }

        commands = new ArrayList<Command>();
        tsort(commands, new IdentitySet<Command>());
    }

    private void solveDirectDependencies(Map<String, Command> commandsMap)
    {
        final BuildTarget target = method.getAnnotation(BuildTarget.class);

        for (String dep : target.depends()) {
            Command cmd = commandsMap.get(dep);

            if (cmd != null) {
                directDeps.add(cmd);
            }
        }

        if (!target.before().isEmpty()) {
            Command cmd = commandsMap.get(target.before());

            if (cmd != null) {
                cmd.directDeps.add(this);
            }
        }
    }

    /**
     * Topological sort dependent modules using a Depth First Search
     * @param elements All descendant elements
     * @param visited  Already visited elements
     */
    private void tsort(List<Command> elements, IdentitySet<Command> visited)
    {
        for (int i = directDeps.size() - 1; i >= 0; i--) {
            Command dependency = directDeps.get(i);

            if (!visited.contains(dependency)) {
                visited.add(dependency);
                dependency.tsort(elements, visited);
            }
        }

        elements.add(this);
    }

    //~ Static fields/initializers ...........................................................................

    @NonNls public static final String DEFAULT_COMMAND = "default";

    public boolean isDefault() {
        return DEFAULT_COMMAND.equals(name);
    }

    public String getDescription() {
        return description;
    }

    //~ Inner Classes ........................................................................................

}
