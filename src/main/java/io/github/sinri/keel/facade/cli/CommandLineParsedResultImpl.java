package io.github.sinri.keel.facade.cli;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

class CommandLineParsedResultImpl implements CommandLineParsedResult {
    private final Map<Character, Option> shortMap;
    private final Map<String, Option> longMap;
    private final List<String> parameters;

    public CommandLineParsedResultImpl() {
        this.shortMap = new HashMap<>();
        this.longMap = new HashMap<>();
        this.parameters = new ArrayList<>();
    }

    public void recordOption(@Nonnull Option option) {
        Set<String> aliasSet = option.getAliasSet();
        for (var alias : aliasSet) {
            if (alias == null) continue;
            if (alias.isEmpty()) continue;
            if (alias.length() == 1) {
                shortMap.put(alias.charAt(0), option);
            } else {
                longMap.put(alias, option);
            }
        }
    }

    @Nullable
    @Override
    public String readOption(char shortName) {
        Option option = shortMap.get(shortName);
        if (option == null) return null;
        if (option.isFlag()) return "";
        return option.getValue();
    }

    @Nullable
    @Override
    public String readOption(@Nonnull String longName) {
        Option option = longMap.get(longName);
        if (option == null) return null;
        if (option.isFlag()) return "";
        return option.getValue();
    }

    @Override
    public boolean readFlag(char shortName) {
        return readOption(shortName) != null;
    }

    @Override
    public boolean readFlag(@Nonnull String longName) {
        return readOption(longName) != null;
    }

    @Nullable
    @Override
    public String readParameter(int index) {
        try {
            return parameters.get(index);
        } catch (IndexOutOfBoundsException ignored) {
            return null;
        }
    }

    @Override
    public void recordParameter(@Nonnull String parameter) {
        parameters.add(parameter);
    }
}
