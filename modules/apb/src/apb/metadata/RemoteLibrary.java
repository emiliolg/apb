

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
import java.util.Collection;
import java.util.Collections;

import apb.Environment;

import apb.tasks.DownloadTask;
import apb.tasks.Task;

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

    @Nullable private File libraryFile;
    @NotNull private String path;

    //~ Constructors .........................................................................................

    protected RemoteLibrary(@NotNull String group, @NotNull String id, @NotNull String version)
    {
        super(group, id, version);
        path =  "$libraries/" + id + ".jar";

    }

    public void setPath(@NotNull String path)
    {
        this.path = path;
    }

    //~ Methods ..............................................................................................

    public Collection<File> getFiles(@NotNull final Environment env)
    {
        return Collections.singleton(fileFromBase(env));
    }

    @Nullable public File getSourcesFile(@NotNull final Environment env)
    {
        return null;
    }

    @NotNull private File fileFromBase(@NotNull Environment env)
    {
        File target = libraryFile;

        if (target == null) {
            target = getTarget(env);
            Task t = new DownloadTask(env, getSourceUrl(env), target.getPath());
            t.execute();
            libraryFile = target;
        }

        return target;
    }

    @NotNull private File getTarget(@NotNull Environment env)
    {
        return env.fileFromBase(path);
    }

    private String getSourceUrl(Environment env)
    {
        String repo = env.getProperty("repository." + group, "");

        if (repo.isEmpty()) {
            repo = env.getProperty("repository", DEFAULT_REPOSITORY);
        }

        return repo + '/' + group.replace('.', '/') + '/' + id + '/' + version + '/' + id + '-' + version + ".jar";
    }

    //~ Static fields/initializers ...........................................................................

    private static String DEFAULT_REPOSITORY = "http://mirrors.ibiblio.org/pub/mirrors/maven2";
}
