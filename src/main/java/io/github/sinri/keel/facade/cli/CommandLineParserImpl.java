package io.github.sinri.keel.facade.cli;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.regex.Pattern;

class CommandLineParserImpl implements CommandLineParser {
    private static final Pattern WHITE_CHARS = Pattern.compile("\\s");
    private static final Pattern SHORT_NAMED_ARGUMENT_PATTERN = Pattern.compile("^-([A-Za-z0-9_])(?:=(.*))?$");
    private static final Pattern LONG_NAMED_ARGUMENT_PATTERN = Pattern.compile("^--([A-Za-z0-9_.][A-Za-z0-9_.-]*)(?:=(.*))?$");

    private final Map<String, Option> optionMap = new HashMap<>();
    private final Map<String, String> nameToOptionIdMap = new HashMap<>();

    public void addOption(@Nonnull Option option) throws CommandLineParserBuildError {
        if (optionMap.containsKey(option.id())) {
            throw new CommandLineParserBuildError("Duplicate named argument definition id: " + option.id());
        }
        optionMap.put(option.id(), option);

        Set<String> aliasSet = option.getAliasSet();
        if (aliasSet.isEmpty()) {
            throw new CommandLineParserBuildError("Option must have at least one alias");
        }

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
            if (nameToOptionIdMap.containsKey(alias)) {
                throw new CommandLineParserBuildError("Alias cannot duplicate: " + alias);
            }
            nameToOptionIdMap.put(alias, option.id());
        }
    }

    @Nonnull
    @Override
    public CommandLineParsedResult parse(String[] args) throws CommandLineParserParseError {
        CommandLineParsedResult parsedResult = CommandLineParsedResult.create();

        if (args == null || args.length == 0) {
            return parsedResult;
        }

        Map<String, String> options = new TreeMap<>();
        List<String> parameters = new ArrayList<>();

        /*
         mode=0: before options and parameters
         mode=1: met option name, start option
         mode=2: met option value, or confirmed flag, end option
         mode=3: met -- or parameter
         */

        int mode = 0;
        Option currentOption = null;
        for (String arg : args) {
            if (arg == null) continue;
            if (mode == 0 || mode == 2) {
                if ("--".equals(arg)) {
                    mode = 3;
                } else {
                    String parsedOptionName = Option.parseOptionName(arg);
                    if (parsedOptionName == null) {
                        if (mode == 0) {
                            // is parameter
                            parameters.add(arg);
                            mode = 3;
                        } else {
                            throw new CommandLineParserParseError("Invalid option: " + arg);
                        }
                    } else {
                        String optionId = nameToOptionIdMap.get(parsedOptionName);
                        if (optionId == null) {
                            throw new CommandLineParserParseError("Option " + parsedOptionName + " not found");
                        }
                        currentOption = optionMap.get(optionId);
                        if (currentOption == null) {
                            throw new CommandLineParserParseError("Option " + parsedOptionName + " not found");
                        }
                        options.put(currentOption.id(), null);
                        if (currentOption.isFlag()) {
                            mode = 2;
                        } else {
                            mode = 1;
                        }
                    }
                }
            } else if (mode == 1) {
                currentOption.setValue(arg);
                options.put(currentOption.id(), arg);
                mode = 2;
                currentOption = null;
            } else if (mode == 3) {
                parameters.add(arg);
            }
        }

        if (currentOption != null && currentOption.isFlag() && currentOption.getValue() == null) {
            throw new CommandLineParserParseError("Invalid option: " + currentOption.id());
        }

        options.keySet().forEach(optionId -> {
            Option option = optionMap.get(optionId);
            System.out.printf("option[%s]: %s\n", optionId, option);
            parsedResult.recordOption(option);
        });
        parameters.forEach(p -> {
            System.out.printf("parameter: %s\n", p);
            parsedResult.recordParameter(p);
        });

        return parsedResult;
    }
}
