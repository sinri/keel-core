package io.github.sinri.keel.core.cutter;

/**
 * 切分处理器总处理时间超时异常。
 *
 * @since 5.0.0
 */
public final class CutterTimeout extends Exception {
    public CutterTimeout() {
        super("This IntravenouslyCutter instance met timeout");
    }
}
