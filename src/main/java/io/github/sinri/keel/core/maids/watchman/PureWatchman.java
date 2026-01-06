package io.github.sinri.keel.core.maids.watchman;

import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.vertx.core.*;
import org.jspecify.annotations.NullMarked;


/**
 * 基于配置的更夫实现。
 * <p>
 * 本类实现类似单节点内无限循环调用的机制，基于异步 Promise 和集群锁。
 *
 * @since 5.0.0
 */
@NullMarked
public class PureWatchman extends WatchmanImpl {

    private final Options options;
    private final LoggerFactory loggerFactory;

    protected PureWatchman(String watchmanName, Options options, LoggerFactory loggerFactory) {
        super(watchmanName);
        this.options = options;
        this.loggerFactory = loggerFactory;
    }


    public static Future<String> deploy(
            Vertx vertx,
            String watchmanName,
            Handler<Options> optionsHandler,
            LoggerFactory loggerFactory
    ) {
        Options options = new Options();
        optionsHandler.handle(options);
        PureWatchman keelPureWatchman = new PureWatchman(watchmanName, options, loggerFactory);
        return vertx.deployVerticle(keelPureWatchman, new DeploymentOptions()
                .setThreadingModel(ThreadingModel.WORKER)
        );
    }

    @Override

    protected final LoggerFactory getLoggerFactory() {
        return loggerFactory;
    }


    public WatchmanEventHandler regularHandler() {
        return options.getHandler();
    }

    @Override
    public long interval() {
        return options.getInterval();
    }

    @NullMarked
    public static class Options {

        private WatchmanEventHandler handler;
        private long interval = 60_000L;

        public Options() {
            this.handler = event -> {
            };
        }


        public WatchmanEventHandler getHandler() {
            return handler;
        }


        public Options setHandler(WatchmanEventHandler handler) {
            this.handler = handler;
            return this;
        }

        public long getInterval() {
            return interval;
        }


        public Options setInterval(long interval) {
            this.interval = interval;
            return this;
        }

    }
}
