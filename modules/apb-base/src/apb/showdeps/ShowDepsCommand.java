// ...........................................................................................................
// (C) Copyright  1996/2007 Fuego Inc.  All Rights Reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Fuego Inc.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// $Revision: $
// ...........................................................................................................
package apb.showdeps;

import apb.Command;
import apb.ModuleHelper;
import apb.ProjectElementHelper;
import apb.metadata.Module;
import apb.metadata.Project;
import apb.metadata.ProjectElement;
import apb.metadata.TestModule;
import static apb.utils.FileUtils.createOutputStream;
import apb.utils.IndentedWriter;
import static apb.utils.StreamUtils.buffered;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

public abstract class ShowDepsCommand
    extends Command {
    protected ShowDepsInfo info;//These are initialized in the start() method
    private IndentedWriter iw;
    protected File targetDir;
    protected File outputFile;

    public ShowDepsCommand(@NotNull final String name) {
        super("showdeps", name, "Output the dependency graph using " + name, false, Project.class, Module.class);
    }

    @Override
    public final void invoke(final ProjectElement element) {
        info = element.getHelper().getInfoObject("showdeps", ShowDepsInfo.class);
        start(element);
        traverse(element, new HashSet<ProjectElement>());
        finish(element);
    }

    protected void start(final ProjectElement element) {}
    protected void finish(final ProjectElement element) {}

    private void traverse(final ProjectElement element, final Set<ProjectElement> visited) {
        if(!visited.contains(element)) {
            visited.add(element);
            if(isIncluded(element)) {
                visitNode(element);
            }
            if (element instanceof Project) {
                final Project project = (Project) element;
                for (final ProjectElement component : project.components()) {
                    if(isIncluded(project) && isIncluded(component)) {
                        visitComponent(project, component);
                    }
                    traverse(component, visited);
                }
            }
            for (final ModuleHelper moduleHelper : element.getHelper().getDependencies()) {
                final Module module = moduleHelper.getModule();
                if(isIncluded(element) && isIncluded(module)) {
                    visitDependency(element, module);
                }
                traverse(module, visited);
            }
        }
    }

    private boolean isIncluded(final ProjectElement element) {
        //Exclude tests
        return info.includeTestModules || !(element instanceof TestModule || element.getId().matches("(?i:.*test.*)"));
    }

    protected abstract void visitDependency(final ProjectElement element,final Module module);
    protected abstract void visitComponent(final Project project, final ProjectElement component);
    protected abstract void visitNode(final ProjectElement node);

    protected void println(final Object o) {
        print(o);
        print("\n");
    }

    protected void print(final Object o) {
        try {
            iw.append(String.valueOf(o));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void indent() {
        iw.indent();
    }

    protected void dedent() {
        iw.dedent();
    }

    protected void createOutputFile(@NotNull final String outputFile,
                                    @NotNull final ProjectElementHelper helper) {
        targetDir = helper.fileFromBase(info.dir);
        this.outputFile = new File(targetDir, outputFile);
        helper.logInfo("Generating: %s\n", this.outputFile);
        try {
            iw = new IndentedWriter(buffered(new OutputStreamWriter(buffered(createOutputStream(this.outputFile, false)), "UTF-8")));
        } catch (UnsupportedEncodingException e) {
            assert false : e;
        } catch (FileNotFoundException e) {
            assert false : e;
        }
    }

    protected void closeOutput() throws IOException {
        iw.close();
    }
}
