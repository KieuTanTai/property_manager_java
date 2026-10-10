package com.example.identity.presentation.controller;

import java.util.Map;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.identity.interfaces.iapplication.IAccountApplication;
import com.example.identity.models.account.AccountModel;
import com.example.identity.presentation.record.AuthRequest;
import com.example.identity.presentation.record.PasswordChangeRequest;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final IAccountApplication service;

    public AccountController(IAccountApplication service) {
        this.service = service;
    }

    @PostMapping("/register")
    public AccountModel register(@Valid @RequestBody AuthRequest request) {
        return service.register(request.email(), request.password());
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody AuthRequest request) {
        AccountModel account = service.login(request.email(), request.password());
        return Map.of("accountId", account.getAccountId(), "email", account.getAccountEmail(),
                "isActive", account.getAccountIsActive());
    }

    @GetMapping("/email")
    public AccountModel byEmail(@RequestParam String email) {
        return service.findByEmail(email);
    }

    @GetMapping("/status")
    public Page<AccountModel> byStatus(@RequestParam boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int pageSize) {
        return service.findByStatus(isActive, page, pageSize);
    }

    @PostMapping("/password/change")
    public AccountModel changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        return service.changePassword(request.email(), request.oldPassword(), request.newPassword());
    }

    @PostMapping("/active")
    public AccountModel activate(@RequestBody String email) {
        return service.setActive(service.findByEmail(email.trim()).getAccountId(), true);
    }

    @DeleteMapping("/delete/{accountId}")
    public ResponseEntity<AccountModel> deactivate(@PathVariable UUID accountId) {
        return ResponseEntity.ok(service.setActive(accountId, false));
    }
}
