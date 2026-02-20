package com.shopjoy.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

/**
 * Validator for @ValidPrice annotation.
 */
public class ValidPriceValidator implements ConstraintValidator<ValidPrice, Number> {
    
    private boolean allowNull;
    
    @Override
    public void initialize(ValidPrice constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
    }
    
    @Override
    public boolean isValid(Number value, ConstraintValidatorContext context) {
        if (value == null) {
            return allowNull;
        }
        
        double price = value.doubleValue();
        
        if (price <= 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Price must be greater than 0")
                    .addConstraintViolation();
            return false;
        }
        
        BigDecimal bd = BigDecimal.valueOf(price);
        int decimalPlaces = bd.scale();
        
        if (decimalPlaces > 2) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Price must have at most 2 decimal places")
                    .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
