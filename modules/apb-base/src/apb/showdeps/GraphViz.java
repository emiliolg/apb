// ...........................................................................................................
// (C) Copyright  1996/2007 Fuego Inc.  All Rights Reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Fuego Inc.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// $Revision: $
// ...........................................................................................................
package apb.showdeps;

import apb.ProjectElementHelper;
import apb.metadata.Module;
import apb.metadata.Project;
import apb.metadata.ProjectElement;
import static apb.utils.FileUtils.createOutputStream;
import static apb.utils.StreamUtils.asyncTransfer;
import static apb.utils.StreamUtils.buffered;
import static apb.utils.StreamUtils.noClose;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import static java.lang.Character.isLetterOrDigit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GraphViz
        extends ShowDepsCommand {

    private final Map<ProjectElement,String> idMap = new HashMap<ProjectElement,String>();
    private final Set<String> usedIds = new HashSet<String>();
    private GraphVizInfo graphVizInfo;

    public GraphViz() {
        super("graphviz");
    }



    @NotNull
    private String id(@NotNull final ProjectElement element) {
        String id = idMap.get(element);
        if(id == null) {
            id = makeUniqueId(element.getId());
            idMap.put(element,id);
        }
        return id;
    }

    @NotNull
    private String makeUniqueId(@NotNull final String id) {
        final StringBuilder sb = new StringBuilder(id.length());
        for(int i = 0; i < id.length(); i++) {
            final char c = id.charAt(i);
            if(isLetterOrDigit(c) || c == '_') {
                sb.append(c);
            }
        }

        if(sb.length() == 0) {
            sb.append('n');
        }

        final String candidate = sb.toString();
        boolean added = usedIds.add(candidate);
        int count = 0;
        while(!added) {
            added = usedIds.add(candidate + count);
            ++count;
        }

        return count == 0?candidate:candidate+count;
    }

    @Override
    protected void visitDependency(final ProjectElement element, final Module module) {
        print(id(element));
        print(" -> ");
        print(id(module));
        println(";");
    }


    @Override
    protected void visitComponent(final Project project, final ProjectElement component) {
        print(id(project));
        print(" -> ");
        print(id(component));
        println("[style=dashed, weight=" + graphVizInfo.projectEdgeWeight + "];");
    }

    @Override
    protected void visitNode(final ProjectElement node) {
        print(id(node));
        print(" [label=\"");
        print(node.getId());
        print("\"");
        if(node instanceof Project) {
            print(", shape=box");
            print(", fillcolor=green");
            print(", style=filled");
        }
        println("];");
    }



    @Override
    protected void start(final ProjectElement element) {
        final ProjectElementHelper helper = element.getHelper();
        graphVizInfo = helper.getInfoObject("graphviz", GraphVizInfo.class);
        createOutputFile(element.getId() + ".gv", helper);

        println("strict digraph {");
        indent();
    }

    @Override
    protected void finish(final ProjectElement element) {
        dedent();
        println("}");
        final ProjectElementHelper helper = element.getHelper();
        try {
            closeOutput();
            final File tredFile = new File(targetDir, helper.getId() + ".tred");
            final File outFile = new File(targetDir, helper.getId() + "." + graphVizInfo.outputType);

            helper.logInfo("Generating: %s\n", tredFile);
            exec(tredFile, graphVizInfo.tredCommand, outputFile.toString());

            helper.logInfo("Generating: %s\n", outFile);
            exec(outFile, graphVizInfo.dotCommand, "-T" + graphVizInfo.outputType, tredFile.toString());
        } catch (IOException e) {
            //TODO: error handling?
            helper.logSevere("Error: %s\n", e.getMessage());
        }
    }

    private static void exec(final File output, final String... command) throws IOException {
        final ProcessBuilder pb = new ProcessBuilder(command);
        final Process process = pb.start();
        asyncTransfer(process.getInputStream(), buffered(createOutputStream(output)));
        asyncTransfer(process.getErrorStream(), noClose(System.err));
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new InterruptedIOException(e.getMessage());
        }

    }

}
