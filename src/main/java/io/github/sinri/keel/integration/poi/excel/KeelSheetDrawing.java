package io.github.sinri.keel.integration.poi.excel;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.core.ValueBox;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFShape;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @since 4.1.1
 */
@TechnicalPreview(since = "4.1.1")
public class KeelSheetDrawing {
    @Nonnull
    private final ValueBox<XSSFDrawing> drawingForXlsxValueBox = new ValueBox<>();
    @Nonnull
    private final ValueBox<HSSFPatriarch> drawingForXlsValueBox = new ValueBox<>();


    public KeelSheetDrawing(@Nonnull KeelSheet keelSheet) {
        drawingForXlsxValueBox.setValue(null);
        drawingForXlsValueBox.setValue(null);
        if (keelSheet.getSheetsReaderType() == KeelSheetsReaderType.XLSX) {
            var x = keelSheet.getSheet().getDrawingPatriarch();
            if (x == null) {
                drawingForXlsxValueBox.setValue(null);
            } else {
                drawingForXlsxValueBox.setValue((XSSFDrawing) x);
            }
        } else if (keelSheet.getSheetsReaderType() == KeelSheetsReaderType.XLS) {
            var x = keelSheet.getSheet().getDrawingPatriarch();
            if (x == null) {
                drawingForXlsValueBox.setValue(null);
            } else {
                drawingForXlsValueBox.setValue((HSSFPatriarch) x);
            }
        }
    }

    public List<KeelSheetsPicture> getPictures() {
        List<KeelSheetsPicture> list = new ArrayList<>();

        List<XSSFPicture> picturesForXlsx = getPicturesForXlsx();
        if (picturesForXlsx != null) {
            picturesForXlsx.forEach(picture -> {
                list.add(new KeelSheetsPicture(picture));
            });
        }

        List<HSSFPicture> picturesForXls = getPicturesForXls();
        if (picturesForXls != null) {
            picturesForXls.forEach(picture -> {
                list.add(new KeelSheetsPicture(picture));
            });
        }

        return list;
    }

    @Nullable
    private XSSFDrawing getDrawingForXlsx() {
        return drawingForXlsxValueBox.getValue();
    }

    @Nullable
    private List<XSSFShape> getShapesForXlsx() {
        XSSFDrawing xlsxDrawing = getDrawingForXlsx();
        if (xlsxDrawing == null) return null;
        return xlsxDrawing.getShapes();
    }

    @Nullable
    private List<XSSFPicture> getPicturesForXlsx() {
        List<XSSFShape> xlsxShapes = getShapesForXlsx();
        if (xlsxShapes == null) return null;
        return xlsxShapes.stream()
                         .filter(shape -> shape instanceof XSSFPicture)
                         .map(x -> (XSSFPicture) x)
                         .collect(Collectors.toList());
    }

    @Nullable
    private HSSFPatriarch getDrawingForXls() {
        return drawingForXlsValueBox.getValue();
    }

    @Nullable
    private List<org.apache.poi.hssf.usermodel.HSSFShape> getShapesForXls() {
        HSSFPatriarch drawing = getDrawingForXls();
        if (drawing == null) return null;
        return drawing.getChildren();
    }

    @Nullable
    private List<HSSFPicture> getPicturesForXls() {
        List<HSSFShape> shapesForXls = getShapesForXls();
        if (shapesForXls == null) return null;
        return shapesForXls.stream()
                           .filter(x -> x instanceof HSSFPicture)
                           .map(x -> (HSSFPicture) x)
                           .collect(Collectors.toList());
    }

    public static class KeelSheetsPicture {
        private final int atRow;
        private final int atCol;
        private final int width;
        private final int height;
        private final String suggestFileExtension;
        private final String mimeType;

        public KeelSheetsPicture(XSSFPicture xssfPicture) {
            if (xssfPicture.getClientAnchor() != null) {
                var anchor = xssfPicture.getClientAnchor();
                atRow = anchor.getRow1() + 1; // 转换为Excel行号（从1开始）
                atCol = anchor.getCol1() + 1; // 转换为Excel列号（从1开始）
            } else {
                atRow = -1;
                atCol = -1;
            }

            var dimension = xssfPicture.getImageDimension();
            width = dimension.width;
            height = dimension.height;

            this.suggestFileExtension = xssfPicture.getPictureData().suggestFileExtension();
            this.mimeType = xssfPicture.getPictureData().getMimeType();
        }

        public KeelSheetsPicture(HSSFPicture hssfPicture) {
            HSSFClientAnchor anchor = hssfPicture.getClientAnchor();
            if (anchor != null) {
                atRow = anchor.getRow1() + 1; // 转换为Excel行号（从1开始）
                atCol = anchor.getCol1() + 1; // 转换为Excel列号（从1开始）

                // 使用anchor的尺寸信息 - POI 5.4.1兼容方式
                // 转换为像素（近似值）
                width = Math.max(1, anchor.getDx1() / 9525); // 9525 EMUs per pixel
                height = Math.max(1, anchor.getDy1() / 9525);
            } else {
                atRow = -1;
                atCol = -1;

                width = -1;
                height = -1;
            }

            this.suggestFileExtension = hssfPicture.getPictureData().suggestFileExtension();
            this.mimeType = hssfPicture.getPictureData().getMimeType();
        }

        public int getAtRow() {
            return atRow;
        }

        public int getAtCol() {
            return atCol;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public String getSuggestFileExtension() {
            return suggestFileExtension;
        }

        public String getMimeType() {
            return mimeType;
        }
    }
}
