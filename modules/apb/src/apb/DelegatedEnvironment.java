

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

import apb.utils.DebugOption;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DelegatedEnvironment
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
        super(parent.getLogger());
        this.parent = parent;
    }

    //~ Methods ..............................................................................................

    @Override public boolean isFailOnError()
    {
        return parent.isFailOnError();
    }

    @Override public void setNonRecursive(boolean b)
    {
        parent.setNonRecursive(b);
    }

    /**
     * Returns true if log level is quiet
     * @return true if log level is quiet
     */
    public boolean isQuiet()
    {
        return parent.isQuiet();
    }

    /**
     * Returns true if the build must NOT proceed recursive to the module dependecies
     */
    public boolean isNonRecursive()
    {
        return parent.isNonRecursive();
    }

    /**
     * Returns true if we want the build to proceed unconditionally without checking file timestamps
     * @return true if we want the build to proceed unconditionally without checking file timestamps
     */
    public boolean forceBuild()
    {
        return parent.forceBuild();
    }

    /**
     * Returns true if log level is verbose
     * @return true if log level is verbose
     */
    public boolean isVerbose()
    {
        return parent.isVerbose();
    }

    /**
     * Returns true if must show the following option
     */
    public boolean mustShow(DebugOption option)
    {
        return parent.mustShow(option);
    }

    @NotNull @Override public Collection<File> getExtClassPath()
    {
        // Optimization to use parent extClassPath if the property was not present in this environment.

        return retrieveLocalProperty(EXT_PATH_PROPERTY) != null ? super.getExtClassPath()
                                                                : parent.getExtClassPath();
    }

    @Nullable protected String retrieveProperty(@NotNull String id)
    {
        String result = retrieveLocalProperty(id);
        return result == null ? parent.getOptionalProperty(id) : result;
    }
}
