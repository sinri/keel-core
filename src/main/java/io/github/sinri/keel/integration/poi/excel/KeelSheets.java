package io.github.sinri.keel.integration.poi.excel;

import io.github.sinri.keel.core.ValueBox;
import io.vertx.core.Closeable;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.Objects;
import java.util.function.Function;

/**
 * @since 3.0.13
 * @since 3.0.18 Finished Technical Preview.
 * @since 4.0.2 implements `io.vertx.core.Closeable`, remove `java.lang.AutoCloseable`.
 */
public class KeelSheets implements Closeable {
    /**
     * @since 3.1.3
     */
    private final @Nullable FormulaEvaluator formulaEvaluator;
    protected @Nonnull Workbook autoWorkbook;

    /**
     * @param workbook The generated POI Workbook Implementation.
     * @since 3.0.20
     */
    protected KeelSheets(@Nonnull Workbook workbook) {
        this(workbook, false);
    }

    /**
     * Create a new Sheets.
     */
    protected KeelSheets() {
        this(null, false);
    }

    /**
     * Open an existed workbook or create. Not use stream-write mode by default.
     *
     * @param workbook if null, create a new Sheets; otherwise, use it.
     * @since 3.1.3
     */
    protected KeelSheets(@Nullable Workbook workbook, boolean withFormulaEvaluator) {
        autoWorkbook = Objects.requireNonNullElseGet(workbook, XSSFWorkbook::new);
        if (withFormulaEvaluator) {
            formulaEvaluator = autoWorkbook.getCreationHelper().createFormulaEvaluator();
        } else {
            formulaEvaluator = null;
        }
    }

    /**
     * @since 4.0.2
     */
    public static <T> Future<T> useSheets(@Nonnull SheetsOpenOptions sheetsOpenOptions,
                                          @Nonnull Function<KeelSheets, Future<T>> usage) {
        return Future.succeededFuture()
                     .compose(v -> {
                         try {
                             KeelSheets keelSheets;
                             if (sheetsOpenOptions.isUseHugeXlsxStreamReading()) {
                                 if (sheetsOpenOptions.getInputStream() != null) {
                                     keelSheets = new KeelSheets(sheetsOpenOptions.getHugeXlsxStreamingReaderBuilder()
                                                                                  .open(sheetsOpenOptions.getInputStream())
                                     );
                                 } else if (sheetsOpenOptions.getFile() != null) {
                                     keelSheets = new KeelSheets(sheetsOpenOptions.getHugeXlsxStreamingReaderBuilder()
                                                                                  .open(sheetsOpenOptions.getFile())
                                     );
                                 } else {
                                     throw new IOException("No input source!");
                                 }
                             } else {
                                 InputStream inputStream = sheetsOpenOptions.getInputStream();
                                 if (inputStream != null) {
                                     Workbook workbook;
                                     Boolean useXlsx = sheetsOpenOptions.isUseXlsx();
                                     if (useXlsx == null) {
                                         try {
                                             workbook = new XSSFWorkbook(inputStream);
                                         } catch (IOException e) {
                                             try {
                                                 workbook = new HSSFWorkbook(inputStream);
                                             } catch (IOException ex) {
                                                 throw new RuntimeException(ex);
                                             }
                                         }
                                     } else {
                                         if (useXlsx) {
                                             workbook = new XSSFWorkbook(inputStream);
                                         } else {
                                             workbook = new HSSFWorkbook(inputStream);
                                         }
                                     }
                                     keelSheets = new KeelSheets(workbook, sheetsOpenOptions.isWithFormulaEvaluator());
                                 } else if (sheetsOpenOptions.getFile() != null) {
                                     keelSheets = new KeelSheets(
                                             WorkbookFactory.create(sheetsOpenOptions.getFile()),
                                             sheetsOpenOptions.isWithFormulaEvaluator()
                                     );
                                 } else {
                                     throw new IOException("No input source!!");
                                 }
                             }
                             return usage.apply(keelSheets)
                                         .andThen(ar -> {
                                             Promise<Void> promise = Promise.promise();
                                             keelSheets.close(promise);
                                         });
                         } catch (IOException e) {
                             return Future.failedFuture(e);
                         }
                     });
    }

    /**
     * @since 4.0.2
     */
    public static <T> Future<T> useSheets(@Nonnull SheetsCreateOptions sheetsCreateOptions,
                                          @Nonnull Function<KeelSheets, Future<T>> usage) {
        return Future.succeededFuture()
                     .compose(v -> {
                         KeelSheets keelSheets;
                         if (sheetsCreateOptions.isUseXlsx()) {
                             keelSheets = KeelSheets.autoGenerateXLSX(sheetsCreateOptions.isWithFormulaEvaluator());
                             if (sheetsCreateOptions.isUseStreamWriting()) {
                                 keelSheets.useStreamWrite();
                             }
                         } else {
                             keelSheets = KeelSheets.autoGenerateXLS(sheetsCreateOptions.isWithFormulaEvaluator());
                         }

                         return usage.apply(keelSheets)
                                     .andThen(ar -> {
                                         Promise<Void> promise = Promise.promise();
                                         keelSheets.close(promise);
                                     });
                     });
    }

