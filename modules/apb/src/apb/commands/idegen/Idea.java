package apb.commands.idegen;

import apb.Command;
import apb.Environment;
import apb.tasks.IdeaTask;
import apb.metadata.ProjectElement;
//
// User: emilio
// Date: Mar 18, 2009
// Time: 4:11:52 PM

//
public class Idea extends Command {
    public Idea() {
        super("idegen", "idea", "Generate Idea project and module files.", true);
    }

    public void invoke(ProjectElement projectElement, Environment env) {
        IdeaTask.execute(env);
    }
}
