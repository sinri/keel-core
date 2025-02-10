package io.github.sinri.keel.test.lab.unit;

import io.github.sinri.keel.facade.tesuto.instant.InstantRunUnit;
import io.github.sinri.keel.facade.tesuto.instant.KeelInstantRunner;
import io.vertx.core.Future;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class HikaruTest extends KeelInstantRunner {
    @InstantRunUnit
    public Future<Void> testHikaruForPikas() {
        Pikas all = Keel.reflectionHelper().getAnnotationOfClass(Hikaru.class, Pikas.class);
        for (Pika pika : all.value()) {
            getIssueRecorder().info(r -> r.message("testHikaruForPikas: Each Pika of Hikaru: " + pika.value()));
        }
        return Future.succeededFuture();
    }

    @InstantRunUnit
    public Future<Void> testHikaruForPika() {
        Pika[] annotationsOfClass = Keel.reflectionHelper().getAnnotationsOfClass(Hikaru.class, Pika.class);
        for (Pika pika : annotationsOfClass) {
            getIssueRecorder().info(r -> r.message("testHikaruForPika: Each Pika of Hikaru: " + pika.value()));
        }
        return Future.succeededFuture();
    }
}
