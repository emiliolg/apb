

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
import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DelegatedEnvironment
    extends DefaultEnvironment
{
    //~ Instance fields ......................................................................................

    @NotNull private final Environment parent;

    //~ Constructors .........................................................................................

    /**
     * Properties for the current environment
     */

    public DelegatedEnvironment(@NotNull Environment parent)
    {
        this.parent = parent;
    }

    //~ Methods ..............................................................................................

    public void logInfo(String msg, Object... args)
    {
        parent.logInfo(msg, args);
    }

    public void logWarning(String msg, Object... args)
    {
        parent.logWarning(msg, args);
    }

    public void logSevere(String msg, Object... args)
    {
        parent.logSevere(msg, args);
    }

    public void logVerbose(String msg, Object... args)
    {
        parent.logVerbose(msg, args);
    }

    public Collection<File> getExtClassPath()
    {
        return parent.getExtClassPath();
    }

    @Nullable protected String retrieveProperty(@NotNull String id)
    {
        String result = super.getOptionalProperty(id);
        return result == null ? parent.getOptionalProperty(id) : result;
    }
}
