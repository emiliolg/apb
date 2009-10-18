
import apb.metadata.*;

public class PlayWithMath
    extends Module
{
    {
        dependencies(localLibrary("lib/samples-math-1.0.jar"));

        group = "samples";
        version = "1.0";
        pkg.mainClass = "Play";
        pkg.addClassPath = true;
    }
}
