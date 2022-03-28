package de.uniba.dsg.validation.validator;

import de.uniba.dsg.validation.annotation.MoreThanZero;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MoreThanZeroValidator implements ConstraintValidator<MoreThanZero, Number> {
    @Override
    public void initialize(MoreThanZero constraintAnnotation) {
    }

    @Override
    public boolean isValid(Number object, ConstraintValidatorContext constraintContext) {
        if (object instanceof Integer) {
            return (int)object > 0;
        } else if (object instanceof Double) {
            return (double)object > 0;
        } else if (object instanceof Long) {
            return (long)object > 0;
        }

        return false;
    }
}
