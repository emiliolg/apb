
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

package apb.coverage;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Arrays.asList;

import static apb.coverage.CoverageReport.Column.*;

/**
 * This class provides a way to specify a Coverage Report
 * The methods are intended to be used following a 'Fluent' interface syntax.
 * <p>
 * For example:
 * <p>
 * <blockquote><pre>
 *
 * rep = HTML.sortBy(CLASS).descending()
 * rep2 = TEXT.columns(NAME, METHOD).depth(PACKAGE)
 *
 * </pre></blockquote>
 *
 */
public class CoverageReport
{
    //~ Instance fields ......................................................................................

    @NotNull private List<Column> columns;

    @NotNull private Depth                    depth;
    @NotNull private EnumSet<Column>          descending;
    @NotNull private List<Column>             order;
    @Nullable private File                    output;
    @NotNull private EnumMap<Column, Integer> thresholds;

    @NotNull private final String type;

    //~ Constructors .........................................................................................

    private CoverageReport(@NotNull String type, @NotNull Depth depth, @NotNull List<Column> columns)
    {
        this.type = type;
        this.depth = depth;
        this.columns = columns;
        order = new ArrayList<Column>();
        descending = EnumSet.noneOf(Column.class);
        thresholds = new EnumMap<Column, Integer>(Column.class);
        setThreshold(DEFAULT_MIN_COVERAGE);
    }

    //~ Methods ..............................................................................................

    @NotNull public CoverageReport orderBy(@NotNull Column column)
    {
        CoverageReport result = cloneIfNecessary();
        result.order.add(column);
        return result;
    }

    @NotNull public CoverageReport descending()
    {
        if (order.isEmpty()) {
            throw new IllegalStateException("Descending must be called after 'sortBy'");
        }

        CoverageReport result = cloneIfNecessary();
        result.descending.add(order.get(order.size() - 1));
        return result;
    }

    @NotNull public CoverageReport depth(@NotNull Depth d)
    {
        CoverageReport result = cloneIfNecessary();
        result.depth = d;
        return result;
    }

    @NotNull public CoverageReport columns(@NotNull Column... cs)
    {
        CoverageReport result = cloneIfNecessary();
        result.columns = asList(cs);
        return result;
    }

    @NotNull public CoverageReport outputTo(@NotNull String outputFile)
    {
        CoverageReport result = cloneIfNecessary();
        result.output = new File(outputFile);
        return result;
    }

    @NotNull public CoverageReport threshold(@NotNull Column c, int percentage)
    {
        CoverageReport result = cloneIfNecessary();
        result.thresholds.put(c, percentage);
        return result;
    }

    @NotNull public CoverageReport threshold(int percentage)
    {
        CoverageReport result = cloneIfNecessary();

        for (Column c : Column.values()) {
            result.thresholds.put(c, percentage);
        }

        return result;
    }

    public File getOutputFile(File outputDir)
    {
        File out = output == null ? new File(outputDir, "coverage") : output;

        if (out.getName().indexOf(".") == -1) {
            out = new File(out.getPath() + "." + type);
        }

        return out;
    }

    @NotNull List<Column> getColumns()
    {
        return columns;
    }

    @NotNull String getType()
    {
        return type;
    }

    @NotNull List<String> defines(File outputDir)
    {
        String       prefix = "-Dreport." + type + ".";
        List<String> result = new ArrayList<String>();
        result.add(prefix + "depth=" + depth.name().toLowerCase());
        result.add(prefix + "columns=" + columnsAsString());
        result.add(prefix + "sort=" + orderAsString());
        result.add(prefix + "out.file=" + getOutputFile(outputDir).getAbsolutePath());

        if (!type.equals(XML.type)) {
            result.add(prefix + "metrics=" + thresholdsAsString());
        }

        return result;
    }

    private void setThreshold(final int percentage)
    {
        for (Column c : Column.values()) {
            if (c != NAME) {
                thresholds.put(c, percentage);
            }
        }
    }

    private String thresholdsAsString()
    {
        StringBuilder str = new StringBuilder();

        for (Map.Entry<Column, Integer> e : thresholds.entrySet()) {
            if (str.length() > 0) {
                str.append(",");
            }

            str.append(e.getKey().name().toLowerCase());
            str.append(":");
            str.append(e.getValue());
        }

        return str.toString();
    }

    private String columnsAsString()
    {
        StringBuilder str = new StringBuilder();

        for (Column c : columns) {
            if (str.length() > 0) {
                str.append(",");
            }

            str.append(c.name().toLowerCase());
        }

        return str.toString();
    }

    private String orderAsString()
    {
        StringBuilder str = new StringBuilder();

        for (Column c : order) {
            if (str.length() > 0) {
                str.append(",");
            }

            str.append(descending.contains(c) ? '-' : '+');
            str.append(c.name().toLowerCase());
        }

        return str.toString();
    }

    @NotNull private CoverageReport cloneIfNecessary()
    {
        return this == HTML || this == TEXT || this == XML ? new CoverageReport(type, depth, columns) : this;
    }

    //~ Static fields/initializers ...........................................................................

    private static final int DEFAULT_MIN_COVERAGE = 50;

    public static final CoverageReport HTML =
        new CoverageReport("html", Depth.METHOD, asList(NAME, METHOD, BLOCK, LINE));
    public static final CoverageReport TEXT =
        new CoverageReport("txt", Depth.ALL, asList(CLASS, METHOD, BLOCK, LINE, NAME));
    public static final CoverageReport XML =
        new CoverageReport("xml", Depth.METHOD, asList(NAME, METHOD, BLOCK, LINE));

    //~ Enums ................................................................................................

    public enum Depth
    {
        ALL,
        PACKAGE,
        CLASS,
        METHOD
    }

    public enum Column
    {
        NAME,
        CLASS,
        METHOD,
        BLOCK,
        LINE
    }
}
