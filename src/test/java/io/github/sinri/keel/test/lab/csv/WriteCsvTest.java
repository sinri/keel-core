package io.github.sinri.keel.test.lab.csv;

import io.github.sinri.keel.facade.tesuto.instant.InstantRunUnit;
import io.github.sinri.keel.facade.tesuto.instant.KeelInstantRunner;
import io.github.sinri.keel.integration.poi.csv.KeelCsvWriter;
import io.vertx.core.Future;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class WriteCsvTest extends KeelInstantRunner {
    @InstantRunUnit
    public Future<Void> test1() {
        String file = "src/test/resources/runtime/csv/write_test.csv";

        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("Name", "Age", "Number"));
        rows.add(List.of("Asana", "18", "1.0"));
        rows.add(List.of("Bi'zu'mi", "-73", "10000000000000.0"));
        rows.add(List.of("Ca\"la\"pa", "10000000000003", "-2.212341242345235"));
        rows.add(List.of("Da\nnue", "-1788888883427777", "0.0"));

        try (FileOutputStream fos = new FileOutputStream(file)) {
            KeelCsvWriter keelCsvWriter = new KeelCsvWriter(fos);

            return Keel.asyncCallIteratively(rows, row -> {
                try {
                    keelCsvWriter.blockWriteRow(row);
                    return Future.succeededFuture();
                } catch (IOException e) {
                    return Future.failedFuture(e);
                }
            }).compose(v -> {
                try {
                    keelCsvWriter.close();
                    return Future.succeededFuture();
                } catch (IOException e) {
                    return Future.failedFuture(e);
                }
            });

        } catch (IOException e) {
            return Future.failedFuture(e);
        }
    }
}
