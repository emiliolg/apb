import apb.metadata.Module;

public class DefaultModule
        extends Module {
    {

        version = "0.9.4";

        compiler.lint = true;
        compiler.failOnWarning = true;

        pkg.dir = "../lib";

    }
}
