package io.github.sinri.keel.facade.tesuto.instant;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation used on the public methods (which should return {@code Future<Void>}) of the implement class of KeelTest.
 * This annotation is intended to mark a method as an instant run unit, which will be executed by the
 * {@link KeelInstantRunner}.
 *
 * @see KeelInstantRunner
 * @since 3.0.10
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface InstantRunUnit {
    /**
     * If it is set to true, the test method would be skipped.
     *
     * @see InstantRunUnitSkipped
     * @since 3.0.14
     * @deprecated Since 4.0.0 use Annotation {@code InstantRunUnitSkipped} instead.
     */
    @Deprecated(since = "4.0.0")
    boolean skip() default false;
}
