
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

package apb.metadata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import apb.Environment;
import apb.ModuleHelper;

/**
 * A class representing a Library that will be stored in the file system.
 */
public class LocalLibrary
    implements Dependency
{
    //~ Instance fields ......................................................................................

    public String   path;
    public String runtimePath;
    private boolean optional;
    private String  sourcesPath;

    //~ Constructors .........................................................................................

    protected LocalLibrary(String path)
    {
        this(path, false);
    }
    protected LocalLibrary(String path, String runtimePath)
    {
        this(path, runtimePath, false);
    }

    protected LocalLibrary(String path, String runtimePath, boolean optional)
    {
        this.path = path;
        this.optional = optional;
        this.runtimePath = runtimePath;
    }

    protected LocalLibrary(String path, boolean optional) {
        this(path, null, optional);
    }

    //~ Methods ..............................................................................................


    public File getFile(final Environment env)
    {
        return fileFromBase(env, path);
    }

    public File getSourcesFile(final Environment env)
    {
        return sourcesPath == null ? null : fileFromBase(env, sourcesPath);
    }

    public void setSources(String sources)
    {
        sourcesPath = sources;
    }

    private File fileFromBase(Environment env, String p)
    {
        final File result;

        final File lib = env.fileFromBase(p);

        if (lib.exists()) {
            result = lib.getAbsoluteFile();
        }
        else {
            result = null;

            if (!optional) {
                env.handle("Libray not found: " + lib);
            }
        }

        return result;
    }
}
