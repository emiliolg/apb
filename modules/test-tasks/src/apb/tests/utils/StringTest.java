

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

import java.io.IOException;

import apb.utils.ColorUtils;
import apb.utils.StringUtils;

import junit.framework.TestCase;
//
// User: emilio
// Date: Sep 3, 2009
// Time: 5:38:33 PM

//
public class StringTest
    extends TestCase
{
    //~ Methods ..............................................................................................

    public void testBase64()
        throws IOException
    {
        String result = StringUtils.encodeBase64(STRING1.getBytes("UTF8"));
        assertEquals("QSBzaW1wbGUgc3RyaW5n", result);

        assertBytesEquals(STRING1.getBytes(), StringUtils.decodeBase64(result));

        result = StringUtils.encodeBase64(STRING2.getBytes("UTF8"));
        assertEquals("4Yi0AUYxRgpBIHVuaWNvZGUgc3RyaW5nLg==", result);
        assertBytesEquals(STRING2.getBytes("UTF8"), StringUtils.decodeBase64(result));
    }

    public void testEncode()
        throws IOException
    {
        String result = StringUtils.encode(STRING2);
        assertEquals("\\u1234\\u0001F1F\\nA unicode string.", result);

        result = StringUtils.encode(STRING3);
        assertEquals("\\ Some control chars\\: \\t\\r\\f. A literal backslash\\: \\\\.", result);
    }

    public void testEmpty()
        throws IOException
    {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(STRING1));

        assertFalse(StringUtils.isNotEmpty(""));
        assertFalse(StringUtils.isNotEmpty(null));
        assertTrue(StringUtils.isNotEmpty(STRING1));
    }

    public void testMatch()
        throws IOException
    {
        assertTrue(StringUtils.match("**/*.java", "a/x/y/A.java", true));
        assertFalse(StringUtils.match("**/*.java", "a/x/y/A.JAVA", true));
        assertTrue(StringUtils.match("**/*.java", "a/x/y/A.JAVA", false));
        assertTrue(StringUtils.match("a/**/*.java", "a/x/y/A.JAVA", false));
        assertTrue(StringUtils.match("a/x/y/*.java", "a/x/y/A.JAVA", false));

        assertTrue(StringUtils.match("/x/*.java", "/x/A.java", true));
        assertFalse(StringUtils.match("/y/*.java", "y/A.java", true));

        assertTrue(StringUtils.match("*", "/x/Pepe.java", true));

        assertTrue(StringUtils.matchPatternStart("**/*.java", "a/x/y/A.java-xx", true));
        assertFalse(StringUtils.matchPatternStart("a/*.java", "a/x/y/A.JAVA/yyy", true));
        assertTrue(StringUtils.matchPatternStart("a/x/y/*.java/xxx", "a/x/y/A.JAVA", false));
        assertFalse(StringUtils.matchPatternStart("/y/*.java", "y/A.java", true));
    }

    public void testJavaId()
        throws IOException
    {
        assertTrue(StringUtils.isJavaId("A23"));
        assertFalse(StringUtils.isJavaId("23A"));
        assertFalse(StringUtils.isJavaId("A23#"));
    }

    public void testNChars()
        throws IOException
    {
        assertEquals("", StringUtils.nChars(0, '*'));
        assertEquals("**", StringUtils.nChars(2, '*'));
        assertEquals("aaaaa", StringUtils.nChars(5, 'a'));
    }

    public void testGetStackTrace()
    {
        Throwable t = new Exception("test");
        assertTrue(StringUtils.getStackTrace(t).startsWith("java.lang.Exception: test"));
    }

    public void testAppendIndenting()
    {
        String result = StringUtils.appendIndenting("Header: ", "Line1\nLine2\nLine3");
        assertEquals("Header: Line1\n" + "        Line2\n" + "        Line3", result);
    }

    public void testColorUtils()
    {
        String s = ColorUtils.colorize(ColorUtils.RED, "hello");
        assertEquals(ColorUtils.RED + "hello" + ColorUtils.RESET, s);
        s = ColorUtils.trimColors(s);
        assertEquals("hello", s);
        s = ColorUtils.trimColors(s);
        assertEquals("hello", s);
    }

    private void assertBytesEquals(byte[] bytes1, byte[] bytes2)
    {
        assertEquals(bytes1.length, bytes2.length);

        for (int i = 0; i < bytes1.length; i++) {
            assertEquals(bytes1[i], bytes2[i]);
        }
    }

    //~ Static fields/initializers ...........................................................................

    private static final String STRING1 = "A simple string";
    private static final String STRING2 = "\u1234\1F1F\nA unicode string.";
    private static final String STRING3 = " Some control chars: \t\r\f. A literal backslash: \\.";
}
