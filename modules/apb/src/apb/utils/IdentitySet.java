

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

import java.util.AbstractSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
//
// User: emilio
// Date: Oct 15, 2008
// Time: 12:10:13 PM

//
public class IdentitySet<T>
    extends AbstractSet<T>
{
    //~ Instance fields ......................................................................................

    private final IdentityHashMap<T, T> map = new IdentityHashMap<T, T>();

    //~ Methods ..............................................................................................

    @SuppressWarnings({ "SuspiciousMethodCalls" })
    public boolean contains(Object element)
    {
        return map.containsKey(element);
    }

    @Override public Iterator<T> iterator()
    {
        return map.keySet().iterator();
    }

    @Override public int size()
    {
        return map.size();
    }

    public boolean add(T element)
    {
        return map.put(element, element) == null;
    }

    public void clear()
    {
        map.clear();
    }
}
