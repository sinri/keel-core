package io.github.sinri.keel.test.lab.excel;

import com.github.pjfanning.xlsx.StreamingReader;
import io.github.sinri.keel.facade.tesuto.instant.InstantRunUnit;
import io.github.sinri.keel.facade.tesuto.instant.InstantRunUnitSkipped;
import io.github.sinri.keel.facade.tesuto.instant.KeelInstantRunner;
import io.github.sinri.keel.integration.poi.excel.KeelSheet;
import io.github.sinri.keel.integration.poi.excel.KeelSheets;
import io.github.sinri.keel.integration.poi.excel.SheetsOpenOptions;
import io.vertx.core.Future;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class StaxTest extends KeelInstantRunner {
    @InstantRunUnit
    @InstantRunUnitSkipped
    public Future<Void> test1() {
        FileInputStream in = null;
        try {
            in = new FileInputStream("/Users/sinri/code/keel/src/test/resources/runtime/huge.xlsx");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        var wk = StreamingReader.builder()
                                //缓存到内存中的行数，默认是10
                                .rowCacheSize(100)
                                //读取资源时，缓存到内存的字节大小，默认是1024
                                .bufferSize(4096)
                                //打开资源，必须，可以是InputStream或者是File
                                .open(in);
        Sheet sheet = wk.getSheetAt(0);

        for (Row r : sheet) {
            System.out.print("第" + r.getRowNum() + "行：");
            for (Cell c : r) {
                if (c != null) {
                    System.out.print(c.getStringCellValue() + " ");
                }
            }
            System.out.println();
        }

        return Future.succeededFuture();
    }

    @InstantRunUnit
    public Future<Void> test2() {
        return KeelSheets.useSheets(new SheetsOpenOptions()
                        .setHugeXlsxStreamingReaderBuilder(builder -> {
                            //                    builder
                            //                            .rowCacheSize(100)//缓存到内存中的行数，默认是10
                            //                            .bufferSize(4096)//读取资源时，缓存到内存的字节大小，默认是1024
                        })
                        .setFile("/Users/sinri/code/keel/src/test/resources/runtime/huge.xlsx")
                , keelSheets -> {
                    KeelSheet keelSheet = keelSheets.generateReaderForSheet(0);
                    keelSheet.getMatrixRowIterator(3, null).forEachRemaining(row -> {
                        getInstantLogger().info("<" + row.readValue(0) + "> " + row.readValueToBigDecimal(1));
                    });
                    return Future.succeededFuture();
                });
    }
}
