package io.github.sinri.keel.core.cron;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

class KeelCronExpressionTest extends KeelUnitTest {

    @Test
    @DisplayName("测试parseCalenderToElements静态方法")
    void parseCalenderToElements() {
        // 创建一个测试日历：2024年1月15日 10:30:45 星期一
        Calendar calendar = new GregorianCalendar(2024, Calendar.JANUARY, 15, 10, 30, 45);
        
        ParsedCalenderElements elements = KeelCronExpression.parseCalenderToElements(calendar);
        
        assertEquals(30, elements.minute);
        assertEquals(10, elements.hour);
        assertEquals(15, elements.day);
        assertEquals(1, elements.month); // 1-based month
        assertEquals(1, elements.weekday); // Monday = 1 (Sunday = 0)
        assertEquals(45, elements.second);
    }

    @Test
    @DisplayName("测试基本数字cron表达式匹配")
    void match() {
        // 创建cron表达式：每天 10:30
        KeelCronExpression cron = new KeelCronExpression("30 10 * * *");
        
        // 匹配的时间：2024年1月15日 10:30:00
        Calendar matchingCalendar = new GregorianCalendar(2024, Calendar.JANUARY, 15, 10, 30, 0);
        assertTrue(cron.match(matchingCalendar));
        
        // 不匹配的时间：2024年1月15日 10:31:00
        Calendar nonMatchingCalendar = new GregorianCalendar(2024, Calendar.JANUARY, 15, 10, 31, 0);
        assertFalse(cron.match(nonMatchingCalendar));
        
        // 不匹配的时间：2024年1月15日 11:30:00
        Calendar nonMatchingCalendar2 = new GregorianCalendar(2024, Calendar.JANUARY, 15, 11, 30, 0);
        assertFalse(cron.match(nonMatchingCalendar2));
    }

    @Test
    @DisplayName("测试ParsedCalenderElements匹配")
    void testMatch() {
        KeelCronExpression cron = new KeelCronExpression("0 0 1 1 0");
        
        // 匹配：1月1日 00:00 星期日
        ParsedCalenderElements matching = new ParsedCalenderElements(0, 0, 1, 1, 0);
        assertTrue(cron.match(matching));
        
        // 不匹配：不同分钟
        ParsedCalenderElements nonMatching1 = new ParsedCalenderElements(1, 0, 1, 1, 0);
        assertFalse(cron.match(nonMatching1));
        
        // 不匹配：不同小时
        ParsedCalenderElements nonMatching2 = new ParsedCalenderElements(0, 1, 1, 1, 0);
        assertFalse(cron.match(nonMatching2));
        
        // 不匹配：不同天
        ParsedCalenderElements nonMatching3 = new ParsedCalenderElements(0, 0, 2, 1, 0);
        assertFalse(cron.match(nonMatching3));
        
        // 不匹配：不同月
        ParsedCalenderElements nonMatching4 = new ParsedCalenderElements(0, 0, 1, 2, 0);
        assertFalse(cron.match(nonMatching4));
        
        // 不匹配：不同星期
        ParsedCalenderElements nonMatching5 = new ParsedCalenderElements(0, 0, 1, 1, 1);
        assertFalse(cron.match(nonMatching5));
    }

    @Test
    @DisplayName("测试通配符cron表达式")
    void testWildcardExpression() {
        KeelCronExpression cron = new KeelCronExpression("* * * * *");
        
        // 任何时间都应该匹配
        Calendar calendar1 = new GregorianCalendar(2024, Calendar.JANUARY, 1, 0, 0, 0);
        assertTrue(cron.match(calendar1));
        
        Calendar calendar2 = new GregorianCalendar(2024, Calendar.DECEMBER, 31, 23, 59, 59);
        assertTrue(cron.match(calendar2));
        
        Calendar calendar3 = new GregorianCalendar(2024, Calendar.JUNE, 15, 12, 30, 45);
        assertTrue(cron.match(calendar3));
    }

    @Test
    @DisplayName("测试范围cron表达式")
    void testRangeExpression() {
        KeelCronExpression cron = new KeelCronExpression("0-5 8-17 1-15 1-6 1-5");
        
        // 匹配：分钟0-5, 小时8-17, 天1-15, 月1-6, 星期一-五
        Calendar matching = new GregorianCalendar(2024, Calendar.JANUARY, 15, 10, 3, 0);
        assertTrue(cron.match(matching));
        
        // 不匹配：分钟超出范围
        Calendar nonMatching1 = new GregorianCalendar(2024, Calendar.JANUARY, 15, 10, 6, 0);
        assertFalse(cron.match(nonMatching1));
        
        // 不匹配：小时超出范围
        Calendar nonMatching2 = new GregorianCalendar(2024, Calendar.JANUARY, 15, 18, 3, 0);
        assertFalse(cron.match(nonMatching2));
    }

