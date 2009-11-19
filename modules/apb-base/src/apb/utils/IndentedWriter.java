// ...........................................................................................................
// (C) Copyright  1996/2007 Fuego Inc.  All Rights Reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Fuego Inc.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// $Revision: $
// ...........................................................................................................
package apb.utils;

import java.io.IOException;
import java.io.Writer;

public class IndentedWriter
    extends Writer
{
    //~ Instance fields ......................................................................................

    private int     indent;
    private boolean onNewLine = true;

    private final Writer out;

    //~ Constructors .........................................................................................

    public IndentedWriter(Writer out)
    {
        assert out != null : "out is null";
        this.out = out;
    }

    //~ Methods ..............................................................................................

    public void write(char[] cbuf, int off, int len)
        throws IOException
    {
        synchronized (lock) {
            char c;
            //This could be optimized if needed
            for (int i = 0; i < len; i++) {
                if (onNewLine) {
                    writeIndent();
                    onNewLine = false;
                }

                c = cbuf[i + off];
                out.write(c);

                if (c == '\n') {
                    onNewLine = true;
                }
            }
        }
    }

    public void indent()
    {
        synchronized(lock) {
            ++indent;
        }
    }

    public void dedent()
    {
        synchronized(lock) {
            --indent;
            assert indent >= 0 : "too many dedents";
        }
    }

    public void flush()
        throws IOException
    {
        out.flush();
    }

    public void close()
        throws IOException
    {
        out.close();
    }

    private void writeIndent()
        throws IOException
    {
        for (int j = 0; j < indent; j++) {
            out.write('\t');
        }
    }
}
