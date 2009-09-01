

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
import apb.ProjectBuilder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//
// User: emilio
// Date: Jul 3, 2009
// Time: 6:16:57 PM

//
public class RemoteLibrary
    extends Library
{
    //~ Instance fields ......................................................................................

    private final String relativeUrl;

    @NotNull private String targetDir;

    //~ Constructors .........................................................................................

    protected RemoteLibrary(@NotNull String group, @NotNull String id, @NotNull String version)
    {
        super(group, id, version);
        targetDir = "$libraries";
        relativeUrl = group.replace('.', '/') + '/' + id + '/' + version;
    }

    //~ Methods ..............................................................................................

    public void setTargetDir(@NotNull String dir)
    {
        targetDir = dir;
    }

    @Nullable public File getArtifact(@NotNull Environment env, @NotNull PackageType type)
    {
        String name = getArtifactName(type);
        File   result = null;

        if (name != null) {
            File target = env.fileFromBase(targetDir + File.separator + name);
            result =
                ProjectBuilder.getInstance().getArtifactsCache().getArtifact(group, relativeUrl + "/" + name,
                                                                             target);
        }

        return result;
    }

    protected String getArtifactName(PackageType type)
    {
        return type == PackageType.JAR ? id + '-' + version + ".jar" : null;
    }
}
