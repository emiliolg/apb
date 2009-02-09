import apb.metadata.Module;

public final class ApbAnt
    extends Module
{
    //~ Instance initializers ................................................................................

    {
        description = "APB Ant Tasks";
        dependencies(new Apb(), localLibrary("../lib/annotations.jar"), localLibrary("../lib/ant.jar"));

        pkg.dir = "../lib";
        pkg.name = "ant-apb";
        pkg.addClassPath = true;
        compiler.lint = true;
    }
}
