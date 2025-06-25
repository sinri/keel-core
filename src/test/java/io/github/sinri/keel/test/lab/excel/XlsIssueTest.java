package io.github.sinri.keel.test.lab.excel;

import io.github.sinri.keel.facade.tesuto.instant.InstantRunUnit;
import io.github.sinri.keel.facade.tesuto.instant.InstantRunnerResult;
import io.github.sinri.keel.facade.tesuto.instant.KeelInstantRunner;
import io.github.sinri.keel.integration.poi.excel.KeelSheet;
import io.github.sinri.keel.integration.poi.excel.KeelSheets;
import io.github.sinri.keel.integration.poi.excel.SheetsOpenOptions;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.List;

public class XlsIssueTest extends KeelInstantRunner {
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

    @InstantRunUnit
    public Future<Void> giveMeFive() {
        var path = "/Users/leqee/code/Keel/src/test/resources/excel/excel_5.xls";
        return KeelSheets.useSheets(
                new SheetsOpenOptions().setFile(path),
                keelSheets -> {
                    //KeelSheets.autoGenerate(fs);
                    KeelSheet keelSheet = keelSheets.generateReaderForSheet(1);
                    return keelSheet.readAllRowsToMatrix()
                                    .compose(matrix -> {
                                        matrix.getRawRowList().forEach(rawRow -> {
                                            getInstantLogger().info(r -> r.message("row: " + rawRow.toString()));
                                        });
                                        return Future.succeededFuture();
                                    });
                }
        );
    }
}
