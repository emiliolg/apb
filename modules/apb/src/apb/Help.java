package apb;

import java.lang.reflect.Method;

import apb.metadata.BuildTarget;
import apb.metadata.ProjectElement;
//
// User: emilio
// Date: Mar 4, 2009
// Time: 3:26:39 PM

public class Help
    extends Command
{
    Help()
    {
        super("help", getHelpMethod(), "List the available commands with a brief description");
    }

    @BuildTarget
    @SuppressWarnings("UnusedDeclaration")
    public static void help(ProjectElement element, Environment env)
    {
        System.err.println("Commands for '" + element.getName() + "' : ");

        for (Command cmd : buildCommands(element.getClass()).values()) {
            if (!cmd.isDefault()) {
                System.err.printf("    %-20s: %s\n", cmd, cmd.getDescription());
            }
        }
        System.exit(0);
    }

    private static Method getHelpMethod()
    {
        try {
            return apb.Help.class.getMethod("help", ProjectElement.class, Environment.class);
        }
        catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
