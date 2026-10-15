package com.example.identity.presentation.controller;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.identity.interfaces.iapplication.IAccountApplication;
import com.example.identity.models.account.AccountModel;
import com.example.identity.presentation.assembler.IdentityResponseAssembler;
import com.example.identity.presentation.record.AuthRequest;
import com.example.identity.presentation.record.PasswordChangeRequest;
import com.example.identity.presentation.response.AccountResponse;
import com.example.identity.presentation.response.LoginResponse;
import com.example.identity.presentation.record.AccountRequests;
import com.example.identity.presentation.response.ClaimResponse;
import com.example.shared.persistence.record.RecordBaseCursorPage;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private static final String NAME_IDENTIFIER_CLAIM =
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier";
    private static final String EMAIL_CLAIM =
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress";
    private static final String ROLE_CLAIM =
            "http://schemas.microsoft.com/ws/2008/06/identity/claims/role";

    private final IAccountApplication service;
    private final IdentityResponseAssembler assembler;

    public AccountController(IAccountApplication service, IdentityResponseAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @PostMapping("/register")
    public AccountResponse register(@Valid @RequestBody AuthRequest request) {
        return assembler.account(service.register(request.email(), request.password()));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody AuthRequest request) {
        AccountModel account = service.login(request.email(), request.password());
        AccountResponse accountResponse = assembler.account(account);
        return new LoginResponse(accountResponse, assembler.profile(account), claims(accountResponse));
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

    @RequestMapping(value = "/password/change", method = { RequestMethod.POST, RequestMethod.PUT })
    public int changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        return service.changePassword(request.email(), request.oldPassword(), request.newPassword());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam(required = false) String email,
            @Valid @RequestBody(required = false) AccountRequests.Logout request) {
        String logoutEmail = request == null ? email : request.email();
        if (logoutEmail == null || logoutEmail.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            service.logout(logoutEmail);
            return ResponseEntity.noContent().build();
        } catch (UnsupportedOperationException | IllegalArgumentException notImplemented) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/delete")
    public int deactivateSelf(
            @Valid @RequestBody AccountRequests.Inactive request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("The password and confirmation password do not match.");
        }
        return service.deactivate(request.email(), request.password());
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
                request.accountAdditionalPermissions(), request.userProfiles());
    }

    @RequestMapping(value = "/update-range", method = { RequestMethod.POST, RequestMethod.PUT })
    public int updateRange(@RequestBody List<AccountModel> accounts) {
        return service.updateAll(accounts);
    }

    @RequestMapping(value = "/active", method = { RequestMethod.POST, RequestMethod.PUT })
    public AccountResponse activate(@RequestBody String email) {
        service.activateByAdmin(email.trim());
        return assembler.account(service.findByEmail(email.trim()));
    }

    @DeleteMapping("/delete/{accountId}")
    public ResponseEntity<AccountResponse> deactivate(@PathVariable UUID accountId) {
        service.deactivateByAdmin(accountId);
        return ResponseEntity.ok(assembler.account(service.findById(accountId)));
    }

    private List<ClaimResponse> claims(AccountResponse account) {
        List<ClaimResponse> claims = new ArrayList<>();
        claims.add(new ClaimResponse(NAME_IDENTIFIER_CLAIM, account.accountId().toString()));
        claims.add(new ClaimResponse(EMAIL_CLAIM, account.email()));
        account.roleCodes().forEach(role -> claims.add(new ClaimResponse(ROLE_CLAIM, role)));
        account.permissionCodes().forEach(permission ->
                claims.add(new ClaimResponse("AdditionalPermission", permission)));
        return claims;
    }
}
