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

public class Html
        extends ShowDepsCommand
{
    private List<Project> projects = new ArrayList<Project>();
    private List<Module> modules = new ArrayList<Module>();
    private List<TestModule> testModules = new ArrayList<TestModule>();
    private Map<ProjectElement, Set<ProjectElement>> dependencies = new HashMap<ProjectElement,Set<ProjectElement>>();
    private Map<ProjectElement,Set<ProjectElement>> components = new HashMap<ProjectElement,Set<ProjectElement>>();

    public Html() {
        super("html");
    }

    @Override
    protected void start(final ProjectElement element) {
        createOutputFile(element.getId() + ".html", element.getHelper());
    }

    @Override
    protected void finish(ProjectElement element) {
        final List<ProjectElement> all = sortElements();

        emmitHeader(all);
        for (int i = 0; i < all.size(); i++) {
            final ProjectElement projectElement = all.get(i);
            emmitRow(i, projectElement, all);
        }
        emmitFooter();

        try {
            closeOutput();
        } catch (IOException e) {
            element.getHelper().logSevere("ERROR: closing file %s: %s", targetDir, e.getMessage());
        }
    }

    private void emmitHeader(final List<ProjectElement> all) {
        begin("html");
        begin("head");
        inline("title", "Dependency Matrix");
        println("");
        begin("style", att("type", "text/css"));
        println("td { width:12pt; text-align:center; }");
        println("table { border-collapse:collapse; }");
        println("table, th, td { border: 1px solid black; }");
        end("style");
        end("head");
        begin("body");
        begin("table");
        begin("thead");

        begin("tr");
        inline("th","---");
        inline("th","");
        for (int i = 0; i < all.size(); i++) {
            inline("th", String.valueOf(i));
        }
        end("tr");
        end("thead");
        begin("tbody");


    }

    private void inline(String tag, String text) {
        print("<");
        print(tag);
        print(">");
        print(text);
        print("</");
        print(tag);
        print(">");
    }

    static class Att {
        final String name;
        final String value;
        Att(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return name + "=\"" + value + '\"';
        }
    }

    private Att att(final String name, final String value) {
        return new Att(name, value);
    }


    private void begin(String tag, Att ...atts ) {
        print("<");
        print(tag);
        for (Att att : atts) {
            print(" ");
            print(att.toString());
        }
        println(">");
        indent();
    }

    private void emmitFooter() {
        end("tbody");
        end("table");
        end("body");
        end("html");
    }

    private void end(String tag) {
        dedent();
        print("</");
        print(tag);
        println(">");
    }

    private void emmitRow(final int index, @NotNull final ProjectElement element, @NotNull final List<ProjectElement> all) {
        begin("tr");
        inline("th",element.getId()); //Puajj!
        inline("th","(" + index+ ")"); //Puajj!

        final Set<ProjectElement> elementDeps = dependencies.get(element);
        final Set<ProjectElement> elementComps = components.get(element);

        for (int i = 0; i < all.size(); i++) {
            final ProjectElement projectElement =  all.get(i);
            char c = ' ';
            if(i == index) {
                c = '.';
            } else {
                if(elementDeps != null && elementDeps.contains(projectElement)) {
                    c = 'D';
                } else if (elementComps != null && elementComps.contains(projectElement)) {
                    c = 'C';
                }
            }
            inline("td", "" + c);
        }

        end("tr");
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
