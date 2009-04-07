package apb.commands.idegen.idea;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
//
// User: emilio
// Date: Mar 30, 2009
// Time: 3:35:17 PM

//
public class Library
{
    //~ Instance fields ......................................................................................

    private final String name;

    private final Set<File> paths;

    //~ Constructors .........................................................................................

    public Library(String libraryName)
    {
        name = libraryName == null ? "##local" : libraryName;
        paths = new HashSet<File>();
    }

    //~ Methods ..............................................................................................

    public String getName()
    {
        return name;
    }

    public Set<File> getPaths()
    {
        return paths;
    }

    public void add(File path)
    {
        if (path != null) {
            try {
                paths.add(path.getCanonicalFile());
            }
            catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    @Override public String toString()
    {
        return name;
    }
}
