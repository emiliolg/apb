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

public class GraphVizInfo {

    @BuildProperty public String tredCommand = "tred";

    @BuildProperty public String dotCommand = "dot";
    
    @BuildProperty public String outputType = "pdf";
}
