package io.github.sinri.keel.facade.cli;

import javax.annotation.Nonnull;

interface KeelCliArgsWriter {
    static KeelCliArgsWriter create() {
        return new KeelCliArgsImpl();
    }

    void recordParameter(@Nonnull String parameter);

    void recordOption(@Nonnull KeelCliOption option);

    KeelCliArgs toResult();
}
