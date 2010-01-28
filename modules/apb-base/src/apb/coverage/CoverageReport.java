

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


package apb.coverage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

import static apb.coverage.CoverageReport.Column.*;
import static java.util.Arrays.asList;

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

    @NotNull private Depth                          depth;
    @NotNull private final EnumMap<Column, Integer> thresholds;
    @NotNull private final EnumSet<Column>          descending;
    @Nullable private File                          output;

    @NotNull private List<Column>       columns;
    @NotNull private final List<Column> order;

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

    /**
     * Adds an order criteria to the CoverageReport by a given Column
     *
     * @param column The Column to use for ordering
     *        Valid values are Column.NAME, Column.CLASS, Column.METHOD,
     *        Column.BLOCK and Column.LINE.  See {@link Column}.
     *
     * @return a CoverageReport instance ordered by a given Column
     */
    @NotNull public CoverageReport orderBy(@NotNull Column column)
    {
        CoverageReport result = cloneIfNecessary();
        result.order.add(column);
        return result;
    }

    /**
     * Sets the descending order for a CoverageReport
     * This method must be called after the 'sortBy' method
     *
     * @throws IllegalStateException  if there are no columns
     *         set by the 'sortBy' method in advance
     *
     * @return an ordered CoverageReport instance
     */
    @NotNull public CoverageReport descending()
    {
        if (order.isEmpty()) {
            throw new IllegalStateException("Descending must be called after 'sortBy'");
        }

        CoverageReport result = cloneIfNecessary();
        result.descending.add(order.get(order.size() - 1));
        return result;
    }

    /**
     * Sets the depth to the CoverageReport by a given Column
     *
     * @param depth The Depth of the Coverage Report
     *        Valid values are Depth.ALL, Depth.CLASS,
     *        and Depth.METHOD.  See {@link Depth}.
     *
     * @return a CoverageReport instance ordered by a given Column
     */
    @NotNull public CoverageReport depth(@NotNull Depth depth)
    {
        CoverageReport result = cloneIfNecessary();
        result.depth = depth;
        return result;
    }

    /**
     * Adds an order criteria to the CoverageReport by a given Columns
     *
     * @param cs Column objects separated by comas to use for ordering
     *        Valid values are Column.NAME, Column.CLASS, Column.METHOD,
     *        Column.BLOCK and Column.LINE.  See {@link Column}.
     *
     * @return a CoverageReport instance ordered by a given Column
     */
    @NotNull public CoverageReport columns(@NotNull Column... cs)
    {
        CoverageReport result = cloneIfNecessary();
        result.columns = asList(cs);
        return result;
    }

    /**
     * Adds an output File to the CoverageReport
     *
     * @param outputFile A path to a file
     *
     * @return an updated CoverageReport instance with the output file
     */
    @NotNull public CoverageReport outputTo(@NotNull String outputFile)
    {
        CoverageReport result = cloneIfNecessary();
        result.output = new File(outputFile);
        return result;
    }

    /**
     * Adds an threshold for a given CoverageReport Column
     *
     * @param column The Column to use for ordering
     *        Valid values are Column.NAME, Column.CLASS, Column.METHOD,
     *        Column.BLOCK and Column.LINE.  See {@link Column}.
     *
     * @param percentage The threshold. Valid values are from 0 to 100
     *
     * @return an updated CoverageReport instance with the Columns threshold
     */
    @NotNull public CoverageReport threshold(@NotNull Column column, int percentage)
    {
        CoverageReport result = cloneIfNecessary();
        result.thresholds.put(column, percentage);
        return result;
    }

    /**
     * Sets a fixed threshold for all the Columns of the CoverageReport
     *
     * @param percentage The threshold. Valid values are from 0 to 100
     *
     * @return an updated CoverageReport instance with the Columns threshold
     */
    @NotNull public CoverageReport threshold(int percentage)
    {
        CoverageReport result = cloneIfNecessary();

        for (Column c : Column.values()) {
            result.thresholds.put(c, percentage);
        }

        return result;
    }

    /**
     * Returns an output File instance
     * if there is already an output file it is returned,
     * else a new output file is created with the given outputDir
     *
     * @param outputDir A path to a file
     *
     * @return an output File  
     */
    public File getOutputFile(File outputDir)
    {
        File out = output == null ? new File(outputDir, "coverage") : output;

        if (out.getName().indexOf(".") == -1) {
            out = new File(out.getPath() + "." + type);
        }

        return out;
    }

    /**
     * Returns the list of columns of the current CoverageReport
     *
     * @return a List of Column
     *         See {@link Column}. 
     */
    @NotNull public List<Column> getColumns()
    {
        return columns;
    }

    /**
     * Returns the current CoverageReport type
     *
     * @return a String containing the CoverageReport type
     */
    @NotNull public String getType()
    {
        return type;
    }

    /**
     * Get the argument definitions for the current CoverageReport
     *
     * @return a List<String> containing the values of the following arguments:
     *         depth, columns, sort, out.file and metrics (for type = HTML or TEXT)    
     */
    @NotNull public List<String> defines(File outputDir)
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
