

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

import java.util.ArrayList;
//
// User: emilio
// Date: Sep 3, 2008
// Time: 12:29:57 PM

//
public class ProjectElementList
    extends ArrayList<ProjectElement>
{
    //~ Methods ..............................................................................................

    @Override public boolean add(ProjectElement o)
    {
        return super.add(NameRegistry.intern(o));
    }

    public void addAll(ProjectElement[] elements)
    {
        for (ProjectElement e : elements) {
            add(e);
        }
    }

    //~ Static fields/initializers ...........................................................................

    private static final long serialVersionUID = -3277940314952780349L;
}
