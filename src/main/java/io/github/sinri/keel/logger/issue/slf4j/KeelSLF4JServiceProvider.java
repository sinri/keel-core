package io.github.sinri.keel.logger.issue.slf4j;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.issue.recorder.adapter.KeelIssueRecorderAdapter;
import org.slf4j.IMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

import javax.annotation.Nonnull;
import java.util.function.Supplier;


@TechnicalPreview(since = "4.1.1")
public abstract class KeelSLF4JServiceProvider implements SLF4JServiceProvider {
    /**
     * Keel 日志记录器工厂实例。
     * <p>
     * 在 {@link #initialize()} 方法中初始化，负责创建和缓存 {@link KeelSlf4jLogger} 实例。
     */
    private KeelLoggerFactory loggerFactory;

    /**
     * 获取日志记录器工厂实例。
     * <p>
     * 该方法被声明为 {@code final}，确保子类无法修改日志记录器工厂的获取逻辑。
     *
     * @return Keel 日志记录器工厂实例
     */
    @Override
    public final KeelLoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    /**
     * 获取标记工厂实例。
     * <p>
     * 当前实现不支持 SLF4J 标记功能，始终返回 {@code null}。
     * 这意味着使用 {@link org.slf4j.Marker} 相关的日志方法时，
     * 标记信息将被忽略。
     *
     * @return {@code null} - 不支持标记功能
     */
    @Override
    public IMarkerFactory getMarkerFactory() {
        return null;
    }

    /**
     * 获取映射诊断上下文 (MDC) 适配器。
     * <p>
     * 当前实现不支持 SLF4J MDC 功能，始终返回 {@code null}。
     * 这意味着 {@link org.slf4j.MDC} 相关操作将不会生效。
     * <p>
     * 建议使用 Keel 日志系统的 {@code attribute} 和 {@code context}
     * 方法来实现类似的上下文追踪功能。
     *
     * @return {@code null} - 不支持 MDC 功能
     */
    @Override
    public MDCAdapter getMDCAdapter() {
        return null;
    }

    /**
     * 获取所请求的 SLF4J API 版本。
     * <p>
     * 指定与 SLF4J 2.0.17 版本兼容，这是 Keel 日志系统
     * 当前支持和测试的 SLF4J 版本。
     *
     * @return SLF4J API 版本号 "2.0.17"
     */
    @Override
    public String getRequestedApiVersion() {
        return "2.0.17";
    }

    /**
     * 初始化 SLF4J 服务提供者。
     * <p>
     * 该方法被声明为 {@code final}，确保子类无法修改初始化逻辑。
     * 在初始化过程中，会创建一个新的 {@link KeelLoggerFactory} 实例，
     * 该工厂使用通过 {@link #getAdapterSupplier()} 方法获取的适配器提供者。
     * <p>
     * 初始化完成后，可以通过 {@link #getLoggerFactory()} 方法获取
     * 日志记录器工厂实例。
     */
    @Override
    public final void initialize() {
        loggerFactory = new KeelLoggerFactory(getAdapterSupplier());
    }

    /**
     * 获取问题记录器适配器提供者。
     * <p>
     * 这是一个抽象方法，子类必须实现以提供具体的适配器实例。
     * 适配器负责将 SLF4J 的日志调用转换为 Keel 日志系统的记录操作。
     * <p>
     * 返回的适配器将被用于创建 {@link KeelLoggerFactory} 实例，
     * 从而确保所有通过 SLF4J 创建的日志记录器都能正确地将日志
     * 路由到 Keel 日志系统。
     * <p>
     * 实现类应该确保返回的适配器不为 {@code null}，并且能够
     * 正确处理日志记录的转换和输出。
     *
     * @return 问题记录器适配器提供者，不能为 {@code null}
     */
    @Nonnull
    protected abstract Supplier<KeelIssueRecorderAdapter> getAdapterSupplier();

}