    /**
     * @since 3.0.20 The great DAN and HONG discovered an issue with POI Factory Mode.
     */
    @Deprecated(since = "4.0.2", forRemoval = true)
    public static KeelSheets autoGenerate(@Nonnull InputStream inputStream) {
        return autoGenerate(inputStream, false);
    }

    /**
     * @since 3.1.4
     */
    @Deprecated(since = "4.0.2", forRemoval = true)
    public static KeelSheets autoGenerate(@Nonnull InputStream inputStream, boolean withFormulaEvaluator) {
        Workbook workbook;
        try {
            // XLSX
            workbook = new XSSFWorkbook(inputStream);
        } catch (IOException e) {
            try {
                // XLS
                workbook = new HSSFWorkbook(inputStream);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return new KeelSheets(workbook, withFormulaEvaluator);
    }

    /**
     * @since 3.1.1
     */
    @Deprecated(since = "4.0.2", forRemoval = true)
    public static KeelSheets autoGenerateXLSX() {
        return new KeelSheets(new XSSFWorkbook());
    }

    /**
     * @since 3.1.4
     */
    @Deprecated(since = "4.0.2", forRemoval = true)
    public static KeelSheets autoGenerateXLSX(boolean withFormulaEvaluator) {
        return new KeelSheets(new XSSFWorkbook(), withFormulaEvaluator);
    }

    /**
     * @since 3.1.1
     */
    @Deprecated(since = "4.0.2", forRemoval = true)
    public static KeelSheets autoGenerateXLS() {
        return new KeelSheets(new HSSFWorkbook());
    }

    /**
     * @since 3.1.4
     */
    @Deprecated(since = "4.0.2", forRemoval = true)
    public static KeelSheets autoGenerateXLS(boolean withFormulaEvaluator) {
        return new KeelSheets(new HSSFWorkbook(), withFormulaEvaluator);
    }

    /**
     * @since 4.0.2 became private
     */
    private KeelSheets useStreamWrite() {
        if (autoWorkbook instanceof XSSFWorkbook) {
            autoWorkbook = new SXSSFWorkbook((XSSFWorkbook) autoWorkbook);
        } else {
            throw new IllegalStateException("Now autoWorkbook is not an instance of XSSFWorkbook.");
        }
        return this;
    }

    public KeelSheet generateReaderForSheet(@Nonnull String sheetName) {
        return this.generateReaderForSheet(sheetName, true);
    }

    /**
     * @since 3.1.4
     */
    public KeelSheet generateReaderForSheet(@Nonnull String sheetName, boolean parseFormulaCellToValue) {
        var sheet = this.getWorkbook().getSheet(sheetName);
        ValueBox<FormulaEvaluator> formulaEvaluatorValueBox = new ValueBox<>();
        if (parseFormulaCellToValue) {
            formulaEvaluatorValueBox.setValue(this.formulaEvaluator);
        }
        return new KeelSheet(sheet, formulaEvaluatorValueBox);
    }

    public KeelSheet generateReaderForSheet(int sheetIndex) {
        return this.generateReaderForSheet(sheetIndex, true);
    }

    /**
     * @since 3.1.4
     */
    public KeelSheet generateReaderForSheet(int sheetIndex, boolean parseFormulaCellToValue) {
        var sheet = this.getWorkbook().getSheetAt(sheetIndex);
        ValueBox<FormulaEvaluator> formulaEvaluatorValueBox = new ValueBox<>();
        if (parseFormulaCellToValue) {
            formulaEvaluatorValueBox.setValue(this.formulaEvaluator);
        }
        return new KeelSheet(sheet, formulaEvaluatorValueBox);
    }

    public KeelSheet generateWriterForSheet(@Nonnull String sheetName, Integer pos) {
        Sheet sheet = this.getWorkbook().createSheet(sheetName);
        if (pos != null) {
            this.getWorkbook().setSheetOrder(sheetName, pos);
        }
        return new KeelSheet(sheet, new ValueBox<>(this.formulaEvaluator));
    }

    public KeelSheet generateWriterForSheet(@Nonnull String sheetName) {
        return generateWriterForSheet(sheetName, null);
    }

    public int getSheetCount() {
        return autoWorkbook.getNumberOfSheets();
    }

    /**
     * @return Raw Apache POI Workbook instance.
     */
    @Nonnull
    public Workbook getWorkbook() {
        return autoWorkbook;
    }

    public void save(OutputStream outputStream) {
        try {
            autoWorkbook.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(File file) {
        try {
            save(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(String fileName) {
        save(new File(fileName));
    }

    /**
     * @param completion the promise to signal when close has completed
     * @since 4.0.2
     */
    @Override
    public void close(Promise<Void> completion) {
        try {
            autoWorkbook.close();
            completion.complete();
        } catch (IOException e) {
            completion.fail(e);
        }
    }
}
