package io.github.sinri.keel.facade.cli;

import io.github.sinri.keel.core.json.JsonifiableSerializer;
import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class KeelCliArgsParserTest extends KeelJUnit5Test {

    private KeelCliArgsParser parser;

    /**
     * The constructor would run after {@code @BeforeAll} annotated method.
     * Here, {@link JsonifiableSerializer#register()} would be called, and a {@link KeelIssueRecorder} would be created.
     */
    public KeelCliArgsParserTest(Vertx vertx) {
        super(vertx);
        // System.out.println("CommandLineParserTest constructor with vertx " + vertx);
    }

    @BeforeEach
    void setUp() {
        parser = KeelCliArgsParser.create();
    }

    @Test
    @DisplayName("测试创建CommandLineParser实例")
    void testCreateParser() {
        assertNotNull(parser);
    }


    @Test
    @DisplayName("测试添加有效选项")
    void testAddValidOption() throws KeelCliArgsDefinitionError {
        KeelCliOption option1 = new KeelCliOption().alias("v").alias("verbose").description("Verbose output");
        KeelCliOption option2 = new KeelCliOption().alias("f").alias("file").description("Input file");
        KeelCliOption option3 = new KeelCliOption().alias("help").description("Show help");

        assertDoesNotThrow(() -> parser.addOption(option1));
        assertDoesNotThrow(() -> parser.addOption(option2));
        assertDoesNotThrow(() -> parser.addOption(option3));
    }

    @Test
    @DisplayName("测试添加重复ID的选项")
    void testAddDuplicateIdOption() throws KeelCliArgsDefinitionError {
        KeelCliOption option1 = new KeelCliOption().alias("v");
        parser.addOption(option1);

        // 尝试添加相同ID的选项应该抛出异常
        assertThrows(KeelCliArgsDefinitionError.class, () -> parser.addOption(option1));
    }

    @Test
    @DisplayName("测试添加重复别名的选项")
    void testAddDuplicateAliasOption() throws KeelCliArgsDefinitionError {
        KeelCliOption option1 = new KeelCliOption().alias("v");
        KeelCliOption option2 = new KeelCliOption().alias("v"); // 重复的短别名
        KeelCliOption option3 = new KeelCliOption().alias("verbose");
        KeelCliOption option4 = new KeelCliOption().alias("verbose"); // 重复的长别名

        parser.addOption(option1);
        assertThrows(KeelCliArgsDefinitionError.class, () -> parser.addOption(option2));

        parser.addOption(option3);
        assertThrows(KeelCliArgsDefinitionError.class, () -> parser.addOption(option4));
    }

    @Test
    @DisplayName("测试添加无别名的选项")
    void testAddOptionWithoutAlias() {
        KeelCliOption option = new KeelCliOption(); // 没有添加任何别名
        assertThrows(KeelCliArgsDefinitionError.class, () -> parser.addOption(option));
    }

    @Test
    @DisplayName("测试解析空参数")
    void testParseEmptyArgs() throws KeelCliArgsParseError {
        KeelCliArgs result = parser.parse(new String[]{});
        assertNotNull(result);
        assertNull(result.readParameter(0));

        // 测试null参数
        result = parser.parse(null);
        assertNotNull(result);
        assertNull(result.readParameter(0));
    }

    @Test
    @DisplayName("测试解析长选项")
    void testParseLongOptions() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        parser.addOption(new KeelCliOption().alias("file"));
        parser.addOption(new KeelCliOption().alias("output"));

        // 测试 --file value 格式
        KeelCliArgs result = parser.parse(new String[]{"--file", "input.txt"});
        assertEquals("input.txt", result.readOption("file"));

        // 测试 --output value 格式 (注意：当前实现不支持 --option=value 语法)
        result = parser.parse(new String[]{"--output", "result.txt"});
        assertEquals("result.txt", result.readOption("output"));
    }

    @Test
    @DisplayName("测试解析短选项")
    void testParseShortOptions() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        parser.addOption(new KeelCliOption().alias("f"));
        parser.addOption(new KeelCliOption().alias("o"));

        // 测试 -f value 格式
        KeelCliArgs result = parser.parse(new String[]{"-f", "input.txt"});
        assertEquals("input.txt", result.readOption('f'));

        // 测试 -o value 格式 (注意：当前实现不支持 -o=value 语法)
        result = parser.parse(new String[]{"-o", "output.txt"});
        assertEquals("output.txt", result.readOption('o'));
    }

    @Test
    @DisplayName("测试解析混合选项")
    void testParseMixedOptions() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        parser.addOption(new KeelCliOption().alias("f").alias("file"));
        parser.addOption(new KeelCliOption().alias("o").alias("output"));

        KeelCliArgs result = parser.parse(new String[]{
                "-f", "input.txt", "--output", "result.txt"
        });

        assertEquals("input.txt", result.readOption('f'));
        assertEquals("input.txt", result.readOption("file"));
        assertEquals("result.txt", result.readOption('o'));
        assertEquals("result.txt", result.readOption("output"));
    }

    @Test
    @DisplayName("测试解析参数")
    void testParseParameters() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        parser.addOption(new KeelCliOption().alias("f"));

        // 测试在选项之前的参数 - 当前实现在遇到第一个参数后会进入参数模式
        KeelCliArgs result = parser.parse(new String[]{
                "param1", "param2", "param3"
        });

        assertEquals("param1", result.readParameter(0));
        assertEquals("param2", result.readParameter(1));
        assertEquals("param3", result.readParameter(2));
    }

    @Test
    @DisplayName("测试解析带--分隔符的参数")
    void testParseParametersWithDoubleDash() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        parser.addOption(new KeelCliOption().alias("f"));

        KeelCliArgs result = parser.parse(new String[]{
                "-f", "file.txt", "--", "param1", "param2", "-v"
        });

        assertEquals("file.txt", result.readOption('f'));
        assertEquals("param1", result.readParameter(0));
        assertEquals("param2", result.readParameter(1));
        assertEquals("-v", result.readParameter(2)); // --后面的都是参数
    }

    @Test
    @DisplayName("测试解析未定义选项")
    void testParseUndefinedOption() {
        // 测试未定义的长选项
        assertThrows(KeelCliArgsParseError.class, () ->
                parser.parse(new String[]{"--undefined", "value"}));

        // 测试未定义的短选项
        assertThrows(KeelCliArgsParseError.class, () ->
                parser.parse(new String[]{"-u", "value"}));
    }


    @Test
    @DisplayName("测试选项值为null的情况")
    void testOptionWithNullValue() throws KeelCliArgsDefinitionError {
        parser.addOption(new KeelCliOption().alias("f"));

        // 测试最后一个选项没有值 - 应该抛出异常
        assertThrows(KeelCliArgsParseError.class, () -> 
            parser.parse(new String[]{"-f"}));
    }

    @Test
    @DisplayName("测试Flag选项功能")
    void testFlagOptions() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        parser.addOption(new KeelCliOption().alias("v").alias("verbose").flag());
        parser.addOption(new KeelCliOption().alias("h").alias("help").flag());
        parser.addOption(new KeelCliOption().alias("f").alias("file")); // 非flag选项

        // 测试flag选项解析
        KeelCliArgs result = parser.parse(new String[]{"-v", "--help", "-f", "test.txt"});
        
        // flag选项应该返回空字符串
        assertEquals("", result.readOption('v'));
        assertEquals("", result.readOption("verbose"));
        assertEquals("", result.readOption('h'));
        assertEquals("", result.readOption("help"));
        
        // flag选项的readFlag应该返回true
        assertTrue(result.readFlag('v'));
        assertTrue(result.readFlag("verbose"));
        assertTrue(result.readFlag('h'));
        assertTrue(result.readFlag("help"));
        
        // 非flag选项正常处理
        assertEquals("test.txt", result.readOption('f'));
    }

    @Test
    @DisplayName("测试Flag选项的isFlag方法")
    void testFlagOptionProperties() {
        KeelCliOption flagOption = new KeelCliOption().alias("v").flag();
        KeelCliOption normalOption = new KeelCliOption().alias("f");
        
        assertTrue(flagOption.isFlag());
        assertFalse(normalOption.isFlag());
    }

    @Test
    @DisplayName("测试未设置的Flag选项")
    void testUnsetFlagOptions() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        parser.addOption(new KeelCliOption().alias("v").flag());
        parser.addOption(new KeelCliOption().alias("h").flag());
        
        KeelCliArgs result = parser.parse(new String[]{"param1", "param2"});
        
        // 未设置的flag选项应该返回null和false
        assertNull(result.readOption('v'));
        assertNull(result.readOption('h'));
        assertFalse(result.readFlag('v'));
        assertFalse(result.readFlag('h'));
    }

    @Test
    @DisplayName("测试值验证器功能")
    void testValueValidatorFunction() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        // 创建一个只接受数字的验证器
        KeelCliOption numericOption = new KeelCliOption()
                .alias("n")
                .alias("number")
                .setValueValidator(value -> value != null && value.matches("\\d+"));
        
        parser.addOption(numericOption);
        
        // 测试有效数字值
        KeelCliArgs result = parser.parse(new String[]{"-n", "123"});
        assertEquals("123", result.readOption('n'));
        
        // 测试无效值应该抛出异常
        assertThrows(KeelCliArgsParseError.class, () ->
                parser.parse(new String[]{"-n", "abc"}));
    }

    @Test
    @DisplayName("测试值验证器为null的情况")
    void testNullValueValidator() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        KeelCliOption option = new KeelCliOption()
                .alias("f")
                .setValueValidator(null); // 设置为null
        
        parser.addOption(option);
        
        // 没有验证器时应该接受任何值
        KeelCliArgs result = parser.parse(new String[]{"-f", "any_value"});
        assertEquals("any_value", result.readOption('f'));
    }

    @Test
    @DisplayName("测试Flag选项不使用值验证器")
    void testFlagOptionIgnoresValidator() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        KeelCliOption flagOption = new KeelCliOption()
                .alias("v")
                .flag()
                .setValueValidator(value -> false); // 设置一个总是返回false的验证器
        
        parser.addOption(flagOption);
        
        // Flag选项应该忽略验证器
        KeelCliArgs result = parser.parse(new String[]{"-v"});
        assertEquals("", result.readOption('v'));
        assertTrue(result.readFlag('v'));
    }

    @Test
    @DisplayName("测试值验证器getter方法")
    void testValueValidatorGetter() {
        KeelCliOption option = new KeelCliOption();
        
        // 默认应该是null
        assertNull(option.getValueValidator());
        
        // 设置验证器后应该能获取到
        Function<String, Boolean> validator = (String value) -> true;
        option.setValueValidator(validator);
        assertEquals(validator, option.getValueValidator());
    }

    @Test
    @DisplayName("测试使用Handler添加选项")
    void testAddOptionWithHandler() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        // 使用Handler添加普通选项
        assertDoesNotThrow(() -> parser.addOption(option ->
                option.alias("v").alias("verbose").description("Verbose output")));

        // 使用Handler添加Flag选项
        assertDoesNotThrow(() -> parser.addOption(option ->
                option.alias("h").alias("help").description("Show help").flag()));

        // 使用Handler添加带验证器的选项
        assertDoesNotThrow(() -> parser.addOption(option ->
                option.alias("n").alias("number")
                      .description("Numeric value")
                      .setValueValidator(value -> value != null && value.matches("\\d+"))));

        // 验证Handler添加的选项能正常工作
        KeelCliArgs result = parser.parse(new String[]{
                "--verbose", "output_value", "--help", "--number", "42"
        });

        assertEquals("output_value", result.readOption("verbose"));
        assertTrue(result.readFlag("help"));
        assertEquals("42", result.readOption("number"));
    }

    @Test
    @DisplayName("测试Handler添加选项时的异常处理")
    void testAddOptionWithHandlerExceptions() {
        // Handler内部添加无效别名应该抛出异常
        assertThrows(KeelCliArgsDefinitionError.class, () ->
                parser.addOption(option -> option.alias("-invalid")));

        // Handler内部添加空别名应该抛出异常
        assertThrows(KeelCliArgsDefinitionError.class, () ->
                parser.addOption(option -> option.alias("")));
    }

    @Test
    @DisplayName("测试复杂命令行解析")
    void testComplexCommandLineParsing() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        parser.addOption(new KeelCliOption().alias("f").alias("file"));
        parser.addOption(new KeelCliOption().alias("o").alias("output"));

        KeelCliArgs result = parser.parse(new String[]{
                "--file", "input.txt", "--output", "result.txt", "--", "param1", "param2"
        });

        assertEquals("input.txt", result.readOption("file"));
        assertEquals("input.txt", result.readOption('f'));
        assertEquals("result.txt", result.readOption("output"));
        assertEquals("result.txt", result.readOption('o'));
        assertEquals("param1", result.readParameter(0));
        assertEquals("param2", result.readParameter(1));
    }

    @Test
    @DisplayName("测试读取不存在的选项和参数")
    void testReadNonExistentOptionsAndParameters() throws KeelCliArgsParseError {
        KeelCliArgs result = parser.parse(new String[]{});

        assertNull(result.readOption('x'));
        assertNull(result.readOption("nonexistent"));
        assertFalse(result.readFlag('x'));
        assertFalse(result.readFlag("nonexistent"));
        assertNull(result.readParameter(0));
        assertNull(result.readParameter(-1));
        assertNull(result.readParameter(100));
    }

    @Test
    @DisplayName("测试null参数处理")
    void testNullArgumentHandling() throws KeelCliArgsParseError {
        KeelCliArgs result = parser.parse(new String[]{null, "param1", null, "param2"});
        assertEquals("param1", result.readParameter(0));
        assertEquals("param2", result.readParameter(1));
        assertNull(result.readParameter(2));
    }

    @Test
    @DisplayName("测试边界条件")
    void testEdgeCases() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        parser.addOption(new KeelCliOption().alias("test"));

        // 测试只有--的情况
        KeelCliArgs result = parser.parse(new String[]{"--"});
        assertNull(result.readParameter(0));

        // 测试空字符串参数
        result = parser.parse(new String[]{""});
        assertEquals("", result.readParameter(0));

        // 测试只有选项名没有值的情况 - 应该抛出异常
        assertThrows(KeelCliArgsParseError.class, () -> 
            parser.parse(new String[]{"--test"}));
    }

    @Test
    @DisplayName("测试Option类的基本功能")
    void testOptionBasicFunctionality() {
        KeelCliOption option = new KeelCliOption();

        // 测试ID是否自动生成
        assertNotNull(option.id());
        assertFalse(option.id().isEmpty());

        // 测试默认值
        assertNull(option.description());
        assertFalse(option.isFlag());
        assertTrue(option.getAliasSet().isEmpty());

        // 测试链式调用
        option.alias("v").alias("verbose").description("Verbose mode");

        assertEquals("Verbose mode", option.description());
        assertFalse(option.isFlag()); // 不设置为flag
        assertTrue(option.getAliasSet().contains("v"));
        assertTrue(option.getAliasSet().contains("verbose"));
        assertEquals(2, option.getAliasSet().size());
    }

    @Test
    @DisplayName("测试Option别名验证")
    void testOptionAliasValidation() {
        // 测试null别名
        assertThrows(IllegalArgumentException.class, () -> KeelCliOption.validatedAlias(null));

        // 测试空别名
        assertThrows(IllegalArgumentException.class, () -> KeelCliOption.validatedAlias(""));
        assertThrows(IllegalArgumentException.class, () -> KeelCliOption.validatedAlias("   "));

        // 测试包含空格的别名
        assertThrows(IllegalArgumentException.class, () -> KeelCliOption.validatedAlias("test alias"));
        assertThrows(IllegalArgumentException.class, () -> KeelCliOption.validatedAlias("test\t"));

        // 测试以-开头的别名
        assertThrows(IllegalArgumentException.class, () -> KeelCliOption.validatedAlias("-test"));
        assertThrows(IllegalArgumentException.class, () -> KeelCliOption.validatedAlias("--verbose"));

        // 测试有效别名
        assertDoesNotThrow(() -> KeelCliOption.validatedAlias("v"));
        assertDoesNotThrow(() -> KeelCliOption.validatedAlias("verbose"));
        assertDoesNotThrow(() -> KeelCliOption.validatedAlias("test123"));
        assertDoesNotThrow(() -> KeelCliOption.validatedAlias("test_option"));
        assertDoesNotThrow(() -> KeelCliOption.validatedAlias("test.option"));
    }

    @Test
    @DisplayName("测试Option添加无效别名")
    @SuppressWarnings("null")
    void testOptionInvalidAlias() {
        KeelCliOption option = new KeelCliOption();

        // 测试添加null别名
        assertThrows(KeelCliArgsDefinitionError.class, () -> option.alias(null));

        // 测试添加空别名
        assertThrows(KeelCliArgsDefinitionError.class, () -> option.alias(""));
        assertThrows(KeelCliArgsDefinitionError.class, () -> option.alias("   "));

        // 测试添加包含空格的别名
        assertThrows(KeelCliArgsDefinitionError.class, () -> option.alias("test alias"));

        // 测试添加以-开头的别名
        assertThrows(KeelCliArgsDefinitionError.class, () -> option.alias("-test"));
    }

    @Test
    @DisplayName("测试CommandLineParser正则表达式匹配")
    void testRegexPatternMatching() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        parser.addOption(new KeelCliOption().alias("test-option"));
        parser.addOption(new KeelCliOption().alias("test_option"));
        parser.addOption(new KeelCliOption().alias("test.option"));
        parser.addOption(new KeelCliOption().alias("test123"));

        KeelCliArgs result = parser.parse(new String[]{
                "--test-option", "value1", "--test_option", "value2",
                "--test.option", "value3", "--test123", "value4"
        });

        assertEquals("value1", result.readOption("test-option"));
        assertEquals("value2", result.readOption("test_option"));
        assertEquals("value3", result.readOption("test.option"));
        assertEquals("value4", result.readOption("test123"));
    }

    @Test
    @DisplayName("测试Option别名集合的不可变性")
    void testOptionAliasSetImmutability() {
        KeelCliOption option = new KeelCliOption().alias("v").alias("verbose");
        Set<String> aliasSet = option.getAliasSet();

        // 尝试修改返回的集合应该抛出异常
        assertThrows(UnsupportedOperationException.class, () -> aliasSet.add("new_alias"));
        assertThrows(UnsupportedOperationException.class, () -> aliasSet.remove("v"));
        assertThrows(UnsupportedOperationException.class, () -> aliasSet.clear());
    }

    @Test
    @DisplayName("测试添加具有特殊字符别名的选项")
    void testAddOptionWithSpecialCharAlias() throws KeelCliArgsDefinitionError {
        // 测试包含下划线、点号、连字符的长别名
        KeelCliOption option1 = new KeelCliOption().alias("my_option");
        KeelCliOption option2 = new KeelCliOption().alias("my.option");
        KeelCliOption option3 = new KeelCliOption().alias("my-option");
        KeelCliOption option4 = new KeelCliOption().alias("option123");

        assertDoesNotThrow(() -> parser.addOption(option1));
        assertDoesNotThrow(() -> parser.addOption(option2));
        assertDoesNotThrow(() -> parser.addOption(option3));
        assertDoesNotThrow(() -> parser.addOption(option4));
    }


    @Test
    @DisplayName("测试CommandLineParsedResult的边界情况")
    void testParsedResultEdgeCases() throws KeelCliArgsParseError {
        KeelCliArgs result = parser.parse(new String[]{});

        // 测试读取空字符串长选项名
        assertNull(result.readOption(""));
        assertFalse(result.readFlag(""));

        // 测试读取只有空格的长选项名
        assertNull(result.readOption("   "));
        assertFalse(result.readFlag("   "));

        // 测试读取负索引参数
        assertNull(result.readParameter(-1));
        assertNull(result.readParameter(-100));
    }

    @Test
    @DisplayName("测试复杂参数组合的解析")
    void testComplexArgumentCombinations() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        parser.addOption(new KeelCliOption().alias("i").alias("input"));
        parser.addOption(new KeelCliOption().alias("o").alias("output"));

        // 测试真实命令行场景 - 当前实现在遇到第一个参数后会进入参数模式
        KeelCliArgs result = parser.parse(new String[]{
                "-i", "input.txt", "--output", "output.txt",
                "--", "command", "--not-an-option", "param1", "param2"
        });

        assertEquals("input.txt", result.readOption('i'));
        assertEquals("input.txt", result.readOption("input"));
        assertEquals("output.txt", result.readOption('o'));
        assertEquals("output.txt", result.readOption("output"));
        assertEquals("command", result.readParameter(0));
        assertEquals("--not-an-option", result.readParameter(1));
        assertEquals("param1", result.readParameter(2));
        assertEquals("param2", result.readParameter(3));
    }


    @Test
    @DisplayName("测试不同类型的异常情况")
    void testExceptionScenarios() throws KeelCliArgsDefinitionError {
        parser.addOption(new KeelCliOption().alias("test"));

        // 测试添加包含白字符的别名 - 注意：Option.alias()方法抛出IllegalArgumentException
        assertThrows(KeelCliArgsDefinitionError.class, () ->
                new KeelCliOption().alias("test\nname"));

        assertThrows(KeelCliArgsDefinitionError.class, () ->
                new KeelCliOption().alias("test\tname"));

        assertThrows(KeelCliArgsDefinitionError.class, () ->
                new KeelCliOption().alias("test name"));
    }

    @Test
    @DisplayName("测试更多异常处理场景")
    void testAdditionalExceptionScenarios() throws KeelCliArgsDefinitionError {
        // 测试添加重复选项到已有选项的情况
        KeelCliOption option1 = new KeelCliOption().alias("duplicate");
        parser.addOption(option1);
        
        // 尝试添加相同的选项实例应该抛出异常
        assertThrows(KeelCliArgsDefinitionError.class, () -> parser.addOption(option1));
        
        // 测试解析时选项缺少值的各种情况
        parser.addOption(new KeelCliOption().alias("value-required"));
        
        // 长选项缺少值
        assertThrows(KeelCliArgsParseError.class, () ->
                parser.parse(new String[]{"--value-required"}));
        
        // 选项后面直接跟-- (实际上--会作为选项的值)
        try {
            KeelCliArgs result = parser.parse(new String[]{"--value-required", "--"});
            assertEquals("--", result.readOption("value-required"));
        } catch (KeelCliArgsParseError e) {
            fail("Should not throw exception when -- is used as value");
        }
        
        // 选项后面跟另一个选项 (实际上另一个选项会作为第一个选项的值)
        try {
            KeelCliArgs result2 = parser.parse(new String[]{"--value-required", "--duplicate"});
            assertEquals("--duplicate", result2.readOption("value-required"));
        } catch (KeelCliArgsParseError e) {
            fail("Should not throw exception when another option is used as value");
        }
    }

    @Test
    @DisplayName("测试值验证失败的详细异常信息")
    void testValueValidationExceptionDetails() throws KeelCliArgsDefinitionError {
        KeelCliOption option = new KeelCliOption()
                .alias("port")
                .alias("p")
                .setValueValidator(value -> {
                    try {
                        int port = Integer.parseInt(value);
                        return port > 0 && port < 65536;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                });
        parser.addOption(option);
        
        // 测试无效端口号
        KeelCliArgsParseError exception = assertThrows(KeelCliArgsParseError.class, () ->
                parser.parse(new String[]{"--port", "70000"}));
        
        // 验证异常消息包含选项信息
        assertTrue(exception.getMessage().contains("port") || exception.getMessage().contains("p"));
        
        // 测试非数字值
        assertThrows(KeelCliArgsParseError.class, () ->
                parser.parse(new String[]{"-p", "not_a_number"}));
    }

    @Test
    @DisplayName("测试Option唯一ID生成")
    void testOptionUniqueIdGeneration() {
        KeelCliOption option1 = new KeelCliOption();
        KeelCliOption option2 = new KeelCliOption();
        KeelCliOption option3 = new KeelCliOption();

        // 每个Option应该有唯一的ID
        assertNotEquals(option1.id(), option2.id());
        assertNotEquals(option1.id(), option3.id());
        assertNotEquals(option2.id(), option3.id());

        // ID应该不为空且不是null
        assertNotNull(option1.id());
        assertNotNull(option2.id());
        assertNotNull(option3.id());
        assertFalse(option1.id().trim().isEmpty());
        assertFalse(option2.id().trim().isEmpty());
        assertFalse(option3.id().trim().isEmpty());
    }

    @Test
    @DisplayName("测试解析器状态重置")
    void testParserStateReset() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        parser.addOption(new KeelCliOption().alias("f"));

        // 第一次解析
        KeelCliArgs result1 = parser.parse(new String[]{"-f", "value1"});
        assertEquals("value1", result1.readOption('f'));

        // 第二次解析不同的参数，应该返回新的结果
        KeelCliArgs result2 = parser.parse(new String[]{"-f", "value2"});
        assertEquals("value2", result2.readOption('f'));
        assertEquals("value1", result1.readOption('f'));

        // 第三次解析空参数
        KeelCliArgs result3 = parser.parse(new String[]{});
        assertNull(result3.readOption('f'));
    }

    @Test
    @DisplayName("测试多个参数的处理")
    void testMultipleParametersHandling() throws KeelCliArgsParseError {
        KeelCliArgs result = parser.parse(new String[]{
                "param0", "param1", "param2", "param3", "param4"
        });

        assertEquals("param0", result.readParameter(0));
        assertEquals("param1", result.readParameter(1));
        assertEquals("param2", result.readParameter(2));
        assertEquals("param3", result.readParameter(3));
        assertEquals("param4", result.readParameter(4));
        assertNull(result.readParameter(5));
    }

    @Test
    @DisplayName("测试CommandLineParsedResult记录功能")
    void testParsedResultRecordFunctionality() {
        var resultWriter = KeelCliArgsWriter.create();
        KeelCliArgs result = resultWriter.toResult();

        // 测试记录参数
        resultWriter.recordParameter("param1");
        resultWriter.recordParameter("param2");
        assertEquals("param1", result.readParameter(0));
        assertEquals("param2", result.readParameter(1));

        // 测试记录选项
        KeelCliOption option = new KeelCliOption().alias("v").alias("verbose");
        resultWriter.recordOption(option,"test_value");

        assertEquals("test_value", result.readOption('v'));
        assertEquals("test_value", result.readOption("verbose"));
    }

    @Test
    @DisplayName("测试解析实际应用场景")
    void testRealWorldScenarios() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        // 模拟一个实际的命令行工具配置
        parser.addOption(new KeelCliOption().alias("c").alias("config").description("Config file path"));
        parser.addOption(new KeelCliOption().alias("o").alias("output").description("Output directory"));
        parser.addOption(new KeelCliOption().alias("mode").description("Processing mode"));

        // 场景1: 基本命令行解析 (使用--分隔符来区分选项和参数)
        KeelCliArgs result1 = parser.parse(new String[]{
                "--config", "/path/to/config.yml",
                "--output", "/tmp/output",
                "--mode", "verbose",
                "--",
                "input1.txt",
                "input2.txt"
        });

        assertEquals("/path/to/config.yml", result1.readOption("config"));
        assertEquals("/tmp/output", result1.readOption("output"));
        assertEquals("verbose", result1.readOption("mode"));
        assertEquals("input1.txt", result1.readParameter(0));
        assertEquals("input2.txt", result1.readParameter(1));

        // 场景2: 混合使用短和长选项
        KeelCliArgs result2 = parser.parse(new String[]{
                "-c", "config.ini",
                "-o", "/home/user/output",
                "--mode", "debug"
        });

        assertEquals("config.ini", result2.readOption('c'));
        assertEquals("/home/user/output", result2.readOption('o'));
        assertEquals("debug", result2.readOption("mode"));
    }

    @Test
    @DisplayName("测试特殊字符在参数中的处理")
    void testSpecialCharactersInParameters() throws KeelCliArgsParseError {
        KeelCliArgs result = parser.parse(new String[]{
                "param with spaces", "param\twith\ttabs", "param\nwith\nnewlines", "param\"with\"quotes"
        });

        assertEquals("param with spaces", result.readParameter(0));
        assertEquals("param\twith\ttabs", result.readParameter(1));
        assertEquals("param\nwith\nnewlines", result.readParameter(2));
        assertEquals("param\"with\"quotes", result.readParameter(3));
    }

    @Test
    @DisplayName("测试极长参数和选项值")
    void testVeryLongValuesAndParameters() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        parser.addOption(new KeelCliOption().alias("long"));
        
        String veryLongValue = "a".repeat(10000);
        String veryLongParam = "b".repeat(5000);
        
        KeelCliArgs result = parser.parse(new String[]{
                "--long", veryLongValue, "--", veryLongParam
        });

        assertEquals(veryLongValue, result.readOption("long"));
        assertEquals(veryLongParam, result.readParameter(0));
    }

    @Test
    @DisplayName("测试选项名称的边界情况")
    void testOptionNameEdgeCases() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        // 测试单字符选项
        parser.addOption(new KeelCliOption().alias("a"));
        parser.addOption(new KeelCliOption().alias("Z"));
        parser.addOption(new KeelCliOption().alias("9"));
        
        // 测试数字开头的长选项
        parser.addOption(new KeelCliOption().alias("1st"));
        parser.addOption(new KeelCliOption().alias("2nd-option"));
        
        KeelCliArgs result = parser.parse(new String[]{
                "-a", "value_a", "-Z", "value_Z", "-9", "value_9",
                "--1st", "first", "--2nd-option", "second"
        });

        assertEquals("value_a", result.readOption('a'));
        assertEquals("value_Z", result.readOption('Z'));
        assertEquals("value_9", result.readOption('9'));
        assertEquals("first", result.readOption("1st"));
        assertEquals("second", result.readOption("2nd-option"));
    }

    @Test
    @DisplayName("测试混合Flag和选项的复杂场景")
    void testComplexMixedFlagsAndOptions() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        parser.addOption(new KeelCliOption().alias("v").alias("verbose").flag());
        parser.addOption(new KeelCliOption().alias("q").alias("quiet").flag());
        parser.addOption(new KeelCliOption().alias("f").alias("file"));
        parser.addOption(new KeelCliOption().alias("o").alias("output"));
        parser.addOption(new KeelCliOption().alias("mode"));

        KeelCliArgs result = parser.parse(new String[]{
                "-v", "-f", "input.txt", "--quiet", "--output", "result.txt",
                "--mode", "production", "--", "param1", "param2"
        });

        assertTrue(result.readFlag('v'));
        assertTrue(result.readFlag("verbose"));
        assertTrue(result.readFlag('q'));
        assertTrue(result.readFlag("quiet"));
        assertEquals("input.txt", result.readOption('f'));
        assertEquals("result.txt", result.readOption("output"));
        assertEquals("production", result.readOption("mode"));
        assertEquals("param1", result.readParameter(0));
        assertEquals("param2", result.readParameter(1));
    }

    @Test
    @DisplayName("测试解析器状态隔离")
    void testParserStateIsolation() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        parser.addOption(new KeelCliOption().alias("test"));
        
        // 创建第二个解析器实例
        KeelCliArgsParser parser2 = KeelCliArgsParser.create();
        parser2.addOption(new KeelCliOption().alias("test2"));
        
        // 两个解析器应该独立工作
        KeelCliArgs result1 = parser.parse(new String[]{"--test", "value1"});
        KeelCliArgs result2 = parser2.parse(new String[]{"--test2", "value2"});
        
        assertEquals("value1", result1.readOption("test"));
        assertNull(result1.readOption("test2"));
        
        assertEquals("value2", result2.readOption("test2"));
        assertNull(result2.readOption("test"));
        
        // 解析器1不应该识别解析器2的选项
        assertThrows(KeelCliArgsParseError.class, () ->
                parser.parse(new String[]{"--test2", "value"}));
    }

    @Test
    @DisplayName("测试UTF-8和特殊Unicode字符")
    void testUnicodeCharacters() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        parser.addOption(new KeelCliOption().alias("unicode"));
        
        String unicodeValue = "测试中文🚀émojis";
        String unicodeParam = "参数τεστ🌟";
        
        KeelCliArgs result = parser.parse(new String[]{
                "--unicode", unicodeValue, "--", unicodeParam
        });

        assertEquals(unicodeValue, result.readOption("unicode"));
        assertEquals(unicodeParam, result.readParameter(0));
    }

    @Test
    @DisplayName("测试大量选项定义")
    void testManyOptionDefinitions() throws KeelCliArgsDefinitionError, KeelCliArgsParseError {
        // 添加大量选项
        for (int i = 0; i < 100; i++) {
            parser.addOption(new KeelCliOption().alias("opt" + i));
        }
        
        // 测试能正确解析
        KeelCliArgs result = parser.parse(new String[]{
                "--opt0", "value0", "--opt50", "value50", "--opt99", "value99"
        });
        
        assertEquals("value0", result.readOption("opt0"));
        assertEquals("value50", result.readOption("opt50"));
        assertEquals("value99", result.readOption("opt99"));
    }
}