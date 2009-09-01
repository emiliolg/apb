
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

package apb.testrunner;

import apb.BuildException;

import apb.utils.ClassUtils;

import org.jetbrains.annotations.NotNull;
//
// User: emilio
// Date: Nov 7, 2008
// Time: 10:02:17 AM

public class Invocation
{
    //~ Instance fields ......................................................................................

    @NotNull private final String className;

    private final Object[] params;

    //~ Constructors .........................................................................................

    public Invocation(@NotNull String className, Object... params)
    {
        this.className = className;
        this.params = params;
    }

    //~ Methods ..............................................................................................

    @NotNull public String getClassName()
    {
        return className;
    }

    public Object[] getParams()
    {
        return params;
    }

    public Object instantiate(ClassLoader classLoader)
    {
        try {
            return ClassUtils.newInstance(classLoader, getClassName(), getParams());
        }
        catch (Exception e) {
            throw new BuildException("Cannot instatiate: '" + getClassName() + "'", e);
        }
    }

    public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append(className);

        if (params.length > 0) {
            result.append('(');

            for (int i = 0; i < params.length; i++) {
                if (i > 0) {
                    result.append(',');
                }

                result.append(params[i]);
            }

            result.append(')');
        }

        return result.toString();
    }
}
