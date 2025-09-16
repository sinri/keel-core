package io.github.sinri.keel.logger.issue.slf4j;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.recorder.adapter.KeelIssueRecorderAdapter;
import io.vertx.core.Handler;
import org.slf4j.IMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * Keel 日志系统的 SLF4J 服务提供者抽象基类。
 * <p>
 * 该类实现了 {@link SLF4JServiceProvider} 接口，为 SLF4J 日志框架提供
 * 基于 Keel 日志系统的服务提供者实现。通过该服务提供者，SLF4J 的日志
 * 调用将被路由到 Keel 的日志记录系统中。
 * <p>
 * <strong>功能特性：</strong>
 * <ul>
 *   <li>提供 {@link KeelLoggerFactory} 工厂实例用于创建日志记录器</li>
 *   <li>支持 SLF4J 2.0.17 API 版本</li>
 *   <li>不支持 SLF4J 的 Marker 和 MDC 功能</li>
 *   <li>通过适配器模式集成 Keel 日志记录中心</li>
 * </ul>
 * <p>
 * <strong>使用方式：</strong>
 * <br>
 * 子类需要实现具体的服务提供者逻辑，通常通过 Java SPI 机制自动发现和加载。
 * 在 {@code META-INF/services/org.slf4j.spi.SLF4JServiceProvider} 文件中
 * 声明具体的实现类。自版本4.1.4起，本类不再为抽象类。
 * <p>
 * <strong>限制说明：</strong>
 * <ul>
 *   <li>{@link #getMarkerFactory()} 返回 {@code null}，不支持标记功能</li>
 *   <li>{@link #getMDCAdapter()} 返回 {@code null}，不支持 MDC 功能</li>
 *   <li>建议使用 Keel 日志系统的 {@code attribute} 和 {@code context} 方法替代 MDC</li>
 * </ul>
 *
 * @author Sinri Edogawa
 * @see SLF4JServiceProvider
 * @see KeelLoggerFactory
 * @see KeelSlf4jLogger
 * @see KeelIssueRecorderAdapter
 * @since 4.1.1
 */
@TechnicalPreview(since = "4.1.1")
public class KeelSLF4JServiceProvider implements SLF4JServiceProvider {
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
        loggerFactory = new KeelLoggerFactory(getAdapterSupplier(), getIssueRecordInitializer());
    }

    /**
     * 提供用于获取 {@link KeelIssueRecorderAdapter} 实例的供应者。
     * <p>
     * 该方法返回一个 {@code Supplier}，用于延迟获取 Keel 日志系统的
     * 问题记录适配器实例。适配器实例通过 Keel 日志记录中心的
     * {@code issueRecordCenter().getAdapter()} 方法获取。
     * <p>
     * 子类可以重写此方法来提供自定义的适配器供应者实现，
     * 以满足特定的日志记录需求。
     *
     * @return 提供 {@code KeelIssueRecorderAdapter} 实例的供应者，
     *         该适配器从 Keel 日志记录中心获取
     */
    @Nonnull
    protected Supplier<KeelIssueRecorderAdapter> getAdapterSupplier() {
        return () -> Keel.getLogger().issueRecordCenter().getAdapter();
    }

    @Nullable
    protected Handler<KeelEventLog> getIssueRecordInitializer() {
        return null;
    }
}
