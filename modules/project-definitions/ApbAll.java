
// ...........................................................................................................
// (C) Copyright  1996/2008 Fuego Inc.  All Rights Reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Fuego Inc.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// Last changed on 2008-12-10 20:02:43 (-0200), by: emilio. $Revision$
// ...........................................................................................................

//
// User: emilio
// Date: Dec 9, 2008
// Time: 5:35:09 PM

//

import apb.metadata.Project;

public class ApbAll
    extends Project
{
    //~ Instance initializers ................................................................................

    {
        components(new Apb(), new ApbAnt());
    }
}
