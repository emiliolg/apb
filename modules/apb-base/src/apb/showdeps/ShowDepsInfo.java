// ...........................................................................................................
// (C) Copyright  1996/2007 Fuego Inc.  All Rights Reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Fuego Inc.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// $Revision: $
// ...........................................................................................................
package apb.showdeps;

import apb.metadata.BuildProperty;

public class ShowDepsInfo {
    /**
     * Wheter to include test modules directories in the dependency graph
     */
    @BuildProperty
    public boolean includeTestModules = false;

    /**
     * The directory where the graph will be placed
     */
    @BuildProperty public String dir = "$output-base/showdeps";

}