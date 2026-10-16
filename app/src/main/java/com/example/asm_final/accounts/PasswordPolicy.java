package com.example.asm_final.accounts;
public final class PasswordPolicy {
    private PasswordPolicy() {}
    public static boolean isValid(String password) {
        if(password==null||password.length()<8) return false;
        boolean uppercase=false,digit=false;
        for(char value:password.toCharArray()) { uppercase|=Character.isUpperCase(value);digit|=Character.isDigit(value); }
        return uppercase&&digit;
    }
}
