package io.github.sinri.keel.integration.poi.csv;

import io.vertx.core.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * As of 4.1.1, implements {@link Closeable}, and deprecates all the asynchronous methods.
 * <p>
 *     TODO: implement {@link Iterator} in the future, and remove all the asynchronous methods.
 *
 * @since 3.1.1
 */
public class KeelCsvReader implements Closeable {
    private final BufferedReader br;
    /**
     * TODO: make it final.
     */
    private String separator;

    /**
     * @since 4.1.1
     */
    public KeelCsvReader(@Nonnull BufferedReader br, @Nonnull String separator) {
        this.br = br;
        this.separator = separator;
    }

    public KeelCsvReader(@Nonnull InputStream inputStream, Charset charset) {
        this(inputStream, charset, ",");
    }

    /**
     * @since 4.1.1
     */
    public KeelCsvReader(@Nonnull InputStream inputStream, Charset charset, @Nonnull String separator) {
        this(new BufferedReader(new InputStreamReader(inputStream, charset)), separator);
    }

    public KeelCsvReader(@Nonnull BufferedReader br) {
        this(br, ",");
    }

    /**
     * @deprecated it is not necessary to be asynchronous, use the constructor of {@link KeelCsvReader} directly.
     */
    @Deprecated(since = "4.1.1", forRemoval = true)
    public static Future<KeelCsvReader> create(@Nonnull InputStream inputStream, @Nonnull Charset charset) {
        var x = new KeelCsvReader(inputStream, charset);
        return Future.succeededFuture(x);
    }

    /**
     * @deprecated it is not necessary to be asynchronous, use the constructor of {@link KeelCsvReader} directly.
     */
    @Deprecated(since = "4.1.1", forRemoval = true)
    public static Future<KeelCsvReader> create(@Nonnull File file, @Nonnull Charset charset) {
        return Future.succeededFuture()
                     .compose(v -> {
                         try {
                             var fis = new FileInputStream(file);
                             return create(fis, charset);
                         } catch (IOException e) {
                             return Future.failedFuture(e);
                         }
                     });

    }

    /**
     * @deprecated it is not necessary to be asynchronous, use the constructor of {@link KeelCsvReader} directly.
     */
    @Deprecated(since = "4.1.1", forRemoval = true)
    public static Future<KeelCsvReader> create(@Nonnull String file, @Nonnull Charset charset) {
        return create(new File(file), charset);
    }

    /**
     * Set the separator.
     * <p>
     * The separator is expected to be set in the constructor now.
     *
     * @deprecated This method should be called before any read action.
     */
    @Deprecated(since = "4.1.1", forRemoval = true)
    public KeelCsvReader setSeparator(String separator) {
        this.separator = separator;
        return this;
    }

    /**
     * @return the next row parsed from csv source, or null if no any more rows there.
     * @throws IOException if any IO exceptions occur about the csv source
     */
    @Nullable
    public CsvRow next() throws IOException {
        String line = br.readLine();
        if (line == null) return null;
        return consumeOneLine(null, null, 0, line);
    }

    /**
     * Reads one complete CSV row from the input stream in a blocking manner.
     * <p>
     * This method parses CSV data according to standard CSV rules:
     * <p>
     * - Fields are separated by the configured separator (default: comma)
     * <p>
     * - Fields containing the separator or newlines must be enclosed in double quotes
     * <p>
     * - Double quotes within quoted fields are escaped by doubling them
     * <p>
     * - A single row may span multiple lines if it contains quoted fields with newlines
     * <p>
     * The method returns null when the end of the input stream is reached.
     *
     * @return a CsvRow containing the parsed cells, or null if no more rows are available
     * @throws IOException if an I/O error occurs while reading from the input stream
     * @deprecated use {@link KeelCsvReader#next()} instead.
     */
    @Deprecated(since = "4.1.1", forRemoval = true)
    public @Nullable CsvRow blockReadRow() throws IOException {
        return next();
    }

    /**
     * @param row        the uncompleted row instance, if existed
     * @param buffer     the cell content buffer, if existed
     * @param quoterFlag Three options: 0,1,2
     *                   <p> + aaa,bbb
     *                   <p> - 0000000
     *                   <p> + ,"aa""bb",
     *                   <p> - 0111211122
     * @param line       the raw text of the line
     * @return the parsed row instance; may be incompleted during recursion.
     */
    private CsvRow consumeOneLine(@Nullable CsvRow row, @Nullable StringBuilder buffer, int quoterFlag, @Nonnull String line) throws IOException {
        if (row == null) {
            row = new CsvRow();
        }
        if (buffer == null) {
            buffer = new StringBuilder();
        } else {
            buffer.append("\n");
        }

        for (int i = 0; i < line.length(); i++) {
            var singleString = line.substring(i, i + 1);
            if (singleString.equals("\"")) {
                if (quoterFlag == 0) {
                    quoterFlag = 1;
                } else if (quoterFlag == 1) {
                    quoterFlag = 2;
                } else {
                    buffer.append(singleString);
                    quoterFlag = 1;
                }
            } else if (singleString.equals(separator)) {
                if (quoterFlag == 0 || quoterFlag == 2) {
                    // buffer to cell
                    row.addCell(new CsvCell(buffer.toString()));
                    quoterFlag = 0;
                    buffer = new StringBuilder();
                } else {
                    buffer.append(singleString);
                }
            } else {
                buffer.append(singleString);
            }
        }

        // now this line ends
        if (quoterFlag == 0 || quoterFlag == 2) {
            // now the row ends within this line
            row.addCell(new CsvCell(buffer.toString()));
            return row;
        } else {
            // this row seems to expend to the next line
            String nextLine = br.readLine();
            if (nextLine == null) {
                // strange: file ending without escape quote, for safety, escape it:
                // let us handle it as quoterFlag is 2
                row.addCell(new CsvCell(buffer.toString()));
                return row;
            }
            return consumeOneLine(row, buffer, quoterFlag, nextLine);
        }
    }

    /**
     * @deprecated use {@link KeelCsvReader#blockReadRow()} directly.
     */
    @Deprecated(since = "4.1.1", forRemoval = true)
    public Future<CsvRow> readRow() {
        return Future.succeededFuture()
                     .compose(v -> {
                         try {
                             var row = this.blockReadRow();
                             return Future.succeededFuture(row);
                         } catch (IOException e) {
                             return Future.failedFuture(e);
                         }
                     });
    }

    @Override
    public void close() throws IOException {
        this.br.close();
    }
}
