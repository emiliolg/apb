
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

import java.util.ArrayList;
import java.util.List;

import apb.Environment;

import static java.util.Arrays.asList;

/**
 * This class defines a Module for the building system
 * Every Module definition must inherit (directly or indirectly) from this class
*/
public class Project
    extends ProjectElement
{
    //~ Instance fields ......................................................................................

    private List<ProjectElement> components = new ArrayList<ProjectElement>();

    //~ Methods ..............................................................................................

    public List<ProjectElement> components()
    {
        return components;
    }

    public void components(Module... ms)
    {
        components.addAll(asList(ms));
    }

    public void clean(Environment env) {}

    public void resources(Environment env) {}

    public void compile(Environment env) {}

    public void compileTests(Environment env) {}

    public void runTests(Environment env) {}

    public void packageit(Environment env) {}
}
