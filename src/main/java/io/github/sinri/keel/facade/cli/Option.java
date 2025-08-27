package io.github.sinri.keel.facade.cli;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class Option {
    private final static Pattern VALID_ALAIS_PATTERN = Pattern.compile("^[A-Za-z0-9_.][A-Za-z0-9_.-]*$");
    private final static Pattern VALID_SHORT_PATTERN = Pattern.compile("^-[A-Za-z0-9_]$");
    private final static Pattern VALID_LONG_PATTERN = Pattern.compile("^--[A-Za-z0-9_.][A-Za-z0-9_.-]*$");
    @Nonnull
    private final String id;
    @Nonnull
    private final Set<String> aliasSet;
    @Nullable
    private String description;
    private boolean flag;
    @Nullable
    private String value;
    public Option() {
        this.id = UUID.randomUUID().toString();
        this.aliasSet = new HashSet<>();
    }

    @Nullable
    public static String parseOptionName(@Nonnull String argument) {
        if (argument.startsWith("--")) {
            if (VALID_LONG_PATTERN.matcher(argument).matches()) {
                return argument.substring(2);
            }
        }
        if (argument.startsWith("-")) {
            if (VALID_SHORT_PATTERN.matcher(argument).matches()) {
                return argument.substring(1);
            }
        }
        return null;
    }

    public static void validateAlias(String alias) {
        if (alias == null || !VALID_ALAIS_PATTERN.matcher(alias).matches()) {
            throw new IllegalArgumentException("Alias cannot be null");
        }
    }

    public final String id() {
        return id;
    }

    public String description() {
        return description;
    }

    public Option description(String description) {
        this.description = description;
        return this;
    }

    public boolean isFlag() {
        return flag;
    }

    public Option flag() {
        this.flag = true;
        return this;
    }

    public Option alias(@Nonnull String alias) {
        validateAlias(alias);
        this.aliasSet.add(alias);
        return this;
    }

    public Set<String> getAliasSet() {
        return Collections.unmodifiableSet(aliasSet);
    }

    @Nullable
    public String getValue() {
        return value;
    }

    public Option setValue(@Nullable String value) {
        this.value = value;
        return this;
    }
}
