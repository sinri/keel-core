package io.github.sinri.keel.facade.tesuto.instant;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @see KeelInstantRunner
 * @since 3.0.10
 * Annotation used on the public methods (which should return {@code Future<Void>}) of the implement class of KeelTest.
 * @since 3.0.14 add skip.
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
