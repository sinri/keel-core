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
}
