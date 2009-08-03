

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


package apb.utils;
//
// User: emilio
// Date: Jul 31, 2009
// Time: 4:52:35 PM

//
public enum DebugOption
{
    DEPENDENCIES,
    PROPERTIES,
    TASK_INFO,
    TRACK;

    public static final String ALL = "all";

    @Override public String toString()
    {
        return super.toString().toLowerCase();
    }

    public static DebugOption find(String name)
    {
        for (DebugOption option : values()) {
            if (option.toString().equalsIgnoreCase(name)) {
                return option;
            }
        }

        return null;
    }
}
