

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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import static apb.Constants.DEFAULT_REPOSITORY;
import static apb.Constants.REPOSITORY_PROPERTY;

import static apb.tasks.CoreTasks.download;
//
// User: emilio
// Date: Jul 6, 2009
// Time: 4:21:27 PM

//
class ArtifactsCache
{
    //~ Instance fields ......................................................................................

    private final Environment       env;
    private final Map<String, File> map;

    //~ Constructors .........................................................................................

    public ArtifactsCache(Environment environment)
    {
        env = environment;
        map = new HashMap<String, File>();
    }

    //~ Methods ..............................................................................................

    @NotNull public File getArtifact(@NotNull String group, @NotNull String relativeUrl, @NotNull File target)
    {
        final String path = target.getPath();
        File         result = map.get(path);

        if (result == null) {
            String repo = findRepository(group);

            download(repo + "/" + relativeUrl).to(target)  //
                                              .execute();
            map.put(path, target);
            result = target;
        }

        return result;
    }

    /**
     * Find a library repository
     * It tries to find one based on the 'repository.group' property
     * It tries partial group names staring from the complete one to more general ones
     * For example: org.apache.ant, org.apache, org, <empty>
     * If the property is not found returns the DEFAULT_REPOSITORY
     * @param group
     * @return A repository URL
     */
    @NotNull private String findRepository(@NotNull String group)
    {
        String repo = "";

        while (repo.isEmpty() && !group.isEmpty()) {
            repo = env.getProperty(REPOSITORY_PROPERTY + "." + group, "");
            int dot = group.lastIndexOf('.');
            group = dot == -1 ? "" : group.substring(0, dot);
        }

        if (repo.isEmpty()) {
            repo = env.getProperty(REPOSITORY_PROPERTY, DEFAULT_REPOSITORY);
        }

        return repo;
    }
}
