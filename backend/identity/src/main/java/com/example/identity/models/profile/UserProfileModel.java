package com.example.identity.models.profile;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;


import com.example.identity.models.account.AccountModel;
import com.example.shared.enums.ESystemUserGender;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "user_profile", uniqueConstraints = @UniqueConstraint(
        name = "idx_user_profile_account_id", columnNames = "user_profile_account_id"))
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class UserProfileModel {
    @Id
    @Column(name = "user_profile_id", nullable=false, length=12)
    private String userProfileId;

    @Column(name = "user_profile_account_id", nullable=false)
    private UUID userProfileAccountId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_account_id", referencedColumnName = "account_id",
            insertable = false, updatable = false)
    private AccountModel account;

    @Column(name = "user_profile_first_name", length=30)
    private String userProfileFirstName;

    @Column(name = "user_profile_last_name", length=30)
    private String userProfileLastName;

    @Column(name = "user_profile_date_of_birth")
    private Date userProfileDateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_profile_gender", nullable=false, length=20)
    private ESystemUserGender userProfileGender;

    @Column(name = "user_profile_phone_number", length=10)
    private String userProfilePhoneNumber;

    @Column(name = "user_profile_address", length=255)
    private String userProfileAddress;

    @Column(name = "user_profile_avatar_url", length=255)
    private String userProfileAvatarUrl;

    @Column(name = "user_profile_created_at")
    private LocalDateTime userProfileCreatedAt;

    @Column(name = "user_profile_updated_at")
    private LocalDateTime userProfileUpdatedAt;
}
