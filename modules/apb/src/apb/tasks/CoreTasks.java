

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


package apb.tasks;

import java.io.File;

import apb.Apb;

import org.jetbrains.annotations.NotNull;

public class CoreTasks
{
    //~ Methods ..............................................................................................

    /**
     * Entry point to the fluent interface.
     * @param from The File or Directory to copy from
     */
    @NotNull public static Copy copy(@NotNull String from)
    {
        return copy(new File(from));
    }

    /**
     * Entry point to the fluent interface.
     * @param from The File or Directory to copy from
     */
    @NotNull public static Copy copy(@NotNull File from)
    {
        return new Copy(Apb.getEnv(), from, false);
    }

    /**
     * Entry point to the fluent interface.
     * @param from The File or Directory to copy from
     */
    @NotNull public static Copy copyFiltering(@NotNull String from)
    {
        return copyFiltering(new File(from));
    }

    /**
     * Entry point to the fluent interface.
     * @param from The File or Directory to copy from
     */
    @NotNull public static Copy copyFiltering(@NotNull File from)
    {
        return new Copy(Apb.getEnv(), from, true);
    }

    public static void main(String[] args)
    {
        copy("/x/dir").to("/y/dir").excluding("*.class").execute();
    }
}
