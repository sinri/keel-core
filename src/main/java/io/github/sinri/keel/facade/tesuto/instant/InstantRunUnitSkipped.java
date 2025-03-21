package io.github.sinri.keel.facade.tesuto.instant;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to mark a method as skipped in the context of instant run units.
 * When this annotation is present on a method, it indicates that the method should be skipped during the execution
 * by the {@link KeelInstantRunner}.
 *
 * @see InstantRunUnit
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface InstantRunUnitSkipped {
}
