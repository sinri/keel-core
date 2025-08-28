package io.github.sinri.keel.facade.cli;

import io.github.sinri.keel.core.TechnicalPreview;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@TechnicalPreview(since = "4.1.1")
class KeelCliArgsImpl implements KeelCliArgs, KeelCliArgsWriter {
    private final Map<Character, KeelCliOption> shortMap;
    private final Map<String, KeelCliOption> longMap;
    private final List<String> parameters;

    public KeelCliArgsImpl() {
        this.shortMap = new HashMap<>();
        this.longMap = new HashMap<>();
        this.parameters = new ArrayList<>();
    }

    public void recordOption(@Nonnull KeelCliOption option) {
        Set<String> aliasSet = option.getAliasSet();
        for (var alias : aliasSet) {
            if (alias == null || alias.isEmpty()) continue;
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
        KeelCliOption option = shortMap.get(shortName);
        if (option == null) return null;
        if (option.isFlag()) return "";
        return option.getValue();
    }

    @Nullable
    @Override
    public String readOption(@Nonnull String longName) {
        if (longName.trim().isEmpty()) {
            return null;
        }

        KeelCliOption option = longMap.get(longName);
        if (option == null) return null;
        if (option.isFlag()) return "";
        return option.getValue();
    }

    @Override
    public boolean readFlag(char shortName) {
        return shortMap.containsKey(shortName);
    }

    @Override
    public boolean readFlag(@Nonnull String longName) {
        if (longName.trim().isEmpty()) {
            return false;
        }
        return longMap.containsKey(longName);
    }

    @Nullable
    @Override
    public String readParameter(int index) {
        if (index < 0 || index >= parameters.size()) {
            return null;
        }
        return parameters.get(index);
    }

    @Override
    public void recordParameter(@Nonnull String parameter) {
        parameters.add(parameter);
    }

    @Override
    public KeelCliArgs toResult() {
        return this;
    }
}
