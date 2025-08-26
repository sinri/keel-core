package io.github.sinri.keel.facade.cli;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Option {
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
        this.aliasSet.add(alias);
        return this;
    }

    public Set<String> getAliasSet() {
        return aliasSet;
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
