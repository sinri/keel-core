package io.github.sinri.keel.facade.async;

/**
 * KeelAsyncMixin is a mixin interface that provides utility methods for
 * performing asynchronous operations in a more
 * structured and convenient manner. It includes methods for processing
 * collections and iterators, handling repeated
 * and iterative tasks, and managing exclusive access to resources.
 *
 * @since 4.0.0
 */
public interface KeelAsyncMixin extends KeelAsyncMixinParallel, KeelAsyncMixinLogic, KeelAsyncMixinLock, KeelAsyncMixinBlock {

}
