

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

import apb.metadata.ProjectElement;
//
// User: emilio
// Date: Mar 18, 2009
// Time: 4:11:52 PM

/**
 * @exclude
 */
public class Eclipse
    extends IdegenCommand
{
    //~ Constructors .........................................................................................

    /**
     * Constructs a Eclipse IdegenCommand instance
     */
    public Eclipse()
    {
        super("eclipse");
    }

    //~ Methods ..............................................................................................

   /**
    * This is the method implementation that will be invoked when running this IdegenCommand over a Module or Project
    *
    * @param projectElement The Module or Project to be processes.
    */
    public void invoke(ProjectElement projectElement)
    {
        System.out.println("Not yet implemented");
    }
}