    @Test
    @DisplayName("测试列表cron表达式")
    void testListExpression() {
        KeelCronExpression cron = new KeelCronExpression("0,30 9,12,18 * * *");
        
        // 匹配：分钟0或30, 小时9,12,18
        Calendar matching1 = new GregorianCalendar(2024, Calendar.JANUARY, 15, 9, 0, 0);
        assertTrue(cron.match(matching1));
        
        Calendar matching2 = new GregorianCalendar(2024, Calendar.JANUARY, 15, 12, 30, 0);
        assertTrue(cron.match(matching2));
        
        Calendar matching3 = new GregorianCalendar(2024, Calendar.JANUARY, 15, 18, 0, 0);
        assertTrue(cron.match(matching3));
        
        // 不匹配：分钟不在列表中
        Calendar nonMatching1 = new GregorianCalendar(2024, Calendar.JANUARY, 15, 9, 15, 0);
        assertFalse(cron.match(nonMatching1));
        
        // 不匹配：小时不在列表中
        Calendar nonMatching2 = new GregorianCalendar(2024, Calendar.JANUARY, 15, 10, 0, 0);
        assertFalse(cron.match(nonMatching2));
    }

    @Test
    @DisplayName("测试增量cron表达式")
    void testIncrementExpression() {
        KeelCronExpression cron = new KeelCronExpression("*/15 */6 * * *");
        
        // 匹配：每15分钟，每6小时
        Calendar matching1 = new GregorianCalendar(2024, Calendar.JANUARY, 15, 0, 0, 0);
        assertTrue(cron.match(matching1));
        
        Calendar matching2 = new GregorianCalendar(2024, Calendar.JANUARY, 15, 6, 15, 0);
        assertTrue(cron.match(matching2));
        
        Calendar matching3 = new GregorianCalendar(2024, Calendar.JANUARY, 15, 12, 30, 0);
        assertTrue(cron.match(matching3));
        
        Calendar matching4 = new GregorianCalendar(2024, Calendar.JANUARY, 15, 18, 45, 0);
        assertTrue(cron.match(matching4));
        
        // 不匹配：分钟不是15的倍数
        Calendar nonMatching1 = new GregorianCalendar(2024, Calendar.JANUARY, 15, 0, 7, 0);
        assertFalse(cron.match(nonMatching1));
        
        // 不匹配：小时不是6的倍数
        Calendar nonMatching2 = new GregorianCalendar(2024, Calendar.JANUARY, 15, 5, 0, 0);
        assertFalse(cron.match(nonMatching2));
    }

    @Test
    @DisplayName("测试边界值")
    void testBoundaryValues() {
        // 测试最小值
        KeelCronExpression cron1 = new KeelCronExpression("0 0 1 1 0");
        Calendar calendar1 = new GregorianCalendar(2023, Calendar.JANUARY, 1, 0, 0, 0); // 2023-01-01 是星期日
        assertTrue(cron1.match(calendar1));
        
        // 测试最大值
        KeelCronExpression cron2 = new KeelCronExpression("59 23 31 12 6");
        Calendar calendar2_correct = new GregorianCalendar(2022, Calendar.DECEMBER, 31, 23, 59, 0); // 2022-12-31 是星期六
        assertTrue(cron2.match(calendar2_correct));
    }

    @Test
    @DisplayName("测试无效cron表达式")
    void testInvalidCronExpressions() {
        // 测试字段数量不正确
        assertThrows(RuntimeException.class, () -> new KeelCronExpression("0 0 1 1"));
        assertThrows(RuntimeException.class, () -> new KeelCronExpression("0 0 1 1 0 0"));
        assertThrows(RuntimeException.class, () -> new KeelCronExpression(""));
        
        // 测试无效的范围表达式（这些会抛出异常）
        assertThrows(IllegalArgumentException.class, () -> new KeelCronExpression("50-60 0 1 1 0")); // 分钟范围超出
        assertThrows(IllegalArgumentException.class, () -> new KeelCronExpression("0 20-24 1 1 0")); // 小时范围超出
        assertThrows(IllegalArgumentException.class, () -> new KeelCronExpression("0 0 30-32 1 0")); // 天范围超出
        assertThrows(IllegalArgumentException.class, () -> new KeelCronExpression("0 0 1 10-13 0")); // 月范围超出
        assertThrows(IllegalArgumentException.class, () -> new KeelCronExpression("0 0 1 1 5-7")); // 星期范围超出
        
        // 测试无效的范围（开始值大于结束值）
        assertThrows(IllegalArgumentException.class, () -> new KeelCronExpression("10-5 0 1 1 0")); // 开始值大于结束值
        assertThrows(IllegalArgumentException.class, () -> new KeelCronExpression("0 0 15-10 1 0")); // 开始值大于结束值
        
        // 测试无效的格式
        assertThrows(IllegalArgumentException.class, () -> new KeelCronExpression("abc 0 1 1 0")); // 非数字字符
        assertThrows(IllegalArgumentException.class, () -> new KeelCronExpression("0- 0 1 1 0")); // 不完整的范围
        assertThrows(IllegalArgumentException.class, () -> new KeelCronExpression("-5 0 1 1 0")); // 无效的范围格式
    }

