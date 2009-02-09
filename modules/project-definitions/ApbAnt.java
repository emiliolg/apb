
// ...........................................................................................................
// (C) Copyright  1996/2008 Fuego Inc.  All Rights Reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Fuego Inc.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// Last changed on 2008-12-10 20:02:44 (-0200), by: emilio. $Revision$
// ...........................................................................................................


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
