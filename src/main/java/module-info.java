module io.github.sinri.keel.core {
    requires com.github.oshi;
    requires transitive io.github.sinri.keel.base;
    requires transitive io.github.sinri.keel.logger.api;
    requires transitive io.vertx.auth.common;
    requires transitive io.vertx.core;
    requires transitive io.vertx.mail.client;
    requires java.management;
    requires org.commonmark;
    requires org.commonmark.ext.gfm.tables;
    requires transitive org.jetbrains.annotations;

    // Exports for public API packages

    // Core functionality
    exports io.github.sinri.keel.core.cache;
    exports io.github.sinri.keel.core.cutter;
    exports io.github.sinri.keel.core.markdown;

    // Integration packages
    exports io.github.sinri.keel.core.integration.email.smtp;

    // Maids (background services)
    exports io.github.sinri.keel.core.maids.gatling;
    exports io.github.sinri.keel.core.maids.pleiades;
    exports io.github.sinri.keel.core.maids.watchman;

    // Servant (worker services)
    exports io.github.sinri.keel.core.servant.funnel;
    exports io.github.sinri.keel.core.servant.intravenous;
    exports io.github.sinri.keel.core.servant.queue;
    exports io.github.sinri.keel.core.servant.sundial;

    // Utility packages
    exports io.github.sinri.keel.core.utils;
    exports io.github.sinri.keel.core.utils.cron;
    exports io.github.sinri.keel.core.utils.io;
    exports io.github.sinri.keel.core.utils.runtime;
    exports io.github.sinri.keel.core.utils.value;

    // Authentication utilities
    exports io.github.sinri.keel.core.utils.authenticator.googleauth;
    exports io.github.sinri.keel.core.utils.authenticator.googleauth.async;
    exports io.github.sinri.keel.core.utils.authenticator.googleauth.sync;

    // Encryption utilities
    exports io.github.sinri.keel.core.utils.encryption.aes;
    //    exports io.github.sinri.keel.core.utils.encryption.base32;
    //    exports io.github.sinri.keel.core.utils.encryption.bcrypt;
    exports io.github.sinri.keel.core.utils.encryption.rsa;

    // Opens for reflection-based operations
    opens io.github.sinri.keel.core.utils;
    opens io.github.sinri.keel.core.maids.watchman;
    opens io.github.sinri.keel.core.utils.encryption.aes;
}