package com.example.identity.models.role;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.identity.models.permission.PermissionModel;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "role", uniqueConstraints = {
        @UniqueConstraint(name = "idx_role_code", columnNames = "role_code"),
        @UniqueConstraint(name = "idx_role_name", columnNames = "role_name")
})
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class RoleModel {
    @Id
    @Column(name = "role_id", nullable=false)
    private UUID roleId;

    @Column(name = "role_code", nullable=false, length=50)
    private String roleCode;

    @Column(name = "role_name", nullable=false, length=150)
    private String roleName;

    @Column(name = "role_description", length=300)
    private String roleDescription;

    @Column(name = "role_is_active", nullable=false)
    private Boolean roleIsActive;

    @Column(name = "role_created_at")
    private LocalDateTime roleCreatedAt = LocalDateTime.now();

    @Column(name = "role_updated_at")
    private LocalDateTime roleUpdatedAt = LocalDateTime.now();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    @Builder.Default
    private List<PermissionModel> permissions = new ArrayList<>();
}
