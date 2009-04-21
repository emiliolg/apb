
// ...........................................................................................................
// Copyright (c) 1993, 2009, Oracle and/or its affiliates. All rights reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Oracle Corp.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// Last changed on 2009-04-21 09:54:29 (-0300), by: emilio. $Revision$
// ...........................................................................................................

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

    //~ Constructors .........................................................................................

    private Library()
    {
        NameRegistry.intern(this);
    }

    //~ Methods ..............................................................................................

    public Library version(String v)
    {
        version = v;
        return this;
    }

    public String getName()
    {
        return group + "." + version + "." + id;
    }

    public String toString()
    {
        return getName();
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
