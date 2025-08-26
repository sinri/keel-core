package io.github.sinri.keel.facade.cli;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CommandLineParserImpl implements CommandLineParser {
    private static final Pattern WHITE_CHARS = Pattern.compile("\\s");
    private static final Pattern SHORT_NAMED_ARGUMENT_PATTERN = Pattern.compile("^-([A-Za-z0-9_])(=(.*))?$");
    private static final Pattern LONG_NAMED_ARGUMENT_PATTERN = Pattern.compile("^--([A-Za-z0-9_.][A-Za-z0-9_.-]*)(=(.*))?$");

    private final Map<String, Option> optionMap = new HashMap<>();
    private final Map<Character, Option> shortMap = new HashMap<>();
    private final Map<String, Option> longMap = new HashMap<>();
    private boolean strict = false;

    public void addOption(@Nonnull Option option) throws CommandLineParserBuildError {
        if (optionMap.containsKey(option.id())) {
            throw new CommandLineParserBuildError("Duplicate named argument definition id: " + option.id());
        }
        optionMap.put(option.id(), option);

        Set<String> aliasSet = option.getAliasSet();
        for (var alias : aliasSet) {
            if (alias == null) {
                throw new CommandLineParserBuildError("Alias cannot be null");
            }
            if (alias.isEmpty()) {
                throw new CommandLineParserBuildError("Alias cannot be empty");
            }
            if (WHITE_CHARS.matcher(alias).find()) {
                throw new CommandLineParserBuildError("Alias cannot contains white chars");
            }
            if (alias.startsWith("-")) {
                throw new CommandLineParserBuildError("Alias cannot starts with '-'");
            }
            if (alias.length() == 1) {
                char shortName = alias.charAt(0);
                if (shortMap.containsKey(shortName)) {
                    throw new CommandLineParserBuildError("Duplicate short alias: " + shortName);
                }
                shortMap.put(shortName, option);
            } else {
                if (longMap.containsKey(alias)) {
                    throw new CommandLineParserBuildError("Duplicate alias: " + alias);
                }
                longMap.put(alias, option);
            }
        }
    }

    @Nonnull
    @Override
    public CommandLineParsedResult parse(String[] args) throws CommandLineParserParseError {
        CommandLineParsedResult parsedResult = CommandLineParsedResult.create();

        if (args == null) {
            return parsedResult;
        }
        /*
         MODE 0: may meet named arguments or parameters
         MODE 1: into named arguments, waiting for value
         MODE 2: end named arguments
         MODE 3: AFTER -- or any parameter, the rest are parameters
         */
        int mode = 0;
        String longNameCache = null;
        Character shortNameCache = null;
        for (int i = 0; i < args.length; i++) {
            String currentArg = args[i];
            if (currentArg == null) {
                continue;
            }
            switch (mode) {
                case 1:
                    if (longNameCache != null) {
                        recordOption(parsedResult, longNameCache, currentArg);
                        longNameCache = null;
                    } else if (shortNameCache != null) {
                        recordOption(parsedResult, shortNameCache, currentArg);
                        shortNameCache = null;
                    } else {
                        throw new CommandLineParserParseError("Invalid named argument: " + currentArg);
                    }
                    mode = 2;
                    break;
                case 0:
                case 2:
                    if (currentArg.equals("--")) {
                        mode = 3;
                    } else if (currentArg.startsWith("--")) {
                        Matcher matcher = LONG_NAMED_ARGUMENT_PATTERN.matcher(currentArg);
                        if (matcher.matches()) {
                            String longName = matcher.group(1);
                            if (matcher.groupCount() > 2) {
                                String value = matcher.group(3);
                                recordOption(parsedResult, longName, value);
                                mode = 2;
                            } else {
                                longNameCache = longName;
                                //shortNameCache = null;
                                mode = 1;
                            }
                        } else {
                            throw new CommandLineParserParseError("Invalid long named argument: " + currentArg);
                        }
                    } else if (currentArg.startsWith("-")) {
                        Matcher matcher = SHORT_NAMED_ARGUMENT_PATTERN.matcher(currentArg);
                        if (matcher.matches()) {
                            char shortName = matcher.group(1).charAt(0);
                            if (matcher.groupCount() > 2) {
                                String value = matcher.group(3);
                                recordOption(parsedResult, shortName, value);
                                mode = 2;
                            } else {
                                //longNameCache = null;
                                shortNameCache = shortName;
                                mode = 1;
                            }
                        } else {
                            throw new CommandLineParserParseError("Invalid short named argument: " + currentArg);
                        }
                    } else {
                        recordParameter(parsedResult, currentArg);
                        mode = 3;
                    }
                    break;

                case 3:
                    recordParameter(parsedResult, currentArg);
                    break;
            }
        }

        return parsedResult;
    }

    private void recordOption(CommandLineParsedResult parsedResult, String longName, String value) throws CommandLineParserParseError {
        Option option = this.longMap.get(longName);
        if (option == null) {
            if (isStrictMode()) {
                throw new CommandLineParserParseError("In strict mode, undefined long name is not allowed");
            } else {
                option = new Option().alias(longName);
            }
        }
        option.setValue(value);
        parsedResult.recordOption(option);
    }

    private void recordOption(CommandLineParsedResult parsedResult, char shortName, String value) throws CommandLineParserParseError {
        Option option = this.shortMap.get(shortName);
        if (option == null) {
            if (isStrictMode()) {
                throw new CommandLineParserParseError("In strict mode, undefined short name is not allowed");
            } else {
                option = new Option().alias(String.valueOf(shortName));
            }
        }
        option.setValue(value);
        parsedResult.recordOption(option);
    }

    private void recordParameter(CommandLineParsedResult parsedResult, String parameter) {
        parsedResult.recordParameter(parameter);
    }

    @Override
    public boolean isStrictMode() {
        return strict;
    }

    @Override
    public void setStrictMode(boolean strictOrNot) {
        this.strict = strictOrNot;
    }
}
