package io.lumine.mythic.lib.util.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface BackwardsCompatibility {

    /**
     * Version at which this field was implemented. This corresponds to
     * the first release version that would break if the annotated feature
     * were not implemented.
     */
    String version();

    String reason() default "";
}
