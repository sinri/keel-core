package io.github.sinri.keel.test.lab.excel;

import io.github.sinri.keel.facade.tesuto.instant.InstantRunUnit;
import io.github.sinri.keel.facade.tesuto.instant.InstantRunUnitSkipped;
import io.github.sinri.keel.facade.tesuto.instant.InstantRunnerResult;
import io.github.sinri.keel.facade.tesuto.instant.KeelInstantRunner;
import io.github.sinri.keel.integration.poi.excel.KeelSheet;
import io.github.sinri.keel.integration.poi.excel.KeelSheets;
import io.github.sinri.keel.integration.poi.excel.SheetsCreateOptions;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class WriteHugeExcelTest extends KeelInstantRunner {
    private static final String file = "/Users/sinri/code/keel/src/test/resources/runtime/huge.xlsx";


    @Nonnull
    @Override
    protected Future<Void> starting() {
        return Future.succeededFuture();
    }

    @Nonnull
    @Override
    protected Future<Void> ending(List<InstantRunnerResult> testUnitResults) {
        return Future.succeededFuture();
    }

    //@TestUnit
    public Future<Void> test1() {
        return KeelSheets.useSheets(new SheetsCreateOptions(), sheets -> {
            KeelSheet sheet = sheets.generateWriterForSheet("Needs");
            sheet.blockWriteAllRows(List.of(
                    List.of("Name", "Need", "Note"),
                    List.of("Tim", "Apple", "small"),
                    List.of("Steve", "Pear", "round"),
                    List.of("Wake", "Banana", "long")
            ), 10, 10);
            sheets.save(file);
            return Future.succeededFuture();
        });
    }

    @InstantRunUnit
    @InstantRunUnitSkipped
    public Future<Void> testWriteNotStream() {
        return KeelSheets.useSheets(new SheetsCreateOptions(), sheets -> {
            KeelSheet sheet = sheets.generateWriterForSheet("Huge");

            return write20wRows(sheet)
                    .compose(v -> {
                        sheets.save(file);
                        return Future.succeededFuture();
                    });
        });
    }

    @InstantRunUnit
    public Future<Void> testWriteStream() {
        return KeelSheets.useSheets(new SheetsCreateOptions()
                .setUseStreamWriting(true), sheets -> {
            KeelSheet sheet = sheets.generateWriterForSheet("Huge");

            return write20wRows(sheet)
                    .compose(v -> {
                        sheets.save(file);
                        return Future.succeededFuture();
                    });
        });
    }


    private Future<Void> write20wRows(KeelSheet sheet) {
        long startTime = System.currentTimeMillis();

        List<String> headerRow = new ArrayList<>();
        headerRow.add("INDEX");
        headerRow.add("SPENT");
        sheet.blockWriteAllRows(List.of(headerRow));

        return Keel.asyncCallStepwise(200, ii -> {
            List<List<String>> buffer = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                List<String> row = new ArrayList<>();
                row.add(String.valueOf(ii * 1000 + i));
                row.add(String.valueOf((System.currentTimeMillis() - startTime) / 1000.0));
                buffer.add(row);
            }
            sheet.blockWriteAllRows(buffer, Math.toIntExact(ii * 1000), 0);
            return Future.succeededFuture();
        });
    }
}
