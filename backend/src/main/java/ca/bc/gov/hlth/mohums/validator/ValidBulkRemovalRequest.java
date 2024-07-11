package ca.bc.gov.hlth.mohums.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BulkRemovalRequestValidator.class)
public @interface ValidBulkRemovalRequest {
    String message() default "Invalid Bulk Removal Request";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
