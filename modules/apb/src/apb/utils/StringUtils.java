

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

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import apb.sunapi.Base64;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;
import static java.lang.Integer.toHexString;
//
// User: emilio
// Date: Oct 1, 2008
// Time: 4:59:44 PM

//
public class StringUtils
{
    //~ Methods ..............................................................................................

    public static boolean isEmpty(String s)
    {
        return s == null || s.isEmpty();
    }

    public static boolean isNotEmpty(String s)
    {
        return s != null && !s.isEmpty();
    }

    /**
     * Tests whether or not a given path matches a given pattern.
     *
     * @param pattern The pattern to match against.
     * @param path     The path to match, as a String.
     * @param caseSensitive Whether or not matching should be performed
     *                        case sensitively.
     *
     * @return <code>true</code> if the pattern matches against the string,
     *         <code>false</code> otherwise.
     */
    public static boolean matchPath(@NotNull String pattern, @NotNull String path, boolean caseSensitive)
    {
        // Both should start with a File.separator or nnone of them.
        return path.startsWith(File.separator) == pattern.startsWith(File.separator) &&
               matchPath(tokenizePath(pattern), tokenizePath(path), caseSensitive);
    }

    public static boolean matchPatternStart(String pattern, String path, boolean isCaseSensitive)
    {
        return path.startsWith(File.separator) == pattern.startsWith(File.separator) &&
               matchPathStart(tokenizePath(pattern), tokenizePath(path), isCaseSensitive);
    }

    public static boolean match(String pattern, String str, boolean caseSensitive)
    {
        if ("*".equals(pattern)) {
            return true;
        }

        final int strlen = str.length();

        int firstStar = pattern.indexOf('*');

        if (firstStar == -1) {
            if (strlen != pattern.length()) {
                return false;
            }

            firstStar = strlen;
        }
        else if (firstStar > strlen) {
            return false;
        }

        for (int i = 0; i < firstStar; i++) {
            if (!matchChar(pattern.charAt(i), str.charAt(i), caseSensitive)) {
                return false;
            }
        }

        int patternStart = firstStar;
        int patternEnd = pattern.length() - 1;
        int strStart = patternStart;
        int strEnd = strlen - 1;

        if (firstStar == strlen) {
            return allStars(pattern, firstStar, patternEnd);
        }

        char[] patArr = pattern.toCharArray();
        char[] strArr = str.toCharArray();

        char ch;

        // Process characters after last star
        while ((ch = patArr[patternEnd]) != '*' && strStart <= strEnd) {
            if (!matchChar(ch, strArr[strEnd], caseSensitive)) {
                return false;
            }

            patternEnd--;
            strEnd--;
        }

        if (strStart > strEnd) {
            return allStars(pattern, patternStart, patternEnd);
        }

        // process pattern between stars. padIdxStart and patIdxEnd point
        // always to a '*'.
        while (patternStart != patternEnd && strStart <= strEnd) {
            int patIndex = -1;

            for (int i = patternStart + 1; i <= patternEnd; i++) {
                if (patArr[i] == '*') {
                    patIndex = i;
                    break;
                }
            }

            if (patIndex == patternStart + 1) {
                // Two stars next to each other, skip the first one.
                patternStart++;
            }
            else {
                int patLength = (patIndex - patternStart - 1);
                int foundIdx =
                    foundPattern(patArr, patternStart, patLength, strArr, strStart, strEnd, caseSensitive);

                if (foundIdx == -1) {
                    return false;
                }

                patternStart = patIndex;
                strStart = foundIdx + patLength;
            }
        }

        return allStars(pattern, patternStart, patternEnd);
    }

    public static String encode(String str)
    {
        final int           len = str.length();
        final StringBuilder outBuffer = new StringBuilder(len * 2);

        for (int x = 0; x < len; x++) {
            final char c = str.charAt(x);

            switch (c) {
            case ' ':

                if (x == 0) {
                    outBuffer.append('\\');
                }

                outBuffer.append(' ');
                break;
            case '\\':
                outBuffer.append("\\\\");
                break;
            case '\t':
                outBuffer.append("\\t");
                break;
            case '\n':
                outBuffer.append("\\n");
                break;
            case '\r':
                outBuffer.append("\\r");
                break;
            case '\f':
                outBuffer.append("\\f");
                break;
            default:

                if ((c < 0x0020) || (c > 0x007e)) {
                    outBuffer.append('\\');
                    outBuffer.append('u');
                    outBuffer.append(toHexString((c >> 12) & 0xF));
                    outBuffer.append(toHexString((c >> 8) & 0xF));
                    outBuffer.append(toHexString((c >> 4) & 0xF));
                    outBuffer.append(toHexString(c & 0xF));
                }
                else {
                    if (specialSaveChars.indexOf(c) != -1) {
                        outBuffer.append('\\');
                    }

                    outBuffer.append(c);
                }
            }
        }

        return outBuffer.toString();
    }

