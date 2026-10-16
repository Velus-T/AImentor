package com.example.asm_final;
import org.junit.Test;
import static org.junit.Assert.*;
import com.example.asm_final.accounts.PasswordPolicy;
public class PasswordPolicyTest {
    @Test public void registrationAndProfileUseSameRequirements() {
        assertFalse(PasswordPolicy.isValid(null));assertFalse(PasswordPolicy.isValid("Ab1"));
        assertFalse(PasswordPolicy.isValid("abcdefgh1"));assertFalse(PasswordPolicy.isValid("Abcdefgh"));
        assertTrue(PasswordPolicy.isValid("Mentor01!2026"));
    }
}
