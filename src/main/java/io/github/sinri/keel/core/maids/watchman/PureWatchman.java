package io.github.sinri.keel.core.maids.watchman;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.ThreadingModel;
import org.jetbrains.annotations.NotNull;


/**
 * 基于配置的更夫实现。
 * <p>
 * 本类实现类似单节点内无限循环调用的机制，基于异步 Promise 和集群锁。
 *
 * @since 5.0.0
 */
public class PureWatchman extends WatchmanImpl {

    @NotNull
    private final Options options;
    @NotNull
    private final LoggerFactory loggerFactory;

    protected PureWatchman(@NotNull Keel keel, @NotNull String watchmanName, @NotNull Options options, @NotNull LoggerFactory loggerFactory) {
        super(keel, watchmanName);
        this.options = options;
        this.loggerFactory = loggerFactory;
    }

    @NotNull
    public static Future<String> deploy(
            @NotNull Keel keel,
            @NotNull String watchmanName,
            @NotNull Handler<Options> optionsHandler,
            @NotNull LoggerFactory loggerFactory
    ) {
        Options options = new Options();
        optionsHandler.handle(options);
        PureWatchman keelPureWatchman = new PureWatchman(keel, watchmanName, options, loggerFactory);
        return keel.getVertx().deployVerticle(keelPureWatchman, new DeploymentOptions()
                .setThreadingModel(ThreadingModel.WORKER)
        );
    }

    @Override
    @NotNull
    protected final LoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @NotNull
    public KeelWatchmanEventHandler regularHandler() {
        return options.getHandler();
    }

    @Override
    public long interval() {
        return options.getInterval();
    }

    public static class Options {
        @NotNull
        private KeelWatchmanEventHandler handler;
        private long interval = 60_000L;

        public Options() {
            this.handler = event -> {
            };
        }

        @NotNull
        public KeelWatchmanEventHandler getHandler() {
            return handler;
        }

        @NotNull
        public Options setHandler(@NotNull KeelWatchmanEventHandler handler) {
            this.handler = handler;
            return this;
        }

        public long getInterval() {
            return interval;
        }

        @NotNull
        public Options setInterval(long interval) {
            this.interval = interval;
            return this;
        }

    }
}
