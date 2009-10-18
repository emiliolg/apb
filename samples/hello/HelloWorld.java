import apb.metadata.*;
import static apb.tasks.CoreTasks.*;

@DefaultTarget("hello")
public class HelloWorld
    extends ProjectElement
{

    @BuildTarget(description = "Greetings from APB")
    public void hello()
    {
        printf("Hello World !\n");
    }

    @BuildTarget(
                 depends = "hello",
                 description = "Good Bye from APB"
                )
    public void bye()
    {
        printf("Good Bye World !\n");
    }
}
