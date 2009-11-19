

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


package apb.tests.utils;

import java.io.*;

import apb.utils.ColorUtils;
import apb.utils.StringUtils;
import apb.utils.StreamUtils;

import junit.framework.TestCase;
//
public class StreamTest
    extends TestCase
{
    //~ Methods ..............................................................................................

    public void testBufferedInputStream()
    {
        assertNull(StreamUtils.buffered((InputStream)null));
        final BufferedInputStream bis = new BufferedInputStream(System.in);
        assertSame(StreamUtils.buffered(bis), bis);
        final InputStream nbis = new FilterInputStream(System.in){};
        assertNotSame(StreamUtils.buffered(nbis), nbis);
    }
    
    public void testBufferedOutputStream()
    {
        assertNull(StreamUtils.buffered((OutputStream)null));
        final BufferedOutputStream bis = new BufferedOutputStream(System.out);
        assertSame(StreamUtils.buffered(bis), bis);
        final OutputStream nbis = new FilterOutputStream(System.out){};
        assertNotSame(StreamUtils.buffered(nbis), nbis);
    }
    public void testBufferedReader()
    {
        assertNull(StreamUtils.buffered((Reader)null));
        final BufferedReader bis = new BufferedReader(new InputStreamReader(System.in));
        assertSame(StreamUtils.buffered(bis), bis);
        final Reader nbis = new FilterReader(new InputStreamReader(System.in)){};
        assertNotSame(StreamUtils.buffered(nbis), nbis);
    }
    
    public void testBufferedWriter()
    {
        assertNull(StreamUtils.buffered((Writer)null));
        final BufferedWriter bis = new BufferedWriter(new OutputStreamWriter(System.out));
        assertSame(StreamUtils.buffered(bis), bis);
        final Writer nbis = new FilterWriter(new OutputStreamWriter(System.out)){};
        assertNotSame(StreamUtils.buffered(nbis), nbis);
    }

    public void testClose() {
        StreamUtils.close(null);
        final boolean[] closed = { false };
        StreamUtils.close(new Closeable(){

            @Override
            public void close() throws IOException {
                closed[0]=true;
            }
        });
        assertTrue(closed[0]);
    }

    public void testTransfer() throws IOException {
        final String input = "Hola Mundo";
        final ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes("UTF-8"));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamUtils.transfer(in, out);
        final String output = new String(out.toByteArray(), "UTF-8");
        assertEquals(input, output);
    }

    public void testAsyncTransfer() throws IOException, InterruptedException {
        final String input = "Adios mundo cruel";
        final ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes("UTF-8"));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final Thread thread = StreamUtils.asyncTransfer(in, out);
        thread.join(30000L);
        final String output = new String(out.toByteArray(), "UTF-8");
        assertEquals(input, output);
    }

}