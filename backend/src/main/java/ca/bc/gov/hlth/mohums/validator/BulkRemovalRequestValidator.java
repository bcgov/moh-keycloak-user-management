package ca.bc.gov.hlth.mohums.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;

public class BulkRemovalRequestValidator implements ConstraintValidator<ValidBulkRemovalRequest, Map<String, List<Object>>> {

    private String message;

    @Override
    public void initialize(ValidBulkRemovalRequest constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Map<String, List<Object>> rolesForRemoval, ConstraintValidatorContext context) {
        if (rolesForRemoval.isEmpty()) {
            message = "Bulk Removal Request cannot be empty";
            buildConstraintValidatorContext(context);
            return false;
        }

        for (Map.Entry<String, List<Object>> entry : rolesForRemoval.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isEmpty()) {
                message = "User ID cannot be null or empty";
                buildConstraintValidatorContext(context);
                return false;
            }
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                message = "List of roles to remove cannot be null or empty";
                buildConstraintValidatorContext(context);
                return false;
            }
        }
        return true;
    }

    private void buildConstraintValidatorContext(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
