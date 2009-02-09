
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

package apb.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import apb.Messages;

import org.jetbrains.annotations.NonNls;

/**
 * This class parses command line arguments using two alternatives:
 * one-letter options which must be preceded by '-' and
 * long options which must be preceded by '--'.  Multiple boolean
 * one-letter options can be combined (preceding them with a single
 * '-').
 */
public class OptionParser
{
    //~ Instance fields ......................................................................................

    protected final List<String> arguments;

    private final String  appName;
    private final boolean exitOnStopParsing = true;

    private List<Option> options = null;
    private final String versionNumber;

    //~ Constructors .........................................................................................

    protected OptionParser(final String[] args, String name, String version)
    {
        arguments = Arrays.asList(args);
        appName = name;
        versionNumber = version;
    }

    //~ Methods ..............................................................................................

    public List<String> getArguments()
    {
        return arguments;
    }

    public String getArgShortDescription()
    {
        return "";
    }

    public String[] getArgFullDescription()
    {
        return null;
    }

    public final Option<String> addOption(final char letter, final String name, String description)
    {
        return addOption(String.class, letter, name, description, null);
    }

    public final Option<String> addOption(final String name, String description)
    {
        return addOption(String.class, '\0', name, description, null);
    }

    public final Option<String> addOption(final char letter, final String name, String description,
                                          String defaultValue)
    {
        return addOption(String.class, letter, name, description, defaultValue);
    }

    protected final Option<Boolean> addOption(final char letter, final String name, String description,
                                              final boolean defaultValue)
    {
        return addOption(Boolean.class, letter, name, description, defaultValue);
    }

    protected final Option<Integer> addOption(final char letter, final String name, String description,
                                              final int defaultValue)
    {
        return addOption(Integer.class, letter, name, description, defaultValue);
    }

    protected List<String> parse()
    {
        int i;

        for (i = 0; i < arguments.size(); ++i) {
            String arg = arguments.get(i);

            if (arg.charAt(0) != '-' || "--".equals(arg)) {
                break;
            }

            boolean longOpt = arg.charAt(1) == '-';

            final boolean negation = arg.startsWith(NEG_PREFIX);

            arg = arg.substring(negation ? NEG_PREFIX.length() : longOpt ? 2 : 1);

            if (arg.isEmpty()) {
                printError(Messages.INVOPT(arguments.get(i)));
                stopParsing();
            }

            if (longOpt) {
                Option opt = findOption(arg);
                i = execute(opt, negation, i);
            }
            else {
                for (int j = 0; j < arg.length(); j++) {
                    char   optChar = arg.charAt(j);
                    Option opt = findOption(optChar);

                    if (j > 0 && !opt.isBoolean()) {
                        printError(Messages.CANTCOMBINE);
                    }

                    i = execute(opt, negation, i);
                }
            }
        }

        return new ArrayList<String>(arguments.subList(i, arguments.size()));
    }

    protected void printHelp()
    {
        System.err.println(getAppName() + " " + Messages.OPT_IN_BRACKETS + " " + getArgShortDescription());
        System.err.println();

        System.err.println(Messages.WHERE);

        final String[] full = getArgFullDescription();

        if (full != null) {
            for (String line : full) {
                System.err.print(SPACES);
                System.err.println(line);
            }

            System.err.println();
        }

        System.err.println(Messages.OPTIONS);

        for (Option opt : options) {
            String ops = opt.getName();

            if (opt.isString()) {
                ops += " " + STRING_VALUE;
            }
            else if (opt.isInteger()) {
                ops += " " + VALUE;
            }

            System.err.print(SPACES);

            final char letter = opt.getLetter();

            if (letter == 0) {
                System.err.print("   ");
            }
            else {
                System.err.print("-" + letter + ",");
            }

            System.err.printf(" --%-18s: %s", ops, opt.getDescription());
            System.err.println();
        }

        System.exit(0);
    }

    protected List<String> findOptions(String opt)
    {
        List<String> result = new ArrayList<String>();

        for (Option option : options) {
            if (option.getName().startsWith(opt)) {
                result.add("--" + option.getName());
            }
        }

        return result;
    }

    protected List<String> findShortOptions(char chr)
    {
        List<String> result = new ArrayList<String>();

        for (Option option : options) {
            if (option.getLetter() == chr) {
                result.add("-" + option.getLetter());
            }
        }

        return result;
    }

    Option findOption(char option)
    {
        for (Option o : options) {
            if (o.getLetter() == option) {
                return o;
            }
        }

        printError(Messages.INVOPT("-" + option));
        stopParsing();
        throw new RuntimeException();
    }

    Option findOption(String option)
    {
        for (Option o : options) {
            if (o.getName().equals(option)) {
                return o;
            }
        }

        printError(Messages.INVOPT("'--" + option + "'"));
        stopParsing();
        throw new RuntimeException();
    }