    @Test
    @DisplayName("测试getRawCronExpression方法")
    void getRawCronExpression() {
        String expression = "0 0 1 1 0";
        KeelCronExpression cron = new KeelCronExpression(expression);
        
        assertEquals(expression, cron.getRawCronExpression());
        
        // 测试不同的表达式
        String expression2 = "*/5 * * * *";
        KeelCronExpression cron2 = new KeelCronExpression(expression2);
        assertEquals(expression2, cron2.getRawCronExpression());
    }

    @Test
    @DisplayName("测试toString方法")
    void testToString() {
        String expression = "0 0 1 1 0";
        KeelCronExpression cron = new KeelCronExpression(expression);
        
        assertEquals(expression, cron.toString());
        
        // 测试toString与getRawCronExpression的一致性
        assertEquals(cron.getRawCronExpression(), cron.toString());
    }

    @Test
    @DisplayName("测试复杂的cron表达式")
    void testComplexCronExpressions() {
        // 测试混合使用列表和范围
        KeelCronExpression cron1 = new KeelCronExpression("0,15,30,45 9-17 1-15,20-25 * 1-5");
        Calendar calendar1 = new GregorianCalendar(2024, Calendar.JANUARY, 10, 12, 15, 0); // 2024-01-10 是星期三
        assertTrue(cron1.match(calendar1));
        
        // 测试包含增量和列表的表达式
        KeelCronExpression cron2 = new KeelCronExpression("*/10 8,12,16,20 * * *");
        Calendar calendar2 = new GregorianCalendar(2024, Calendar.JANUARY, 15, 16, 20, 0);
        assertTrue(cron2.match(calendar2));
        
        Calendar calendar3 = new GregorianCalendar(2024, Calendar.JANUARY, 15, 16, 25, 0);
        assertFalse(cron2.match(calendar3)); // 25不是10的倍数
    }

    @Test
    @DisplayName("测试ParsedCalenderElements的构造和toString")
    void testParsedCalenderElements() {
        // 测试完整构造函数
        ParsedCalenderElements elements1 = new ParsedCalenderElements(30, 10, 15, 1, 1, 45);
        assertEquals(30, elements1.minute);
        assertEquals(10, elements1.hour);
        assertEquals(15, elements1.day);
        assertEquals(1, elements1.month);
        assertEquals(1, elements1.weekday);
        assertEquals(45, elements1.second);
        
        // 测试简化构造函数
        ParsedCalenderElements elements2 = new ParsedCalenderElements(0, 0, 1, 1, 0);
        assertEquals(0, elements2.minute);
        assertEquals(0, elements2.hour);
        assertEquals(1, elements2.day);
        assertEquals(1, elements2.month);
        assertEquals(0, elements2.weekday);
        assertEquals(0, elements2.second); // 默认为0
        
        // 测试toString方法
        String expected = "(45) 30 10 15 1 1";
        assertEquals(expected, elements1.toString());
        
        // 测试从Calendar构造
        Calendar calendar = new GregorianCalendar(2024, Calendar.FEBRUARY, 29, 14, 30, 15);
        ParsedCalenderElements elements3 = new ParsedCalenderElements(calendar);
        assertEquals(30, elements3.minute);
        assertEquals(14, elements3.hour);
        assertEquals(29, elements3.day);
        assertEquals(2, elements3.month); // February = 2 (1-based)
        assertEquals(4, elements3.weekday); // 2024-02-29 是星期四 (Thursday = 4)
        assertEquals(15, elements3.second);
    }

    @Test
    @DisplayName("测试特殊日期和时间")
    void testSpecialDatesAndTimes() {
        // 测试闰年2月29日
        KeelCronExpression cron = new KeelCronExpression("0 0 29 2 *");
        Calendar leapYear = new GregorianCalendar(2024, Calendar.FEBRUARY, 29, 0, 0, 0);
        assertTrue(cron.match(leapYear));
        
        // 测试年末最后一天
        KeelCronExpression cron2 = new KeelCronExpression("59 23 31 12 *");
        Calendar yearEnd = new GregorianCalendar(2024, Calendar.DECEMBER, 31, 23, 59, 0);
        assertTrue(cron2.match(yearEnd));
        
        // 测试夏令时切换时间（虽然cron表达式不直接处理时区，但测试各种时间）
        KeelCronExpression cron3 = new KeelCronExpression("0 2 * * 0");
        Calendar dst = new GregorianCalendar(2024, Calendar.MARCH, 10, 2, 0, 0); // 2024-03-10 是星期日
        assertTrue(cron3.match(dst));
    }
}