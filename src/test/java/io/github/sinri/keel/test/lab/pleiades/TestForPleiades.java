package io.github.sinri.keel.test.lab.pleiades;

import io.github.sinri.keel.core.maids.pleiades.Pleiades;
import io.github.sinri.keel.facade.tesuto.KeelTest;
import io.github.sinri.keel.facade.tesuto.TestUnit;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageProducer;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class TestForPleiades extends KeelTest {
    @TestUnit
    public Future<Void> run() {
        AtomicReference<String> deploymentIdRef = new AtomicReference<>();
        return Keel.getVertx().deployVerticle(
                        PleiadesSample.class,
                        new DeploymentOptions()
                                .setInstances(2)
                                .setThreadingModel(ThreadingModel.WORKER)
                )
                .compose(deployed -> {
                    getLogger().info("TestForPleiades deployed: " + deployed);
                    deploymentIdRef.set(deployed);

                    MessageProducer<String> sender = PleiadesSample.generateMessageProducer(
                            PleiadesSample.ADDRESS,
                            new DeliveryOptions()
                                    .setSendTimeout(100L));

                    return Keel.asyncCallStepwise(5, integer -> {
                                PleiadesSample.sendMessage("A[" + integer + "]");
                                return Keel.asyncSleep(500L);
                            })
                            .compose(v -> {
                                return Keel.asyncCallStepwise(5000, integer -> {
                                    sender.write("B[" + integer + "]");
                                    return Keel.asyncSleep(500L);
                                });
                            })
                            .compose(v -> {
                                return Keel.asyncSleep(60_000L)
                                        .compose(vv -> {
                                            return Keel.getVertx().undeploy(deploymentIdRef.get())
                                                    .compose(x -> {
                                                        getLogger().info("UNDEPLOYED " + deploymentIdRef.get());
                                                        return Future.succeededFuture();
                                                    });
                                        });
                            })
                            .compose(v -> {
                                PleiadesSample.sendMessage("C");
                                return sender.write("D");
                            });
                })
                .compose(v -> {
                    return Keel.asyncSleep(60_000L);
                });
    }

    public static class PleiadesSample extends Pleiades<String> {
        public final static String ADDRESS = "PleiadesSample";
        private final String uuid;


        public PleiadesSample() {
            this.uuid = UUID.randomUUID().toString();
        }

        @Deprecated
        public static void sendMessage(String content) {
            Keel.getVertx().eventBus().send(ADDRESS, content);
        }

        @Override
        protected KeelEventLogger buildEventLogger() {
            return KeelIssueRecordCenter.outputCenter()
                    .generateEventLogger("RobertaSample", x -> x.classification(uuid));
        }

        @Override
        public String getAddress() {
            return ADDRESS;
        }

        @Override
        protected void handleMessage(Message<String> message) {
            getLogger().info("start with " + message.body());
            Keel.asyncSleep(2_000L)
                    .andThen(slept -> {
                        getLogger().info("end with " + message.body());
                    });
        }
    }
}
