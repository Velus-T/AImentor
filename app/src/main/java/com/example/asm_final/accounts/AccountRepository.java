package com.example.asm_final.accounts;
/** Aggregate used only by the composition root; screens consume narrow interfaces. */
public interface AccountRepository extends UserAuthenticator, UserRegistration, AccountLookup {}
