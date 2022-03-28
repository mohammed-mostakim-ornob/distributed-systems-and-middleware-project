package de.uniba.dsg.validation.annotation;

import de.uniba.dsg.validation.validator.LaterThanOrEqualToValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = LaterThanOrEqualToValidator.class)
@Documented
public @interface LaterThanOrEqualTo {

    String message() default "Must be later than or equal to the value";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    String year();

    String month();

    String dayOfMonth();

    @Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
    @Retention(RUNTIME)
    @Documented
    @interface List {
        LaterThanOrEqualTo[] value();
    }
}
