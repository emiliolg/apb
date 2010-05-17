// ...........................................................................................................
// (C) Copyright  1996/2007 Fuego Inc.  All Rights Reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Fuego Inc.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// $Revision: $
// ...........................................................................................................
package apb.showdeps;

import apb.metadata.Module;
import apb.metadata.Project;
import apb.metadata.ProjectElement;
import apb.metadata.TestModule;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.jetbrains.annotations.NotNull;

public class Text
        extends ShowDepsCommand
{
    private List<Project> projects = new ArrayList<Project>();
    private List<Module> modules = new ArrayList<Module>();
    private List<TestModule> testModules = new ArrayList<TestModule>();
    private Map<ProjectElement, Set<ProjectElement>> dependencies = new HashMap<ProjectElement,Set<ProjectElement>>();
    private Map<ProjectElement,Set<ProjectElement>> components = new HashMap<ProjectElement,Set<ProjectElement>>();

    public Text() {
        super("text");
    }

    @Override
    protected void start(final ProjectElement element) {
        createOutputFile(element.getId() + ".txt", element.getHelper());
    }

    @Override
    protected void finish(ProjectElement element) {
        for (final ProjectElement projectElement : sortElements()) {
            if (element != projectElement) {
                println(projectElement.getId());
            }
        }

        try {
            closeOutput();
        } catch (IOException e) {
            element.getHelper().logSevere("ERROR: closing file %s: %s", targetDir, e.getMessage());
        }
    }

    private List<ProjectElement> sortElements() {
        Collections.sort(projects, ELEMENT_COMPARATOR);
        Collections.sort(modules, ELEMENT_COMPARATOR);
        Collections.sort(testModules, ELEMENT_COMPARATOR);
        final List<ProjectElement> all = new ArrayList<ProjectElement>(projects);
        all.addAll(modules);
        all.addAll(testModules);
        return all;
    }

    @Override
    protected void visitDependency(final ProjectElement element,final  Module module) {
        Set<ProjectElement> elementSet = dependencies.get(element);
        if(elementSet == null) {
            elementSet = new HashSet<ProjectElement>();
            dependencies.put(element, elementSet);
        }
        elementSet.add(module);
    }

    @Override
    protected void visitComponent(final Project project,final  ProjectElement component) {
        Set<ProjectElement> elementSet = components.get(project);
        if(elementSet == null) {
            elementSet = new HashSet<ProjectElement>();
            components.put(project, elementSet);
        }
        elementSet.add(component);
    }

    @Override
    protected void visitNode(final ProjectElement node) {
        if (node instanceof Project) {
            projects.add((Project) node);
        } else if(node instanceof TestModule) {
            testModules.add((TestModule) node);
        } else {
            modules.add((Module) node);
        }
    }

    private static final Comparator<ProjectElement> ELEMENT_COMPARATOR = new Comparator<ProjectElement>() {
        @Override
        public int compare(ProjectElement o1, ProjectElement o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };
}