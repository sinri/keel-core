package io.github.sinri.keel.web.http;

import io.github.sinri.keel.core.verticles.KeelVerticleImpl;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import javax.annotation.Nonnull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

abstract public class KeelHttpServer extends KeelVerticleImpl {
    public static final String CONFIG_HTTP_SERVER_PORT = "http_server_port";
    public static final String CONFIG_HTTP_SERVER_OPTIONS = "http_server_options";
    public static final String CONFIG_IS_MAIN_SERVICE = "is_main_service";
    protected HttpServer server;
    private KeelIssueRecorder<KeelEventLog> httpServerLogger;

    protected int getHttpServerPort() {
        return this.config().getInteger(CONFIG_HTTP_SERVER_PORT, 8080);
    }

    protected HttpServerOptions getHttpServerOptions() {
        JsonObject httpServerOptions = this.config().getJsonObject(CONFIG_HTTP_SERVER_OPTIONS);
        if (httpServerOptions == null) {
            return new HttpServerOptions()
                    .setPort(getHttpServerPort());
        } else {
            return new HttpServerOptions(httpServerOptions);
        }
    }

    protected boolean isMainService() {
        return this.config().getBoolean(CONFIG_IS_MAIN_SERVICE, true);
    }

    protected abstract void configureRoutes(Router router);

    @Override
    protected Future<Void> startVerticle() {
        this.httpServerLogger = buildHttpServerIssueRecorder();

        this.server = Keel.getVertx().createHttpServer(getHttpServerOptions());

        Router router = Router.router(Keel.getVertx());
        this.configureRoutes(router);

        server.requestHandler(router)
              .exceptionHandler(throwable -> getHttpServerLogger().exception(throwable, r -> r.message("KeelHttpServer Exception")))
              .listen()
              .onComplete(httpServerAsyncResult -> {
                  if (httpServerAsyncResult.succeeded()) {
                      HttpServer httpServer = httpServerAsyncResult.result();
                      getHttpServerLogger().info(r -> r.message("HTTP Server Established, Actual Port: " + httpServer.actualPort()));
                  } else {
                      Throwable throwable = httpServerAsyncResult.cause();
                      getHttpServerLogger().exception(throwable, r -> r.message("Listen failed"));

                      if (this.isMainService()) {
                          Keel.gracefullyClose(Promise::complete);
                      }
                  }
              });

        return Future.succeededFuture();
    }

    /**
     * @since 4.0.2
     */
    @Nonnull
    protected KeelIssueRecorder<KeelEventLog> buildHttpServerIssueRecorder() {
        return KeelIssueRecordCenter.outputCenter().generateIssueRecorder("KeelHttpServer", KeelEventLog::new);
    }

    /**
     * @since 4.0.2
     */
    public KeelIssueRecorder<KeelEventLog> getHttpServerLogger() {
        return httpServerLogger;
    }

    public void terminate(Promise<Void> promise) {
        server.close().andThen(ar -> {
                  if (ar.succeeded()) {
                      getHttpServerLogger().info(r -> r.message("HTTP Server Closed"));
                      promise.complete();
                  } else {
                      getHttpServerLogger().exception(ar.cause(),
                              r -> r.message("HTTP Server Closing Failure: " + ar.cause()
                                                                                 .getMessage()));
                      promise.fail(ar.cause());
                  }
              })
              .andThen(ar -> {
                  if (this.isMainService()) {
                      Keel.gracefullyClose(Promise::complete);
                  }
              });
    }
}
