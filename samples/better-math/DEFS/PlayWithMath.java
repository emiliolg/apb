import apb.metadata.*;

public class PlayWithMath
    extends DefaultModule
{
    {
        dependencies(new Math());

        group = "samples";
        version = "1.0";
        pkg.mainClass = "Play";
        pkg.addClassPath = true;
    }
}
