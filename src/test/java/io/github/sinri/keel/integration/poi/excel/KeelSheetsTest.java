package io.github.sinri.keel.integration.poi.excel;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Iterator;

@ExtendWith({VertxExtension.class})
class KeelSheetsTest extends KeelJUnit5Test {

    @BeforeAll
    static void beforeAll(Vertx vertx, VertxTestContext testContext) {
        beforeAllShared(vertx);
        testContext.completeNow();
    }

    @Test
    void testReadXlsxWithPic0(Vertx vertx, VertxTestContext testContext) throws Exception {
        KeelSheets.useSheets(
                          new SheetsOpenOptions()
                                  .setFile("/Users/sinri/code/keel/src/test/resources/runtime/with-pic.xlsx"),
                          keelSheets -> {
                              Sheet sheet0 = keelSheets.getWorkbook().getSheetAt(0);
                              Iterator<Row> rowIterator = sheet0.rowIterator();
                              int rowIndex = 0;
                              while (rowIterator.hasNext()) {
                                  Row row = rowIterator.next();
                                  Iterator<Cell> cellIterator = row.cellIterator();
                                  int cellIndex = 0;
                                  while (cellIterator.hasNext()) {
                                      Cell cell = cellIterator.next();
                                      getUnitTestLogger().info(String.format(
                                              "cell[%d][%d]: %s as %s",
                                              rowIndex, cellIndex,
                                              cell, cell.getCellType().name()
                                      ));
                                      cellIndex++;
                                  }
                                  rowIndex++;
                              }

                              sheet0.getWorkbook().getAllPictures()
                                    .forEach(sheetPic -> {
                                        getUnitTestLogger().info(String.format(
                                                "pic <%s> type %s mime %s>",
                                                sheetPic.getClass().getName(),
                                                sheetPic.getPictureType(),
                                                sheetPic.getMimeType()
                                        ));

                                        //                                       org.apache.poi.xssf.usermodel.XSSFPictureData xssfPictureData= (org.apache.poi.xssf.usermodel.XSSFPictureData) sheetPic;
                                    });

                              // 获取工作表中的所有绘图对象（包含图片）
                              XSSFDrawing drawing = (XSSFDrawing) sheet0.getDrawingPatriarch();
                              if (drawing != null) {
                                  System.out.println("  发现绘图对象");

                                  int pictureIndex = 0; // 图片计数器
                                  // 遍历所有图形对象
                                  for (XSSFShape shape : drawing.getShapes()) {
                                      getUnitTestLogger().info("shape class: " + shape.getClass().getName());
                                      // 只处理图片类型的对象
                                      if (shape instanceof XSSFPicture) {
                                          XSSFPicture picture = (XSSFPicture) shape;
                                          XSSFPictureData pictureData = picture.getPictureData();

                                          // 获取图片位置信息 - POI 5.4.1兼容方式
                                          int row = 0;
                                          int col = 0;

                                          try {
                                              // 使用新的API获取位置信息
                                              if (picture.getClientAnchor() != null) {
                                                  var anchor = picture.getClientAnchor();
                                                  row = anchor.getRow1() + 1; // 转换为Excel行号（从1开始）
                                                  col = anchor.getCol1() + 1; // 转换为Excel列号（从1开始）
                                              } else {
                                                  // 如果无法获取位置信息，使用默认值
                                                  System.out.println("    警告：无法获取图片精确位置信息");
                                                  row = -1;
                                                  col = -1;
                                              }
                                          } catch (Exception e) {
                                              System.out.println("    警告：获取图片位置信息时出错: " + e.getMessage());
                                              row = -1;
                                              col = -1;
                                          }

                                          // 获取图片尺寸（像素）
                                          int width = 0;
                                          int height = 0;
                                          try {
                                              var dimension = picture.getImageDimension();
                                              width = dimension.width;
                                              height = dimension.height;
                                          } catch (Exception e) {
                                              System.out.println("    警告：无法获取图片尺寸信息: " + e.getMessage());
                                          }

                                          // 输出图片信息
                                          pictureIndex++;
                                          System.out.printf("  图片 %d: %s%n",
                                                  pictureIndex,
                                                  pictureData.suggestFileExtension());
                                          if (row > 0 && col > 0) {
                                              System.out.printf("    位置: 第%d行，第%d列%n", row, col);
                                          } else {
                                              System.out.printf("    位置: 无法确定%n");
                                          }
                                          if (width > 0 && height > 0) {
                                              System.out.printf("    尺寸: %dx%d像素%n", width, height);
                                          } else {
                                              System.out.printf("    尺寸: 无法确定%n");
                                          }
                                          System.out.printf("    MIME类型: %s%n", pictureData.getMimeType());
                                      }
                                  }
                              } else {
                                  System.out.println("  无图片");
                              }

                              return Future.succeededFuture();
                          }
                  )
                  .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    void testReadXlsWithPic0(Vertx vertx, VertxTestContext testContext) throws Exception {
        KeelSheets.useSheets(
                          new SheetsOpenOptions()
                                  .setFile("/Users/sinri/code/keel/src/test/resources/runtime/with-pic.xls"),
                          keelSheets -> {
                              Sheet sheet0 = keelSheets.getWorkbook().getSheetAt(0);
                              Iterator<Row> rowIterator = sheet0.rowIterator();
                              int rowIndex = 0;
                              while (rowIterator.hasNext()) {
                                  Row row = rowIterator.next();
                                  Iterator<Cell> cellIterator = row.cellIterator();
                                  int cellIndex = 0;
                                  while (cellIterator.hasNext()) {
                                      Cell cell = cellIterator.next();
                                      getUnitTestLogger().info(String.format(
                                              "cell[%d][%d]: %s as %s",
                                              rowIndex, cellIndex,
                                              cell, cell.getCellType().name()
                                      ));
                                      cellIndex++;
                                  }
                                  rowIndex++;
                              }

                              sheet0.getWorkbook().getAllPictures()
                                    .forEach(sheetPic -> {
                                        getUnitTestLogger().info(String.format(
                                                "pic <%s> type %s mime %s>",
                                                sheetPic.getClass().getName(),
                                                sheetPic.getPictureType(),
                                                sheetPic.getMimeType()
                                        ));
                                    });

                              // 获取工作表中的所有绘图对象（包含图片）
                              Drawing<?> drawingPatriarch = sheet0.getDrawingPatriarch();
                              getUnitTestLogger().info("drawingPatriarch class: " + drawingPatriarch.getClass().getName());

                              if (drawingPatriarch instanceof HSSFPatriarch) {
                                  HSSFPatriarch drawing = (HSSFPatriarch) drawingPatriarch;
                                  System.out.println("  发现HSSF绘图对象");

                                  int pictureIndex = 0; // 图片计数器
                                  // 遍历所有图形对象 - 使用HSSF兼容的方式
                                  for (org.apache.poi.hssf.usermodel.HSSFShape shape : drawing.getChildren()) {
                                      getUnitTestLogger().info("shape class: " + shape.getClass().getName());
                                      // 只处理图片类型的对象
                                      if (shape instanceof org.apache.poi.hssf.usermodel.HSSFPicture) {
                                          org.apache.poi.hssf.usermodel.HSSFPicture picture = (org.apache.poi.hssf.usermodel.HSSFPicture) shape;
                                          org.apache.poi.hssf.usermodel.HSSFPictureData pictureData = picture.getPictureData();

                                          // 获取图片位置信息 - POI 5.4.1兼容方式
                                          int row = 0;
                                          int col = 0;

                                          try {
                                              // 使用HSSF的ClientAnchor获取位置信息
                                              if (picture.getClientAnchor() != null) {
                                                  var anchor = picture.getClientAnchor();
                                                  row = anchor.getRow1() + 1; // 转换为Excel行号（从1开始）
                                                  col = anchor.getCol1() + 1; // 转换为Excel列号（从1开始）
                                              } else {
                                                  // 如果无法获取位置信息，使用默认值
                                                  System.out.println("    警告：无法获取图片精确位置信息");
                                                  row = -1;
                                                  col = -1;
                                              }
                                          } catch (Exception e) {
                                              System.out.println("    警告：获取图片位置信息时出错: " + e.getMessage());
                                              row = -1;
                                              col = -1;
                                          }

                                          // 获取图片尺寸（像素）
                                          int width = 0;
                                          int height = 0;
                                          try {
                                              // HSSF中获取图片尺寸的方法
                                              var anchor = picture.getClientAnchor();
                                              if (anchor != null) {
                                                  // 使用anchor的尺寸信息 - POI 5.4.1兼容方式
                                                  width = anchor.getDx1();
                                                  height = anchor.getDy1();
                                                  // 转换为像素（近似值）
                                                  width = Math.max(1, width / 9525); // 9525 EMUs per pixel
                                                  height = Math.max(1, height / 9525);
                                              }
                                          } catch (Exception e) {
                                              System.out.println("    警告：无法获取图片尺寸信息: " + e.getMessage());
                                          }

                                          // 输出图片信息
                                          pictureIndex++;
                                          System.out.printf("  图片 %d: %s%n",
                                                  pictureIndex,
                                                  pictureData.suggestFileExtension());
                                          if (row > 0 && col > 0) {
                                              System.out.printf("    位置: 第%d行，第%d列%n", row, col);
                                          } else {
                                              System.out.printf("    位置: 无法确定%n");
                                          }
                                          if (width > 0 && height > 0) {
                                              System.out.printf("    尺寸: %dx%d像素%n", width, height);
                                          } else {
                                              System.out.printf("    尺寸: 无法确定%n");
                                          }
                                          System.out.printf("    MIME类型: %s%n", pictureData.getMimeType());
                                      }
                                  }
                              } else {
                                  System.out.println("  无HSSF绘图对象");
                              }

                              return Future.succeededFuture();
                          }
                  )
                  .onComplete(testContext.succeedingThenComplete());
    }

    @Test
    void testReadXlsxWithPic1(Vertx vertx, VertxTestContext testContext) throws Exception {
        KeelSheets.useSheets(
                          new SheetsOpenOptions()
                                  .setFile("/Users/sinri/code/keel/src/test/resources/runtime/with-pic.xlsx"),
                          keelSheets -> {
                              var sheet = keelSheets.generateReaderForSheet(0);

                              sheet.blockReadAllRows(row -> {
                                  int rowNum = row.getRowNum();
                                  getUnitTestLogger().info("==== rowNum: " + rowNum);
                                  // 修复：遍历当前行的所有单元格，而不是从0到rowNum
                                  int lastCellNum = row.getLastCellNum();
                                  for (int i = 0; i < lastCellNum; i++) {
                                      Cell cell = row.getCell(i);
                                      if (cell != null) {
                                          getUnitTestLogger().info("row[" + rowNum + "][cell" + i + "]=" + cell + " as " + cell.getCellType()
                                                                                                                               .name());
                                      }
                                  }
                              });
                              return Future.succeededFuture();
                          }
                  )
                  .onComplete(testContext.succeedingThenComplete());
    }
}