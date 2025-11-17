module io.github.sinri.keel.core {
    requires com.github.oshi;
    requires io.github.sinri.keel.base;
    requires io.github.sinri.keel.logger.api;
    requires io.vertx.auth.common;
    requires io.vertx.core;
    requires io.vertx.mail.client;
    requires java.logging;
    requires java.management;
    requires org.commonmark;
    requires org.commonmark.ext.gfm.tables;
    requires org.jetbrains.annotations;

    // Exports for public API packages
    exports io.github.sinri.keel.core.cache;
    exports io.github.sinri.keel.core.cutter;
    exports io.github.sinri.keel.core.integration.email.smtp;
    exports io.github.sinri.keel.core.maids.gatling;
    exports io.github.sinri.keel.core.maids.pleiades;
    exports io.github.sinri.keel.core.maids.watchman;
    exports io.github.sinri.keel.core.markdown;
    exports io.github.sinri.keel.core.servant.funnel;
    exports io.github.sinri.keel.core.servant.intravenous;
    exports io.github.sinri.keel.core.servant.queue;
    exports io.github.sinri.keel.core.servant.sundial;
    exports io.github.sinri.keel.core.utils;
    exports io.github.sinri.keel.core.utils.authenticator.googleauth;
    exports io.github.sinri.keel.core.utils.authenticator.googleauth.async;
    exports io.github.sinri.keel.core.utils.authenticator.googleauth.sync;
    exports io.github.sinri.keel.core.utils.encryption.aes;
    exports io.github.sinri.keel.core.utils.encryption.bcrypt;
    exports io.github.sinri.keel.core.utils.encryption.rsa;
    exports io.github.sinri.keel.core.utils.runtime;
}