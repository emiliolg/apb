
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

package apb.metadata;

/**
 * This class defines a Module for the building system
 * Every Module definition must inherit (directly or indirectly) from this class
*/
public class Project
    extends ProjectElement
{
    //~ Instance fields ......................................................................................

    private ProjectElementList components = new ProjectElementList();

    //~ Methods ..............................................................................................

    public ProjectElementList components()
    {
        return components;
    }

    public void components(ProjectElement... ms)
    {
        components.addAll(ms);
    }

    public void clean() {}

    public void resources() {}

    public void compile() {}

    public void compileTests() {}

    public void runTests() {}

    public void runMinimalTests() {}

    public void packageit() {}
}
