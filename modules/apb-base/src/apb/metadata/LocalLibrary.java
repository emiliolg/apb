

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
 * A class representing a Library that will be stored in the file system.
 */
public class LocalLibrary
    extends Library
{
    //~ Instance fields ......................................................................................

    /**
     * The path to the library
     */
    @NotNull public final String path;
    @Nullable public String      runtimePath;
    private final boolean        optional;

    /**
     * The path to the javadoc
     */
    @Nullable private String docPath;

    /**
     * The path to Sources;
     */
    @Nullable private String sourcesPath;

    /**
     * An (optional) subpath inside the sources jar
     */
    @NotNull private String sourcesSubPath;

    //~ Constructors .........................................................................................

    public LocalLibrary(@NotNull String path)
    {
        this(path, false);
    }

    public LocalLibrary(@NotNull String path, boolean optional)
    {
        super("", path, "");
        this.path = path;
        this.optional = optional;
        NameRegistry.intern(this);
    }

    //~ Methods ..............................................................................................

    /**
     * Allow to return a subpath inside a Jar for Sources...
     * @param type
     */
    @Override public String getSubPath(PackageType type)
    {
        return type == PackageType.SRC ? sourcesSubPath : super.getSubPath(type);
    }

    @Nullable public File getArtifact(@NotNull Environment env, @NotNull PackageType type)
    {
        switch (type) {
        case SRC:
            return fileFromBase(env, sourcesPath, true);
        case DOC:
            return fileFromBase(env, docPath, true);
        default:
            return fileFromBase(env, path, false);
        }
    }

    public void setSources(@NotNull String sources)
    {
        setSources(sources, "");
    }

    public void setSources(@NotNull String sources, @NotNull String subPath)
    {
        sourcesPath = sources;
        sourcesSubPath = subPath;
    }

    public void setDoc(@NotNull String doc)
    {
        docPath = doc;
    }

    protected void setRuntimePath(@Nullable String runtimePath)
    {
        this.runtimePath = runtimePath;
    }

    @Nullable private File fileFromBase(@NotNull Environment env, @Nullable String filePath, boolean ignore)
    {
        File result = null;

        if (filePath != null) {
            final File lib = env.fileFromBase(filePath);

            if (lib.exists()) {
                result = lib;
            }
            else if (!optional && !ignore) {
                env.handle("Library not found: " + lib);
            }
        }

        return result;
    }
}
