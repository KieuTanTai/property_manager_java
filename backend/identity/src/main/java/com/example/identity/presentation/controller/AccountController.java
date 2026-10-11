package com.example.identity.presentation.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.identity.interfaces.iapplication.IAccountApplication;
import com.example.identity.interfaces.iapplication.IUserProfileApplication;
import com.example.identity.models.account.AccountModel;
import com.example.identity.presentation.assembler.IdentityResponseAssembler;
import com.example.identity.presentation.record.AuthRequest;
import com.example.identity.presentation.record.PasswordChangeRequest;
import com.example.identity.presentation.response.AccountResponse;
import com.example.identity.presentation.response.LoginResponse;
import com.example.identity.presentation.response.ProfileResponse;
import com.example.identity.presentation.record.AccountRequests;
import com.example.shared.persistence.record.RecordBaseCursorPage;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final IAccountApplication service;
    private final IUserProfileApplication profiles;
    private final IdentityResponseAssembler assembler;

    public AccountController(IAccountApplication service, IUserProfileApplication profiles,
            IdentityResponseAssembler assembler) {
        this.service = service;
        this.profiles = profiles;
        this.assembler = assembler;
    }

    @PostMapping("/register")
    public AccountResponse register(@Valid @RequestBody AuthRequest request) {
        return assembler.account(service.register(request.email(), request.password()));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody AuthRequest request) {
        AccountModel account = service.login(request.email(), request.password());
        ProfileResponse profile = null;
        try {
            profile = profiles.getByAccountId(account.getAccountId());

        } catch (IllegalArgumentException ignored) {
            // An account can exist before its base profile is created.
        }
        return new LoginResponse(assembler.account(account), profile);
    }

    @GetMapping("/email")
    public AccountResponse byEmail(@RequestParam String email) {
        return assembler.account(service.findByEmail(email));
    }

    @GetMapping("/status")
    public RecordBaseCursorPage<AccountResponse> byStatus(@RequestParam boolean isActive,
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "5") int pageSize) {
        var page = service.findPageByStatus(cursor, pageSize, isActive);
        return new RecordBaseCursorPage<>(page.items().stream().map(assembler::account).toList(),
                page.nextCursor(), page.hasMore());
    }

    @PostMapping("/password/change")
    public AccountResponse changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        return assembler.account(service.changePassword(request.email(), request.oldPassword(), request.newPassword()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody AccountRequests.Logout request) {
        service.logout(request.email());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<AccountResponse> deactivateSelf(
            @Valid @RequestBody AccountRequests.Inactive request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("The password and confirmation password do not match.");
        }
        service.deactivate(request.email(), request.password());
        return ResponseEntity.ok(assembler.account(service.findByEmail(request.email())));
    }

    @GetMapping("/analytics")
    public RecordBaseCursorPage<AccountResponse> analytics(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(defaultValue = "true") boolean isGetProfile) {
        var page = service.findPage(cursor, pageSize, isGetProfile);
        return new RecordBaseCursorPage<>(page.items().stream().map(assembler::account).toList(),
                page.nextCursor(), page.hasMore());
    }

    @PostMapping("/import")
    public int importAccounts(@RequestBody AccountRequests.Import request) {
        return service.importAccounts(request.accounts(), request.accountRoles(),
                request.additionalPermissions(), request.profiles());
    }

    @PostMapping("/update-range")
    public int updateRange(@RequestBody AccountRequests.UpdateRange request) {
        return service.updateAll(request.accounts());
    }

    @PostMapping("/active")
    public AccountResponse activate(@RequestBody String email) {
        service.activateByAdmin(email.trim());
        return assembler.account(service.findByEmail(email.trim()));
    }

    @DeleteMapping("/delete/{accountId}")
    public ResponseEntity<AccountResponse> deactivate(@PathVariable UUID accountId) {
        service.deactivateByAdmin(accountId);
        return ResponseEntity.ok(assembler.account(service.findById(accountId)));
    }
}
