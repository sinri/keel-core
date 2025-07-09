package io.github.sinri.keel.core.helper;

import io.github.sinri.keel.core.cron.KeelCronExpression;
import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class KeelDateTimeHelperTest extends KeelUnitTest {

    private KeelDateTimeHelper helper;

    @BeforeEach
    public void setUp() {
        helper = KeelDateTimeHelper.getInstance();
    }

    @Test
    void testGetCurrentDatetime() {
        String currentDatetime = helper.getCurrentDatetime();
        assertNotNull(currentDatetime);
        assertTrue(currentDatetime.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
        getUnitTestLogger().info("Current datetime: " + currentDatetime);
    }

    @Test
    void testGetCurrentDate() {
        String currentDate = helper.getCurrentDate();
        assertNotNull(currentDate);
        assertTrue(currentDate.matches("\\d{4}-\\d{2}-\\d{2}"));
        getUnitTestLogger().info("Current date: " + currentDate);
    }

    @Test
    void testGetCurrentDateExpression() {
        // Test with MySQL datetime pattern
        String datetimeExpression = helper.getCurrentDateExpression(KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
        assertNotNull(datetimeExpression);
        assertTrue(datetimeExpression.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));

        // Test with MySQL date pattern
        String dateExpression = helper.getCurrentDateExpression(KeelDateTimeHelper.MYSQL_DATE_PATTERN);
        assertNotNull(dateExpression);
        assertTrue(dateExpression.matches("\\d{4}-\\d{2}-\\d{2}"));

        // Test with custom format
        String customExpression = helper.getCurrentDateExpression("yyyyMMdd");
        assertNotNull(customExpression);
        assertTrue(customExpression.matches("\\d{8}"));

        // Test with null format
        String nullExpression = helper.getCurrentDateExpression(null);
        assertNull(nullExpression);

        // Test with empty format
        String emptyExpression = helper.getCurrentDateExpression("");
        assertNull(emptyExpression);

        getUnitTestLogger().info("Datetime expression: " + datetimeExpression);
        getUnitTestLogger().info("Date expression: " + dateExpression);
        getUnitTestLogger().info("Custom expression: " + customExpression);
    }

    @Test
    void testGetDateExpressionWithDate() {
        Date testDate = new Date(1640995200000L); // 2022-01-01 00:00:00 UTC
        
        // Test with MySQL datetime pattern - 注意时区转换
        String datetimeExpression = helper.getDateExpression(testDate, KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
        // 由于 SimpleDateFormat 使用系统默认时区，需要根据实际时区调整期望值
        assertTrue(datetimeExpression.matches("2022-01-01 \\d{2}:\\d{2}:\\d{2}"));

        // Test with MySQL date pattern
        String dateExpression = helper.getDateExpression(testDate, KeelDateTimeHelper.MYSQL_DATE_PATTERN);
        assertEquals("2022-01-01", dateExpression);

        // Test with custom format
        String customExpression = helper.getDateExpression(testDate, "yyyyMMdd");
        assertEquals("20220101", customExpression);

        // Test with null format
        String nullExpression = helper.getDateExpression(testDate, null);
        assertNull(nullExpression);

        // Test with empty format
        String emptyExpression = helper.getDateExpression(testDate, "");
        assertNull(emptyExpression);

        getUnitTestLogger().info("Date expression with Date: " + datetimeExpression);
    }

    @Test
    void testGetDateExpressionWithTimestamp() {
        long timestamp = 1640995200000L; // 2022-01-01 00:00:00 UTC
        
        // Test with MySQL datetime pattern - 注意时区转换
        String datetimeExpression = helper.getDateExpression(timestamp, KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
        // 由于 SimpleDateFormat 使用系统默认时区，需要根据实际时区调整期望值
        assertTrue(datetimeExpression.matches("2022-01-01 \\d{2}:\\d{2}:\\d{2}"));

        // Test with MySQL date pattern
        String dateExpression = helper.getDateExpression(timestamp, KeelDateTimeHelper.MYSQL_DATE_PATTERN);
        assertEquals("2022-01-01", dateExpression);

        // Test with custom format
        String customExpression = helper.getDateExpression(timestamp, "yyyyMMdd");
        assertEquals("20220101", customExpression);

        getUnitTestLogger().info("Date expression with timestamp: " + datetimeExpression);
    }

    @Test
    void testGetMySQLFormatLocalDateTimeExpression() {
        String localDateTimeExpression = "2022-01-01T12:30:45";
        String mysqlFormat = helper.getMySQLFormatLocalDateTimeExpression(localDateTimeExpression);
        assertEquals("2022-01-01 12:30:45", mysqlFormat);

        getUnitTestLogger().info("MySQL format: " + mysqlFormat);
    }

    @Test
    void testGetGMTDateTimeExpressionWithZoneId() {
        ZoneId utcZone = ZoneOffset.UTC;
        String gmtExpression = helper.getGMTDateTimeExpression(utcZone);
        assertNotNull(gmtExpression);
        assertTrue(gmtExpression.matches("[A-Za-z]{3}, \\d{2} [A-Za-z]{3} \\d{4} \\d{2}:\\d{2}:\\d{2} GMT"));

        ZoneId systemZone = ZoneId.systemDefault();
        String systemExpression = helper.getGMTDateTimeExpression(systemZone);
        assertNotNull(systemExpression);
        assertTrue(systemExpression.matches("[A-Za-z]{3}, \\d{2} [A-Za-z]{3} \\d{4} \\d{2}:\\d{2}:\\d{2} GMT"));

        getUnitTestLogger().info("GMT expression (UTC): " + gmtExpression);
        getUnitTestLogger().info("GMT expression (System): " + systemExpression);
    }

    @Test
    void testGetGMTDateTimeExpression() {
        String gmtExpression = helper.getGMTDateTimeExpression();
        assertNotNull(gmtExpression);
        assertTrue(gmtExpression.matches("[A-Za-z]{3}, \\d{2} [A-Za-z]{3} \\d{4} \\d{2}:\\d{2}:\\d{2} GMT"));

        getUnitTestLogger().info("GMT expression: " + gmtExpression);
    }

    @Test
    void testMakeStandardWidthField() {
        // Test with single digit and width 3
        String result1 = helper.makeStandardWidthField(5, 3);
        assertEquals("005", result1);

        // Test with double digit and width 3
        String result2 = helper.makeStandardWidthField(12, 3);
        assertEquals("012", result2);

        // Test with exact width
        String result3 = helper.makeStandardWidthField(123, 3);
        assertEquals("123", result3);

        // Test with larger number - should throw exception when width is insufficient
        assertThrows(IllegalArgumentException.class, () -> {
            helper.makeStandardWidthField(1234, 3);
        });

        // Test with single digit and width 2
        String result5 = helper.makeStandardWidthField(5, 2);
        assertEquals("05", result5);

        // Test with zero
        String result6 = helper.makeStandardWidthField(0, 3);
        assertEquals("000", result6);

        // Test with single digit and width 1
        String result7 = helper.makeStandardWidthField(5, 1);
        assertEquals("5", result7);

        // Test with negative number - should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            helper.makeStandardWidthField(-5, 3);
        });

        getUnitTestLogger().info("Standard width field tests: " + result1 + ", " + result2 + ", " + result3 + ", " + result5 + ", " + result6 + ", " + result7);
    }

    @Test
    void testToMySQLDatetime() {
        LocalDateTime testDateTime = LocalDateTime.of(2022, 1, 1, 12, 30, 45);
        String mysqlDatetime = helper.toMySQLDatetime(testDateTime);
        assertEquals("2022-01-01 12:30:45", mysqlDatetime);

        // Test with single digit values
        LocalDateTime singleDigitDateTime = LocalDateTime.of(2022, 1, 1, 1, 2, 3);
        String singleDigitMysqlDatetime = helper.toMySQLDatetime(singleDigitDateTime);
        assertEquals("2022-01-01 01:02:03", singleDigitMysqlDatetime);

        getUnitTestLogger().info("MySQL datetime: " + mysqlDatetime);
        getUnitTestLogger().info("Single digit MySQL datetime: " + singleDigitMysqlDatetime);
    }

    @Test
    void testToMySQLDatetimeWithFormatPattern() {
        LocalDateTime testDateTime = LocalDateTime.of(2022, 1, 1, 12, 30, 45);
        
        // Test with MySQL datetime pattern
        String datetimeResult = helper.toMySQLDatetime(testDateTime, KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
        assertEquals("2022-01-01 12:30:45", datetimeResult);

        // Test with MySQL date pattern
        String dateResult = helper.toMySQLDatetime(testDateTime, KeelDateTimeHelper.MYSQL_DATE_PATTERN);
        assertEquals("2022-01-01", dateResult);

        // Test with MySQL time pattern
        String timeResult = helper.toMySQLDatetime(testDateTime, KeelDateTimeHelper.MYSQL_TIME_PATTERN);
        assertEquals("12:30:45", timeResult);

        // Test with custom pattern
        String customResult = helper.toMySQLDatetime(testDateTime, "yyyyMMddHHmmss");
        assertEquals("20220101123045", customResult);

        getUnitTestLogger().info("MySQL datetime with pattern: " + datetimeResult);
        getUnitTestLogger().info("MySQL date with pattern: " + dateResult);
        getUnitTestLogger().info("MySQL time with pattern: " + timeResult);
        getUnitTestLogger().info("Custom pattern: " + customResult);
    }

    @Test
    void testParseExpressionToDateInstance() {
        // Test valid date string
        String validDateStr = "2022-01-01 12:30:45";
        String validFormat = KeelDateTimeHelper.MYSQL_DATETIME_PATTERN;
        Date parsedDate = helper.parseExpressionToDateInstance(validDateStr, validFormat);
        assertNotNull(parsedDate);
        
        // Verify the parsed date
        String backToString = helper.getDateExpression(parsedDate, validFormat);
        assertEquals(validDateStr, backToString);

        // Test with date only
        String dateOnlyStr = "2022-01-01";
        String dateOnlyFormat = KeelDateTimeHelper.MYSQL_DATE_PATTERN;
        Date parsedDateOnly = helper.parseExpressionToDateInstance(dateOnlyStr, dateOnlyFormat);
        assertNotNull(parsedDateOnly);

        getUnitTestLogger().info("Parsed date: " + parsedDate);
        getUnitTestLogger().info("Parsed date only: " + parsedDateOnly);
    }

    @Test
    void testParseExpressionToDateInstanceWithInvalidInput() {
        // Test invalid date string
        String invalidDateStr = "invalid-date";
        String validFormat = KeelDateTimeHelper.MYSQL_DATETIME_PATTERN;
        
        assertThrows(IllegalArgumentException.class, () -> {
            helper.parseExpressionToDateInstance(invalidDateStr, validFormat);
        });

        // Test null date string
        assertThrows(IllegalArgumentException.class, () -> {
            helper.parseExpressionToDateInstance(null, validFormat);
        });

        // Test null format
        assertThrows(IllegalArgumentException.class, () -> {
            helper.parseExpressionToDateInstance("2022-01-01", null);
        });

        getUnitTestLogger().info("Invalid input tests passed");
    }

    @Test
    void testGetInstantExpression() {
        Instant testInstant = Instant.ofEpochSecond(1640995200L); // 2022-01-01 00:00:00 UTC
        ZoneId utcZone = ZoneOffset.UTC;
        
        // Test with MySQL datetime pattern
        String datetimeExpression = helper.getInstantExpression(testInstant, utcZone, KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
        assertEquals("2022-01-01 00:00:00", datetimeExpression);

        // Test with custom pattern
        String customExpression = helper.getInstantExpression(testInstant, utcZone, "yyyyMMdd");
        assertEquals("20220101", customExpression);

        // Test with different timezone
        ZoneId tokyoZone = ZoneId.of("Asia/Tokyo");
        String tokyoExpression = helper.getInstantExpression(testInstant, tokyoZone, KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
        assertNotNull(tokyoExpression);
        assertTrue(tokyoExpression.matches("2022-01-01 \\d{2}:\\d{2}:\\d{2}"));

        getUnitTestLogger().info("Instant expression (UTC): " + datetimeExpression);
        getUnitTestLogger().info("Instant expression (Tokyo): " + tokyoExpression);
    }

    @Test
    void testIsNowMatchCronExpression() {
        // Test with simple cron expression (every minute)
        boolean everyMinuteMatches = helper.isNowMatchCronExpression("* * * * *");
        assertTrue(everyMinuteMatches);

        // Test with specific time that should not match (next hour)
        Calendar now = Calendar.getInstance();
        int nextHour = (now.get(Calendar.HOUR_OF_DAY) + 1) % 24;
        String nextHourCron = String.format("%d %d * * *",
                now.get(Calendar.MINUTE),
                nextHour);
        
        boolean nextHourMatches = helper.isNowMatchCronExpression(nextHourCron);
        assertFalse(nextHourMatches);

        // Test with wildcard cron expression
        boolean wildcardMatches = helper.isNowMatchCronExpression("* * * * *");
        assertTrue(wildcardMatches);

        // Test with invalid cron expression
        assertThrows(IllegalArgumentException.class, () -> {
            helper.isNowMatchCronExpression("invalid-cron");
        });

        getUnitTestLogger().info("Every minute matches: " + everyMinuteMatches);
        getUnitTestLogger().info("Next hour matches: " + nextHourMatches);
        getUnitTestLogger().info("Wildcard matches: " + wildcardMatches);
    }

    @Test
    void testIsNowMatchCronExpressionWithKeelCronExpression() {
        // Test with KeelCronExpression object
        KeelCronExpression cronExpression = new KeelCronExpression("* * * * *");
        boolean matches = helper.isNowMatchCronExpression(cronExpression);
        assertTrue(matches);

        // Test with specific time cron expression
        Calendar now = Calendar.getInstance();
        String specificCron = String.format("%d %d %d %d %d",
                now.get(Calendar.MINUTE),
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.DAY_OF_MONTH),
                now.get(Calendar.MONTH) + 1,
                now.get(Calendar.DAY_OF_WEEK) - 1);
        
        KeelCronExpression specificExpression = new KeelCronExpression(specificCron);
        boolean specificMatches = helper.isNowMatchCronExpression(specificExpression);
        assertTrue(specificMatches);

        getUnitTestLogger().info("KeelCronExpression matches: " + matches);
        getUnitTestLogger().info("Specific KeelCronExpression matches: " + specificMatches);
    }

    @Test
    void testConstants() {
        // Test that constants are properly defined
        assertEquals("yyyy-MM-dd HH:mm:ss.SSS", KeelDateTimeHelper.MYSQL_DATETIME_MS_PATTERN);
        assertEquals("yyyy-MM-dd HH:mm:ss", KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
        assertEquals("yyyy-MM-dd", KeelDateTimeHelper.MYSQL_DATE_PATTERN);
        assertEquals("HH:mm:ss", KeelDateTimeHelper.MYSQL_TIME_PATTERN);
        assertEquals("EEE, dd MMM yyyy HH:mm:ss z", KeelDateTimeHelper.GMT_PATTERN);
        assertEquals("yyyy-MM-dd'T'HH:mm:ss'Z'", KeelDateTimeHelper.ISO8601_PATTERN);

        getUnitTestLogger().info("All constants are properly defined");
    }

    @Test
    void testSingletonPattern() {
        // Test that getInstance() returns the same instance
        KeelDateTimeHelper instance1 = KeelDateTimeHelper.getInstance();
        KeelDateTimeHelper instance2 = KeelDateTimeHelper.getInstance();
        assertSame(instance1, instance2);

        getUnitTestLogger().info("Singleton pattern works correctly");
    }

    @Test
    void testParseExpressionToDateInstanceWithEdgeCases() {
        // Test with leap day
        String leapDayStr = "2020-02-29 00:00:00";
        Date leapDay = helper.parseExpressionToDateInstance(leapDayStr, KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
        assertNotNull(leapDay);
        
        // Test with invalid leap day (non-leap year)
        String invalidLeapDayStr = "2019-02-40 00:00:00";
        assertThrows(IllegalArgumentException.class, () -> {
            var x=helper.parseExpressionToDateInstance(invalidLeapDayStr, KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
            getUnitTestLogger().info(invalidLeapDayStr+" parsed as "+x.toString());
        });

        // Test with time at end of day
        String endOfDayStr = "2022-01-01 23:59:59";
        Date endOfDay = helper.parseExpressionToDateInstance(endOfDayStr, KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
        assertNotNull(endOfDay);

        getUnitTestLogger().info("Leap day parsed: " + leapDay);
        getUnitTestLogger().info("End of day parsed: " + endOfDay);
    }

    @Test
    void testParseExpressionToDateInstance_ValidDate() {
        KeelDateTimeHelper helper = KeelDateTimeHelper.getInstance();
        
        // 测试有效日期
        Date result = helper.parseExpressionToDateInstance("2023-12-25", KeelDateTimeHelper.MYSQL_DATE_PATTERN);
        assertNotNull(result);
        
        // 测试有效日期时间
        Date result2 = helper.parseExpressionToDateInstance("2023-12-25 14:30:00", KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
        assertNotNull(result2);
    }

    @Test
    void testParseExpressionToDateInstance_InvalidDate() {
        KeelDateTimeHelper helper = KeelDateTimeHelper.getInstance();
        
        // 测试无效日期 - 2月30日不存在
        assertThrows(IllegalArgumentException.class, () -> {
            helper.parseExpressionToDateInstance("2023-02-30", KeelDateTimeHelper.MYSQL_DATE_PATTERN);
        });
        
        // 测试无效日期 - 4月31日不存在
        assertThrows(IllegalArgumentException.class, () -> {
            helper.parseExpressionToDateInstance("2023-04-31", KeelDateTimeHelper.MYSQL_DATE_PATTERN);
        });
        
        // 测试无效时间 - 25小时不存在
        assertThrows(IllegalArgumentException.class, () -> {
            helper.parseExpressionToDateInstance("2023-12-25 25:00:00", KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
        });
        
        // 测试无效时间 - 60分钟不存在
        assertThrows(IllegalArgumentException.class, () -> {
            helper.parseExpressionToDateInstance("2023-12-25 14:60:00", KeelDateTimeHelper.MYSQL_DATETIME_PATTERN);
        });
    }

    @Test
    void testParseExpressionToDateInstance_InvalidFormat() {
        KeelDateTimeHelper helper = KeelDateTimeHelper.getInstance();
        
        // 测试格式不匹配
        assertThrows(IllegalArgumentException.class, () -> {
            helper.parseExpressionToDateInstance("2023-12-25", "invalid-pattern");
        });
        
        // 测试日期字符串与格式不匹配
        assertThrows(IllegalArgumentException.class, () -> {
            helper.parseExpressionToDateInstance("2023/12/25", KeelDateTimeHelper.MYSQL_DATE_PATTERN);
        });
    }

    @Test
    void testParseExpressionToDateInstance_LeapYear() {
        KeelDateTimeHelper helper = KeelDateTimeHelper.getInstance();
        
        // 测试闰年的2月29日 - 应该成功
        Date result = helper.parseExpressionToDateInstance("2024-02-29", KeelDateTimeHelper.MYSQL_DATE_PATTERN);
        assertNotNull(result);
        
        // 测试非闰年的2月29日 - 应该失败
        assertThrows(IllegalArgumentException.class, () -> {
            helper.parseExpressionToDateInstance("2023-02-29", KeelDateTimeHelper.MYSQL_DATE_PATTERN);
        });
    }
}