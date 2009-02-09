
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
 * A class representing a Library that will be fetched from a repository
 */
public class Library
    implements Dependency
{
    //~ Instance fields ......................................................................................

    protected String group;
    protected String id;
    protected String version;

    //~ Methods ..............................................................................................

    public Library version(String v)
    {
        version = v;
        return this;
    }

    static Library create(String group, String id, String version)
    {
        Library result = new Library();
        result.group = group;
        result.id = id;
        result.version = version;
        return result;
    }
}
