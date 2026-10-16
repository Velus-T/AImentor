package com.example.asm_final.accounts;
public interface UserProfileRepository {
    UserProfile load(String username);
    boolean update(String username,UserProfile profile,String newPassword);
}
