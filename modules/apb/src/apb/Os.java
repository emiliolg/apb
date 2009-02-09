
// ...........................................................................................................
// (C) Copyright  1996/2008 Fuego Inc.  All Rights Reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Fuego Inc.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// Last changed on 2008-12-12 12:41:21 (-0200), by: emilio. $Revision$
// ...........................................................................................................

package apb;

/**
 * This class gives information about the current OS
 * It contains a Singleton with the current OS
 */

public class Os
{
    //~ Instance fields ......................................................................................

    private boolean anyUnix;

    private boolean linux;
    private boolean macOs;
    private String  name;
    private boolean os2;
    private boolean windows;

    //~ Constructors .........................................................................................

    private Os()
    {
        name = System.getProperty("os.name");

        anyUnix = true;

        if (name.contains("Windows")) {
            windows = true;
            anyUnix = false;
        }
        else if ("OS/2".equals(name)) {
            os2 = true;
            anyUnix = false;
        }
        else if (name.contains("Mac")) {
            macOs = true;
        }
        else if (name.contains("Linux")) {
            linux = true;
        }
    }

    //~ Methods ..............................................................................................

    /**
     * Return the singleton Os instance
     * @return An Os instance (Singleton)
     */
    public static Os getInstance()
    {
        return instance;
    }

    /**
     * Returns the Os name according to Java
     * @return The Os name according to Java
     */
    public String getName()
    {
        return name;
    }

    /**
     * Is the OS any Windows variation?
     * @return true if the OS is any Windows variation
     */
    public boolean isWindows()
    {
        return windows;
    }

    /**
     * Is the OS OS/2?
     * @return true if the OS is OS/2
     */
    public boolean isOs2()
    {
        return os2;
    }

    /**
     * Is the OS any MacOs variation?
     * @return true if the OS is any MacOs variation
     */
    public boolean isMacOs()
    {
        return macOs;
    }

    /**
     * Is the OS any Linux variation?
     * @return true if the OS is any Linux variation
     */
    public boolean isLinux()
    {
        return linux;
    }

    /**
     * Is the OS any Unix variation?
     * (Including Linux, Solaris, HP-UX, MacOsX, etc...
     * @return true if the OS is any Unix variation
     */
    public boolean isAnyUnix()
    {
        return anyUnix;
    }

    //~ Static fields/initializers ...........................................................................

    private static Os instance = new Os();
}
