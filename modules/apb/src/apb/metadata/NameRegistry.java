
// ...........................................................................................................
// Copyright (c) 1993, 2009, Oracle and/or its affiliates. All rights reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Oracle Corp.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// Last changed on 2009-04-20 17:46:16 (-0300), by: emilio. $Revision$
// ...........................................................................................................

package apb.metadata;

import java.util.Map;
import java.util.HashMap;

import org.jetbrains.annotations.NotNull;

/**
 * Private for the package, an utility class that keeps a registry of Objects by class
 * to avoid combinatorial explosion of Dependency objects to be created
 */

class NameRegistry
{
    //~ Methods ..............................................................................................

    @SuppressWarnings("unchecked")
    static<T extends Named> T intern(T obj)
    {
        final String name = obj.getName();
        T result = (T) registry.get(name);

        if (result == null || ! result.getClass().equals(obj.getClass())) {
            registry.put(name, obj);
            result = obj;
        }

        return result;
    }

    //~ Static fields/initializers ...........................................................................

    private static final @NotNull
    Map<String, Named> registry = new HashMap<String, Named>();
}
