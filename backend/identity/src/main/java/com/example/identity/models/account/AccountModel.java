package com.example.identity.models.account;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.identity.models.permission.PermissionModel;
import com.example.identity.models.profile.UserProfileModel;
import com.example.identity.models.role.RoleModel;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "account", uniqueConstraints = @UniqueConstraint(name = "idx_account_email", columnNames = "account_email"))
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class AccountModel {
    @Id 
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "account_email", nullable = false, length = 255)
    private String accountEmail;

    @Column(name = "account_password", nullable = false, length = 255)
    private String accountPassword;

    /** Stores the stable Google subject from a verified ID token, not the short-lived JWT itself. */
    @Column(name = "account_oauth_credential_token", columnDefinition = "TEXT")
    private String accountOauthCredentialToken;

    @Column(name = "account_created_at")
    private LocalDateTime accountCreatedAt = LocalDateTime.now();

    @Column(name = "account_updated_at")
    private LocalDateTime accountUpdatedAt = LocalDateTime.now();

    @Column(name = "account_is_active", nullable = false)
    private Boolean accountIsActive;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "account_role",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private List<RoleModel> roles = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "account_additional_permission",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    @Builder.Default
    private List<PermissionModel> additionalPermissions = new ArrayList<>();

    @OneToOne(mappedBy = "account")
    private UserProfileModel userProfile;
}
