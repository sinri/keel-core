package io.github.sinri.keel.test.lab.excel;

import io.github.sinri.keel.facade.tesuto.instant.InstantRunUnit;
import io.github.sinri.keel.facade.tesuto.instant.InstantRunnerResult;
import io.github.sinri.keel.facade.tesuto.instant.KeelInstantRunner;
import io.github.sinri.keel.integration.poi.excel.KeelSheet;
import io.github.sinri.keel.integration.poi.excel.KeelSheets;
import io.github.sinri.keel.integration.poi.excel.SheetsOpenOptions;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class ReadHugeExcelTest extends KeelInstantRunner {
    private static final String file = "/Users/leqee/code/Keel/src/test/resources/excel/excel_0.xlsx";

    @Nonnull
    @Override
    protected Future<Void> starting() {
        var issueRecorder = KeelIssueRecordCenter.outputCenter().generateEventLogger(getClass().getSimpleName());
        setLogger(issueRecorder);

        //        try {
        //            this.excelStreamReader = new KeelStreamSheets(file);
        //        } catch (IOException e) {
        //            return Future.failedFuture(e);
        //        }
        return Future.succeededFuture();
    }

    @Nonnull
    @Override
    protected Future<Void> ending(List<InstantRunnerResult> testUnitResults) {
        //        try {
        //            this.excelStreamReader.close();
        //        } catch (IOException e) {
        //            return Future.failedFuture(e);
        //        }
        return Future.succeededFuture();
    }

    /**
     * Read 20w rows, SYNC. Test Result: UNIT [test1] PASSED. Spent 10093 ms;
     */
    @InstantRunUnit
    public Future<Void> test1() {
        return KeelSheets.useSheets(new SheetsOpenOptions().setFile(file), excelStreamReader -> {
            KeelSheet excelSheetReader = excelStreamReader.generateReaderForSheet(0);
            AtomicInteger x = new AtomicInteger(0);
            excelSheetReader.blockReadAllRows(row -> {
                String index = row.getCell(0).getStringCellValue();
                String spent = row.getCell(1).getStringCellValue();
                //            this.logger().info("index: " + index + " spent: " + spent);
                x.incrementAndGet();
            });
            this.getLogger().info(r -> r.message("FIN 1 " + x.get()));

            return Future.succeededFuture();
        });
    }

    /**
     * Read 20w rows, ASYNC.
     */
    @InstantRunUnit
    public Future<Void> test2() {
        return KeelSheets.useSheets(new SheetsOpenOptions().setFile(file), excelStreamReader -> {
            KeelSheet excelSheetReader = excelStreamReader.generateReaderForSheet(0);
            AtomicInteger x = new AtomicInteger(0);
            return excelSheetReader.readAllRows(rows -> {
                                       //                    String index = row.getCell(0).getStringCellValue();
                                       //                    String spent = row.getCell(1).getStringCellValue();
                                       //                    this.logger().info("index: " + index + " spent: " + spent);
                                       x.addAndGet(rows.size());
                                       return Future.succeededFuture();
                                   }, 1000)
                                   .compose(v -> {
                                       this.getLogger().info(r -> r.message("FIN 2 " + x.get()));
                                       return Future.succeededFuture();
                                   });
        });
    }
}
