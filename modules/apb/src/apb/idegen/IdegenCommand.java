

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


package apb.idegen;

import org.jetbrains.annotations.NotNull;

import apb.Command;
import apb.metadata.ProjectElement;
//
// User: emilio
// Date: Aug 19, 2009
// Time: 5:42:56 PM

//
public abstract class IdegenCommand
    extends Command
{
    //~ Constructors .........................................................................................

    public IdegenCommand(@NotNull String name)
    {
        super(ProjectElement.class, "idegen", name, "Generate " + name + " project and module files.", true);
    }
}
