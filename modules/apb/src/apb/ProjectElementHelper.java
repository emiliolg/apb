
// ...........................................................................................................
// Copyright (c) 1993, 2009, Oracle and/or its affiliates. All rights reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Oracle Corp.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// Last changed on 2009-04-23 18:07:22 (-0300), by: emilio. $Revision$
// ...........................................................................................................

package apb;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import apb.metadata.Module;
import apb.metadata.Project;
import apb.metadata.ProjectElement;
import apb.metadata.TestModule;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//
// User: emilio
// Date: Oct 10, 2008
// Time: 10:41:05 AM

//
public abstract class ProjectElementHelper
{
    //~ Instance fields ......................................................................................

    public Environment       env;
    @Nullable ProjectElement element;

    private CommandBuilder builder;

    @NotNull private final ProjectElement proto;
    private boolean                       topLevel;

    //~ Constructors .........................................................................................

    protected ProjectElementHelper(@NotNull ProjectElement element, @NotNull Environment environment)
    {
        proto = element;
        env = environment;
        env.addHelper(this);
    }

    //~ Methods ..............................................................................................

    public abstract Set<ModuleHelper> listAllModules();

    @NotNull public static ProjectElementHelper create(ProjectElement element, Environment environment)
    {
        ProjectElementHelper result;

        if (element instanceof TestModule) {
            result = new TestModuleHelper((TestModule) element, environment);
        }
        else if (element instanceof Module) {
            result = new ModuleHelper((Module) element, environment);
        }
        else {
            result = new ProjectHelper((Project) element, environment);
        }

        result.initDependencyGraph();
        return result;
    }

    final public String toString()
    {
        return getName();
    }

    final @NotNull public String getName()
    {
        return proto.getName();
    }

    final public String getId()
    {
        return proto.getId();
    }

    final public Environment getEnv()
    {
        return env;
    }

    @NotNull public ProjectElement getElement()
    {
        if (element == null) {
            throw new IllegalStateException("Not activated element: " + getName());
        }

        return element;
    }

    final public File getBasedir()
    {
        return env.getBaseDir();
    }

    final public void setTopLevel(boolean b)
    {
        topLevel = b;
    }

    final public boolean isTopLevel()
    {
        return topLevel;
    }

    final public String getJdkName()
    {
        return getElement().jdk;
    }

    final public long lastModified()
    {
        return env.sourceLastModified(getElement().getClass());
    }

    final public Collection<Command> listCommands()
    {
        return getBuilder().listCommands();
    }

    protected void initDependencyGraph() {}

    final protected ProjectElement activate()
    {
        return env.activate(proto);
    }

    abstract void build(String commandName);

    void activate(@NotNull ProjectElement activatedElement)
    {
        element = activatedElement;
    }

    final @NotNull private CommandBuilder getBuilder()
    {
        if (builder == null) {
            builder = new CommandBuilder(proto);
        }

        return builder;
    }

    public Command findCommand(String commandName)
    {
        return getBuilder().commands().get(commandName);
    }
}