    public static String quote(String path)
    {
        String result = path;

        if (path.isEmpty() || path.indexOf(' ') == -1) {
            final StringBuilder builder = new StringBuilder();
            builder.append('\'');

            for (int i = 0; i < path.length(); i++) {
                char chr = path.charAt(i);

                if (chr == '\'') {
                    builder.append("\'");
                }
                else {
                    builder.append(chr);
                }
            }

            builder.append('\'');
            result = builder.toString();
        }

        return result;
    }

    public static boolean isJavaId(String s)
    {
        if (s.isEmpty() || !Character.isJavaIdentifierStart(s.charAt(0))) {
            return false;
        }

        for (int i = 1; i < s.length(); i++) {
            if (!Character.isJavaIdentifierPart(s.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static String convert(Object param)
    {
        if (param instanceof File[]) {
            return Arrays.toString((File[]) param);
        }
        else if (param instanceof Properties) {
            //            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //
            //            try {
            //                ((Properties) param).store(baos, "");
            //                return new String(baos.toByteArray(), "8859_1");
            //            }
            //            catch (Exception e) {
            throw new RuntimeException("bug in property conversion");
        }

        //        }
        else {
            return param.toString();
        }
    }

    public static String nChars(int n, char chr)
    {
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < n; i++) {
            str.append(chr);
        }

        return str.toString();
    }

    public static String getStackTrace(Throwable t)
    {
        StringWriter sw = new StringWriter();
        PrintWriter  pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        pw.flush();
        pw.close();
        return sw.toString();
    }

    public static String normalizePath(String include)
    {
        // Normalize slashes
        String pattern = include.trim().replace('/', File.separatorChar);

        if (pattern.endsWith(File.separator)) {
            pattern += "**";
        }

        return pattern;
    }

    public static List<String> normalizePaths(Collection<String> list)
    {
        List<String> patterns = new ArrayList<String>();

        for (String include : list) {
            String pattern = normalizePath(include);
            patterns.add(pattern);
        }

        return patterns;
    }

    /**
     * Append the lines contained in the 'msg' String to the header String
     * indenting the lines to respect the original length of the header
     * @param header The String that will be used as the header to append the lines to
     * @param msg  The msg to be appended
     */
    public static String appendIndenting(@NotNull String header, @NotNull String msg)
    {
        String indent = nChars(header.length(), ' ');
        int    nl;

        StringBuilder result = new StringBuilder(header);

        while ((nl = msg.indexOf('\n')) >= 0) {
            result.append(msg.substring(0, ++nl));
            msg = msg.substring(nl);
            result.append(indent);
        }

        if (!msg.isEmpty()) {
            result.append(msg);
        }

        return result.toString();
    }

    public static List<String> tokenize(String s, String token)
    {
        List<String>    ret = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(s, token);

        while (st.hasMoreTokens()) {
            ret.add(st.nextToken());
        }

        return ret;
    }

    /**
     * Create a string with the elements separated by the indicated character
     * @param list
     * @param sep
     * @return A String with the elements of the list separated by the specified separator
     */
    @NotNull public static String makeString(@NotNull List<String> list, final char sep)
    {
        StringBuilder buffer = new StringBuilder();

        for (String testGroup : list) {
            if (buffer.length() > 0) {
                buffer.append(sep);
            }

            buffer.append(testGroup);
        }

        return buffer.toString();
    }

    /**
     * Encode the given String bytes in a Base64 String
     * @param str The string to encode
     * @return A Base64 String
     */
    public static String encodeBase64(@NotNull String str)
    {
        return Base64.encode(str.getBytes());
    }

    private static boolean matchPathStart(List<String> patterns, List<String> paths, boolean caseSensitive)
    {
        int patternStart = 0;
        int pathStart = 0;

        // up to first '**'
        while (patternStart < patterns.size() && pathStart < paths.size()) {
            String patDir = patterns.get(patternStart);

            if (MATCH_ANYTHING_PATTERN.equals(patDir)) {
                break;
            }

            if (!match(patDir, paths.get(pathStart), caseSensitive)) {
                return false;
            }

            patternStart++;
            pathStart++;
        }

        return pathStart >= paths.size() || patternStart < patterns.size();
    }

    private static int foundPattern(char[] pattern, int patternStart, int patLenght, char[] str, int strStart,
                                    int strEnd, boolean caseSensitive)
    {
        int strLength = (strEnd - strStart + 1);

        for (int i = 0; i <= strLength - patLenght; i++) {
            int j = 0;

            while (j < patLenght &&
                       matchChar(pattern[patternStart + j + 1], str[strStart + i + j], caseSensitive)) {
                j++;
            }

            if (j == patLenght) {
                return strStart + i;
            }
        }

        return -1;
    }

    private static boolean allStars(String pattern, int from, final int to)
    {
        for (int i = from; i <= to; i++) {
            if (pattern.charAt(i) != '*') {
                return false;
            }
        }

        return true;
    }

    private static boolean matchChar(char patternChar, char chr, boolean caseSensitive)
    {
        return patternChar == '?' ||
               (patternChar == chr ||
                !caseSensitive &&
                (toUpperCase(patternChar) == toUpperCase(chr) ||
                 toLowerCase(patternChar) == toLowerCase(chr)));
    }

    private static boolean matchPath(final List<String> patterns, final List<String> paths,
                                     boolean caseSensitive)
    {
        int patternStart = 0;
        int pathsStart = 0;
        int patternEnd = patterns.size() - 1;
        int pathsEnd = paths.size() - 1;

        // up to first '**'

        while (patternStart <= patternEnd && pathsStart <= pathsEnd) {
            String patDir = patterns.get(patternStart);

            if (MATCH_ANYTHING_PATTERN.equals(patDir)) {
                break;
            }

            if (!match(patDir, paths.get(pathsStart), caseSensitive)) {
                return false;
            }

            patternStart++;
            pathsStart++;
        }

        if (pathsStart > pathsEnd) {
            // Path is exhausted
            return matchAnything(patterns, patternStart, patternEnd);
        }
        else if (patternStart > patternEnd) {
            // String not exhausted, but pattern is. Failure.
            return false;
        }

        // up to last '**'
        while (patternStart <= patternEnd && pathsStart <= pathsEnd) {
            String patDir = patterns.get(patternEnd);

            if (MATCH_ANYTHING_PATTERN.equals(patDir)) {
                break;
            }

            if (!match(patDir, paths.get(pathsEnd), caseSensitive)) {
                return false;
            }

            patternEnd--;
            pathsEnd--;
        }

        if (pathsStart > pathsEnd) {
            // String is exhausted
            return matchAnything(patterns, patternStart, patternEnd);
        }

        while (patternStart != patternEnd && pathsStart <= pathsEnd) {
            int patIdxTmp = -1;

            for (int i = patternStart + 1; i <= patternEnd; i++) {
                if (MATCH_ANYTHING_PATTERN.equals(patterns.get(i))) {
                    patIdxTmp = i;
                    break;
                }
            }

            if (patIdxTmp == patternStart + 1) {
                // '**/**' situation, so skip one
                patternStart++;
                continue;
            }

            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = (patIdxTmp - patternStart - 1);
            int strLength = (pathsEnd - pathsStart + 1);
            int foundIdx = -1;

strLoop:
            for (int i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    String subPat = patterns.get(patternStart + j + 1);
                    String subStr = paths.get(pathsStart + i + j);

                    if (!match(subPat, subStr, caseSensitive)) {
                        continue strLoop;
                    }
                }

                foundIdx = pathsStart + i;
                break;
            }

            if (foundIdx == -1) {
                return false;
            }

            patternStart = patIdxTmp;
            pathsStart = foundIdx + patLength;
        }

        return matchAnything(patterns, patternStart, patternEnd);
    }

    private static boolean matchAnything(List<String> patterns, int from, int to)
    {
        for (int i = from; i <= to; i++) {
            if (!MATCH_ANYTHING_PATTERN.equals(patterns.get(i))) {
                return false;
            }
        }

        return true;
    }

    private static List<String> tokenizePath(String path)
    {
        return tokenize(path, File.separator);
    }

    //~ Static fields/initializers ...........................................................................

    @NonNls private static final String MATCH_ANYTHING_PATTERN = "**";
    private static final String         specialSaveChars = "=: \t\r\n\f#!";
}
