package io.github.sinri.keel.test.lab.excel;

import io.github.sinri.keel.facade.tesuto.instant.InstantRunUnit;
import io.github.sinri.keel.facade.tesuto.instant.InstantRunnerResult;
import io.github.sinri.keel.facade.tesuto.instant.KeelInstantRunner;
import io.github.sinri.keel.integration.poi.excel.KeelSheet;
import io.github.sinri.keel.integration.poi.excel.KeelSheets;
import io.github.sinri.keel.integration.poi.excel.SheetsOpenOptions;
import io.github.sinri.keel.integration.poi.excel.entity.KeelSheetMatrix;
import io.github.sinri.keel.integration.poi.excel.entity.KeelSheetMatrixRow;
import io.github.sinri.keel.integration.poi.excel.entity.KeelSheetTemplatedMatrix;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class ReadTemplatedExcelTest extends KeelInstantRunner {
    private static final String file = "/Users/leqee/code/Keel/src/test/resources/excel/excel_1.xlsx";
    private static final String fileXls = "/Users/leqee/code/Keel/src/test/resources/excel/excel_4.xls";


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

    @InstantRunUnit(skip = true)
    public Future<Void> test1() {
        return KeelSheets.useSheets(new SheetsOpenOptions().setFile(file), keelSheets -> {
            KeelSheet keelSheet = keelSheets.generateReaderForSheet(0);
            KeelSheetMatrix keelSheetMatrix = keelSheet.blockReadAllRowsToMatrix(1, 6, null);
            keelSheetMatrix.getRawRowList().forEach(row -> {
                this.getLogger().info(log -> log.message("BLOCK: " + Keel.stringHelper().joinStringArray(row, ", ")));
            });

            keelSheetMatrix.getRowIterator(KeelSheetMatrixRowExt.class).forEachRemaining(r -> {
                this.getLogger().info(log -> log.message("record")
                                                .context(c -> c
                                                        .put("record_id", r.recordId())
                                                        .put("name", r.name())
                                                        .put("age", r.age())
                                                )
                );
            });
            return Future.succeededFuture();
        });
    }

    @InstantRunUnit(skip = false)
    public Future<Void> test2() {
        return KeelSheets.useSheets(new SheetsOpenOptions().setFile(file), keelSheets -> {
            KeelSheet keelSheet = keelSheets.generateReaderForSheet(0);
            return keelSheet.readAllRowsToMatrix(1, 0, null)
                            .compose(keelSheetMatrix -> {
                                keelSheetMatrix.getRawRowList().forEach(row -> {
                                    this.getLogger().info(log -> log.message("ASYNC: " + Keel.stringHelper()
                                                                                             .joinStringArray(row, "," +
                                                                                                     " ")));
                                });
                                return Future.succeededFuture();
                            })
                            .compose(v -> {
                                return Future.succeededFuture();
                            });
        });
    }

    @InstantRunUnit(skip = true)
    public Future<Void> test3() {
        return KeelSheets.useSheets(new SheetsOpenOptions().setFile(file), keelSheets -> {
            KeelSheet keelSheet = keelSheets.generateReaderForSheet(0);
            KeelSheetTemplatedMatrix templatedMatrix = keelSheet.blockReadAllRowsToTemplatedMatrix(0, 6, null);
            templatedMatrix.getRows().forEach(row -> {
                this.getLogger().info(log -> log.message("BLOCK TEMPLATED: " + row.toJsonObject()));
            });
            return Future.succeededFuture();
        });
    }

    @InstantRunUnit(skip = true)
    public Future<Void> test4() {
        return KeelSheets.useSheets(new SheetsOpenOptions().setFile(file), keelSheets -> {
            KeelSheet keelSheet = keelSheets.generateReaderForSheet(0);
            return keelSheet.readAllRowsToTemplatedMatrix(0, 7, null)
                            .compose(templatedMatrix -> {
                                templatedMatrix.getRows().forEach(row -> {
                                    this.getLogger().info(log -> log.message("ASYNC TEMPLATED: " + row.toJsonObject()));
                                });

                                return Future.succeededFuture();
                            })
                            .compose(v -> {
                                return Future.succeededFuture();
                            });
        });
    }

    @InstantRunUnit(skip = true)
    public Future<Void> test5() {
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(new File(fileXls));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return KeelSheets.useSheets(new SheetsOpenOptions().setInputStream(fileInputStream), keelSheets -> {
            KeelSheet keelSheet = keelSheets.generateReaderForSheet(0);
            return keelSheet.readAllRowsToTemplatedMatrix(0, 128, null)
                            .compose(templatedMatrix -> {
                                templatedMatrix.getRows().forEach(row -> {
                                    this.getLogger().info(log -> log.message("ASYNC TEMPLATED: " + row.toJsonObject()));
                                });

                                return Future.succeededFuture();
                            })
                            .compose(v -> {
                                return Future.succeededFuture();
                            });
        });
    }

    public static class KeelSheetMatrixRowExt extends KeelSheetMatrixRow {

        public KeelSheetMatrixRowExt(List<String> rawRow) {
            super(rawRow);
        }

        public Integer recordId() {
            return readValueToInteger(0);
        }

        public String name() {
            return readValue(1);
        }

        public double age() {
            return readValueToDouble(2);
        }
    }
}
