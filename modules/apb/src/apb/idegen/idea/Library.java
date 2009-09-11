

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


package apb.idegen.idea;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import apb.utils.FileUtils;
//
// User: emilio
// Date: Mar 30, 2009
// Time: 3:35:17 PM

//
public class Library
{
    //~ Instance fields ......................................................................................

    private final Set<File> paths;

    private final String name;

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
            paths.add(FileUtils.normalizeFile(path));
        }
    }

    @Override public String toString()
    {
        return name;
    }
}