    String getAppName()
    {
        return appName;
    }

    void printVersion()
    {
        System.err.println(getAppName() + " version: " + versionNumber);
        System.exit(0);
    }

    private int execute(Option opt, boolean negation, int i)
    {
        if (opt.isBoolean()) {
            opt.execute(negation);
        }
        else {
            if (negation) {
                printError(Messages.NONBOOLNEG(opt.getLetter(), opt.getName()));
                stopParsing();
            }

            if (i == arguments.size() - 1) {
                printError(Messages.EXPECTEDARG(opt.getLetter(), opt.getName()));
                stopParsing();
            }

            opt.execute(negation, arguments.get(++i));
        }

        return i;
    }

    private void printError(String msg)
    {
        System.err.println(msg);
    }

    private <T> Option<T> addOption(final Class<T> type, final char letter, final String name,
                                    String description, final T defaultValue)
    {
        init();
        final Option<T> opt = new Option<T>(this, type, letter, name, description, defaultValue);
        options.add(opt);
        return opt;
    }

    private void init()
    {
        if (options == null) {
            options = new ArrayList<Option>();
            final Option<Boolean> helpOption =
                new Option<Boolean>(this, Boolean.class, 'h', Option.HELP, Messages.HELP, null) {
                    public void execute(final boolean negated)
                    {
                        if (!negated) {
                            printHelp();
                        }
                    }
                };

            options.add(helpOption);

            final Option<Boolean> versionOption =
                new Option<Boolean>(this, Boolean.class, '\0', Option.VERSION, Messages.VERSION, null) {
                    public void execute(final boolean negated)
                    {
                        if (!negated) {
                            printVersion();
                        }
                    }
                };
            options.add(versionOption);
        }
    }

    private void stopParsing()
    {
        if (exitOnStopParsing) {
            System.exit(1);
        }

        throw new RuntimeException();
    }

    //~ Static fields/initializers ...........................................................................

    @NonNls private static final String SPACES = "    ";

    @NonNls private static final String STRING_VALUE = "<string>";
    @NonNls private static final String VALUE = "<value>";

    @NonNls private static final String NEG_PREFIX = "--no-";

    //~ Inner Classes ........................................................................................

    /**
    * User: emilio
    * Date: Aug 29, 2005
    * Time: 12:37:38 PM
    */
    public static class Option<T>
    {
        private final String       description;
        private final char         letter;
        private final String       name;
        private final OptionParser optionParser;
        private final Class<T>     type;
        private final ArrayList<T> validValues;

        private T value;

        Option(OptionParser optionParser, final Class<T> type, final char letter, final String name,
               String description, final T defaultValue)
        {
            this.optionParser = optionParser;
            this.letter = letter;
            this.name = name.startsWith("--") ? name.substring(2) : name;
            this.type = type;
            this.description = description == null ? "" : description;
            value = defaultValue;
            validValues = new ArrayList<T>();
        }

        public char getLetter()
        {
            return letter;
        }

        public String getName()
        {
            return name;
        }

        public final T getValue()
        {
            return value;
        }

        public String getDescription()
        {
            String result = description;

            if (!validValues.isEmpty()) {
                StringBuilder str = new StringBuilder();

                for (T validValue : validValues) {
                    str.append(str.length() == 0 ? '[' : '|');
                    str.append(String.valueOf(validValue));
                }

                str.append(']');
                result += " " + str.toString();
            }

            return result;
        }

        public void addValidValue(T val)
        {
            validValues.add(val);
        }

        public String toString()
        {
            return name;
        }

        @SuppressWarnings({ "unchecked" })
        void execute(final boolean negated)
        {
            if (!negated && name.equals(HELP)) {
                optionParser.printHelp();
            }

            if (isBoolean()) {
                value = (T) Boolean.valueOf(!negated);
            }
        }

        @SuppressWarnings({ "unchecked" })
        void execute(final boolean negated, final String str)
        {
            if (isBoolean()) {
                Boolean b = Boolean.valueOf(str);

                if (negated) {
                    b = !b;
                }

                value = (T) b;
            }
            else {
                if (isInteger()) {
                    value = (T) Integer.valueOf(str);
                }
                else {
                    value = (T) str;
                }

                if (!validValues.isEmpty() && !validValues.contains(value)) {
                    optionParser.printError(Messages.INVARG(str, name));
                    optionParser.stopParsing();
                }
            }
        }

        boolean isString()
        {
            return type == String.class;
        }

        boolean isBoolean()
        {
            return type == Boolean.class;
        }

        boolean isInteger()
        {
            return type == Integer.class;
        }

        @NonNls static final String HELP = "help";
        @NonNls static final String VERSION = "version";
    }
}
