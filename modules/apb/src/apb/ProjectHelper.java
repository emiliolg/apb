
// ...........................................................................................................
// Copyright (c) 1993, 2009, Oracle and/or its affiliates. All rights reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Oracle Corp.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// Last changed on 2009-04-23 18:08:30 (-0300), by: emilio. $Revision$
// ...........................................................................................................

package apb;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import apb.metadata.Project;
import apb.metadata.ProjectElement;

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Oct 10, 2008
// Time: 12:33:47 PM

//
public class ProjectHelper
    extends ProjectElementHelper
{
    //~ Instance fields ......................................................................................

    @NotNull private final List<ProjectElementHelper> components;

    //~ Constructors .........................................................................................

    ProjectHelper(@NotNull Project project, @NotNull Environment env)
    {
        super(project, env);
        components = new ArrayList<ProjectElementHelper>();

        for (ProjectElement module : project.components()) {
            components.add(env.getHelper(module));
        }
    }

    //~ Methods ..............................................................................................

    public Project getProject()
    {
        return (Project) getElement();
    }

    public Set<ModuleHelper> listAllModules()
    {
        Set<ModuleHelper> result = new LinkedHashSet<ModuleHelper>();

        for (ProjectElementHelper helper : components) {
            result.addAll(helper.listAllModules());
        }

        return result;
    }

    void build(String commandName)
    {
        for (ProjectElementHelper component : components) {
            component.build(commandName);
        }
    }
}
