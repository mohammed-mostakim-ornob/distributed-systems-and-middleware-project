package de.uniba.dsg.validation.validator;

import de.uniba.dsg.validation.annotation.InPast;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class InPastValidator implements ConstraintValidator<InPast, LocalDate> {

    @Override
    public void initialize(InPast constraintAnnotation) { }

    @Override
    public boolean isValid(LocalDate object, ConstraintValidatorContext constraintContext) {
        return object != null && object.isBefore(LocalDate.now());
    }
}
