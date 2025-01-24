package io.github.sinri.keel.test.lab.csv;

import io.github.sinri.keel.facade.tesuto.KeelTest;
import io.github.sinri.keel.facade.tesuto.TestUnit;
import io.github.sinri.keel.integration.poi.csv.KeelCsvWriter;
import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.List;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class WriteCsvTest extends KeelTest {
    @TestUnit
    public Future<Void> test1() {
        String file = "/Users/leqee/code/Keel/src/test/resources/runtime/csv/write_test.csv";

        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("Name", "Age", "Number"));
        rows.add(List.of("Asana", "18", "1.0"));
        rows.add(List.of("Bi'zu'mi", "-73", "10000000000000.0"));
        rows.add(List.of("Ca\"la\"pa", "10000000000003", "-2.212341242345235"));
        rows.add(List.of("Da\nnue", "-1788888883427777", "0.0"));

        return KeelCsvWriter.create(file)
                .compose(keelCsvWriter -> {
                    return Keel.asyncCallIteratively(rows, keelCsvWriter::writeRow)
                            .compose(v -> {
                                return keelCsvWriter.close();
                            });
                });
    }
}
