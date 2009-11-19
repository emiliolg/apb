// ...........................................................................................................
// (C) Copyright  1996/2007 Fuego Inc.  All Rights Reserved
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF Fuego Inc.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
// $Revision: $
// ...........................................................................................................
package apb.utils;

import org.jetbrains.annotations.NotNull;

import java.io.*;

public class StreamUtils {
    public static BufferedOutputStream buffered(final OutputStream os) {
        final BufferedOutputStream result;
        if (os == null || os instanceof BufferedOutputStream) {
            result = (BufferedOutputStream) os;
        } else {
            result = new BufferedOutputStream(os);
        }
        return result;
    }

    public static BufferedWriter buffered(final Writer writer) {
        final BufferedWriter result;
        if (writer == null || writer instanceof BufferedWriter) {
            result = (BufferedWriter) writer;
        } else {
            result = new BufferedWriter(writer);
        }
        return result;
    }

    public static BufferedInputStream buffered(final InputStream os) {
        final BufferedInputStream result;
        if (os == null || os instanceof BufferedInputStream) {
            result = (BufferedInputStream) os;
        } else {
            result = new BufferedInputStream(os);
        }
        return result;
    }

    public static BufferedReader buffered(final Reader writer) {
        final BufferedReader result;
        if (writer == null || writer instanceof BufferedReader) {
            result = (BufferedReader) writer;
        } else {
            result = new BufferedReader(writer);
        }
        return result;
    }

    public static OutputStream noClose(final OutputStream os) {
        return os == null?null:new FilterOutputStream(os) {
            @Override
            public void close() throws IOException {
                flush();
            }
        };
    }

    public static Thread asyncTransfer(@NotNull final InputStream in,
                                 @NotNull final OutputStream out) {
        final Runnable transfer = new Runnable() {
            @Override
            public void run() {
                try {
                    transfer(in, out);
                } catch (IOException e) {
                    assert false : e;
                } finally {
                    close(out);
                }

            }
        };

        final Thread thread = new Thread(transfer, "Stream transfer");
        thread.start();
        return thread;
    }

    public static void transfer(@NotNull final InputStream in, @NotNull final OutputStream out)
            throws IOException
    {
        final byte[] buffer = new byte[4092];
        int n;
        while ((n = in.read(buffer)) > 0) {
            out.write(buffer, 0, n);
        }
    }

    public static void close(Closeable closeable)
    {
        if (closeable != null) {
            try {
                closeable.close();
            }
            catch (IOException e) {
                // Ignore
            }
        }
    }
}
