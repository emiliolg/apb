
import apb.metadata.*;

import static apb.tasks.CoreTasks.*;

public class Chat
    extends HelloWorld
{
    @BuildTarget(
                 depends = "hello",
                 before = "bye"
                )
    public void chat()
    {
        printf("Nice to see you again. It has been a long time.\n");
    }
}
