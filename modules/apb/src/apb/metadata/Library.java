

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


package apb.metadata;

import java.io.File;

import apb.Environment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class representing a Library that will be fetched from a repository
 */
abstract public class Library
    implements Dependency
{
    //~ Instance fields ......................................................................................

    protected @NotNull
    final String group;
    protected @NotNull
    final String id;
    protected @NotNull
    final String version;

    //~ Constructors .........................................................................................

    protected Library(@NotNull String group, @NotNull String id, @NotNull String version)
    {
        this.group = group;

        this.version = version;
        this.id = id;
        NameRegistry.intern(this);
    }

    //~ Methods ..............................................................................................

    @NotNull public String getName()
    {
        return group + ( group.isEmpty() ? "" :  ".") + version + ( version.isEmpty() ? "" :  ".") + id;
    }

    public String toString()
    {
        return getName();
    }

    @NotNull public final Module asModule()
    {
        throw new UnsupportedOperationException();
    }

    public final boolean isModule()
    {
        return false;
    }

    public final boolean isLibrary()
    {
        return true;
    }

    @NotNull public final Library asLibrary()
    {
        return this;
    }

    @Nullable
    abstract public File getArtifact(@NotNull Environment env, @NotNull PackageType type);

    public boolean mustInclude(boolean compile)
    {
        return true;
    }
}
