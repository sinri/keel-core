package io.github.sinri.keel.test.lab.csv;

import io.github.sinri.keel.facade.tesuto.instant.InstantRunUnit;
import io.github.sinri.keel.facade.tesuto.instant.KeelInstantRunner;
import io.github.sinri.keel.integration.poi.csv.CsvRow;
import io.github.sinri.keel.integration.poi.csv.KeelCsvReader;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ReadCsvTest extends KeelInstantRunner {
    @InstantRunUnit
    public Future<Void> test() {
        String file = "src/test/resources/runtime/csv/write_test.csv";

        try {
            FileInputStream fis = new FileInputStream(file);
            return KeelCsvReader.read(fis, StandardCharsets.UTF_8, ",", reader -> {
                try {
                    int index = 0;
                    while (true) {
                        CsvRow csvRow = reader.next();
                        if (csvRow == null) {
                            getInstantLogger().warning(r -> r.message("CSV OVER"));
                            break;
                        }
                        JsonArray array = new JsonArray();
                        for (int i = 0; i < csvRow.size(); i++) {
                            array.add(csvRow.getCell(i).getString());
                        }
                        int finalIndex = index;
                        getInstantLogger().info(log -> log.message("ROW")
                                .context(c -> c
                                        .put("i", finalIndex)
                                        .put("cell", array)
                                )
                        );
                        index++;
                    }
                    return Future.succeededFuture();
                } catch (IOException e) {
                    return Future.failedFuture(e);
                }
            });
        } catch (IOException e) {
            getInstantLogger().exception(e);
            return Future.failedFuture(e);
        }
    }
}
