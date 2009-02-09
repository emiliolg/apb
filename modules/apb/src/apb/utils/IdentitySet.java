
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

package apb.utils;

import java.util.IdentityHashMap;
//
// User: emilio
// Date: Oct 15, 2008
// Time: 12:10:13 PM

//
public class IdentitySet<T>
{
    //~ Instance fields ......................................................................................

    private final IdentityHashMap<T, T> map = new IdentityHashMap<T, T>();

    //~ Methods ..............................................................................................

    public boolean contains(T element)
    {
        return map.containsKey(element);
    }

    public void add(T element)
    {
        map.put(element, element);
    }

    public void clear()
    {
        map.clear();
    }
}
