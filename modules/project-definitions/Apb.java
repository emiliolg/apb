
// ...........................................................................................................
// (C) Copyright  1996/2008 Fuego Inc.  All Rights Reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Fuego Inc.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// Last changed on 2008-12-02 18:36:11 (-0200), by: emilio. $Revision$
// ...........................................................................................................


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
