package com.roommatch.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
    String message() default "Usa al menos 15 caracteres; máximo 72 bytes UTF-8. Puedes usar una frase con espacios";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
