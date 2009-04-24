package apb.commands.idegen;

import apb.Command;
import apb.Environment;
import apb.metadata.ProjectElement;
//
// User: emilio
// Date: Mar 18, 2009
// Time: 4:11:52 PM

//
public class Eclipse extends Command {
    public Eclipse() {
        super("idegen", "eclipse", "Generate Eclipse project files.", true);
    }

    public void invoke(ProjectElement projectElement, Environment env) {
        System.out.println("Not yet implemented");
    }
}