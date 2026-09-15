package com.roommatch.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
    private static final Set<String> COMMON = Set.of("passwordpassword", "123456789012345", "1234567890123456", "qwertyuiopasdfgh");
    public static boolean valid(String value) {
        return value != null && !value.isBlank() && value.codePointCount(0, value.length()) >= 15
                && value.getBytes(StandardCharsets.UTF_8).length <= 72
                && value.codePoints().distinct().count() > 1
                && !COMMON.contains(value.toLowerCase(Locale.ROOT));
    }
    @Override public boolean isValid(String value, ConstraintValidatorContext context) { return valid(value); }
}
