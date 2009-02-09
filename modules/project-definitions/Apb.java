import apb.metadata.Module;

public final class Apb
    extends Module
{
    //~ Instance initializers ................................................................................

    {
        description = "APB Project Builder";
        dependencies(localLibrary("../lib/annotations.jar"), localLibrary("../lib/junit3.jar"));

        pkg.mainClass = "apb.Main";
        pkg.dir = "../lib";
        pkg.name = "apb";
        pkg.addClassPath = true;
        pkg.generateSourcesJar = true;

        compiler.lint = true;

        javadoc.deprecatedList = false;
        javadoc.links("http://java.sun.com/javase/6/docs/api");
    }
}
