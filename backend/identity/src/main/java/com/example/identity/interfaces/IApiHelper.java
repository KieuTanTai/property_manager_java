package com.example.identity.interfaces;

import com.example.identity.models.account.AccountModel;

import java.util.Map;


public interface IApiHelper {
    Map<String, Object> toAccountResponse(AccountModel account);
}
