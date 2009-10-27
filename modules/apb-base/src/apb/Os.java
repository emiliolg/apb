

// Copyright 2008-2009 Emilio Lopez-Gabeiras
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License
//


package apb;

/**
 * This class gives information about the current OS
 * It contains a Singleton with the current OS
 */

public class Os
{
    //~ Instance fields ......................................................................................

    private boolean anyUnix;

    private boolean      linux;
    private boolean      macOs;
    private boolean      os2;
    private boolean      windows;
    private final String name;

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

    private static final Os instance = new Os();
}
