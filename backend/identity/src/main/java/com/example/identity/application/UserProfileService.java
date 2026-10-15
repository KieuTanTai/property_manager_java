package com.example.identity.application;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.identity.interfaces.iapplication.IUserProfileApplication;
import com.example.identity.interfaces.irepository.UserProfileRepository;
import com.example.identity.models.profile.UserProfileModel;
import com.example.identity.presentation.mapper.IdentityResponseMapper;
import com.example.identity.presentation.response.ProfileResponse;
import com.example.shared.enums.ESystemUserGender;

@Service
public class UserProfileService implements IUserProfileApplication {
    private static final Pattern PHONE = Pattern.compile("^(03|05|07|08|09)\\d{8}$");
    private final UserProfileRepository profiles;
    private final IdentityResponseMapper mapper;

    public UserProfileService(UserProfileRepository profiles, IdentityResponseMapper mapper) {
        this.profiles = profiles;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getByIdentityCode(String identityCode) {
        return mapper.profile(profiles.findById(identityCode)
                .orElseThrow(() -> new IllegalArgumentException("Profile was not found.")));
    }

    @Transactional(readOnly = true)
    public ProfileResponse getByAccountId(UUID accountId) {
        return mapper.profile(profiles.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Profile was not found.")));
    }

    @Transactional
    public ProfileResponse update(UserProfileModel profile) {
        validate(profile);
        if (profiles.findById(profile.getUserProfileId()).isEmpty()) {
            throw new IllegalArgumentException("Profile was not found.");
        }
        if (profile.getUserProfilePhoneNumber() != null && !profile.getUserProfilePhoneNumber().isBlank()) {
            profile.setUserProfilePhoneNumber(normalizePhone(profile.getUserProfilePhoneNumber()));
        }
        return mapper.profile(profiles.save(profile));
    }

    @Transactional
    public ProfileResponse create(String identityCode, UUID accountId) {
        validateIdentityCode(identityCode);
        if (accountId == null) {
            throw new IllegalArgumentException("Account id is required.");
        }
        if (profiles.findById(identityCode).isPresent() || profiles.findByAccountId(accountId).isPresent()) {
            throw new IllegalArgumentException("Profile already exists for the identity code or account.");
        }
        return mapper.profile(profiles.save(UserProfileModel.builder()
                .userProfileId(identityCode)
                .userProfileAccountId(accountId)
                .userProfileGender(ESystemUserGender.Unspecified)
                .build()));
    }

    @Transactional
    public int importProfiles(Iterable<UserProfileModel> values) {
        var profilesToImport = toList(values);
        if (profilesToImport.isEmpty()) {
            throw new IllegalArgumentException("User profiles collection cannot be empty.");
        }
        profilesToImport.forEach(profile -> validate(profile, true));
        profiles.saveAll(profilesToImport);
        return profilesToImport.size();
    }

    @Transactional
    public int updateAll(Iterable<UserProfileModel> values) {
        var profilesToUpdate = toList(values);
        if (profilesToUpdate.isEmpty()) {
            throw new IllegalArgumentException("User profiles collection cannot be empty.");
        }
        profilesToUpdate.forEach(profile -> validate(profile, true));
        profiles.updateAll(profilesToUpdate);
        return profilesToUpdate.size();
    }

    private void validate(UserProfileModel profile) {
        validate(profile, true);
    }

    private void validate(UserProfileModel profile, boolean requireAccountId) {
        if (profile == null) {
            throw new IllegalArgumentException("Profile is required.");
        }
        validateIdentityCode(profile.getUserProfileId());
        if (requireAccountId && profile.getUserProfileAccountId() == null) {
            throw new IllegalArgumentException("Account id is required.");
        }
        if (lengthExceeds(profile.getUserProfileFirstName(), 30)
                || lengthExceeds(profile.getUserProfileLastName(), 30)
                || lengthExceeds(profile.getUserProfilePhoneNumber(), 10)
                || lengthExceeds(profile.getUserProfileAddress(), 255)
                || lengthExceeds(profile.getUserProfileAvatarUrl(), 255)) {
            throw new IllegalArgumentException("One or more profile fields exceed their maximum length.");
        }
    }

    private boolean lengthExceeds(String value, int maximumLength) {
        return value != null && value.length() > maximumLength;
    }

    private void validateIdentityCode(String identityCode) {
        if (identityCode == null || identityCode.isBlank() || identityCode.length() != 12) {
            throw new IllegalArgumentException("Identity code is required and must be 12 characters.");
        }
    }

    private String normalizePhone(String phone) {
        String cleaned = phone.replaceAll("[^\\d+]", "");
        if (cleaned.startsWith("+84")) {
            cleaned = "0" + cleaned.substring(3);
        } else if (cleaned.startsWith("84") && cleaned.length() == 11) {
            cleaned = "0" + cleaned.substring(2);
        }
        if (!PHONE.matcher(cleaned).matches()) {
            throw new IllegalArgumentException("Invalid phone number format.");
        }
        return cleaned;
    }

    private <T> List<T> toList(Iterable<T> values) {
        if (values == null) {
            throw new IllegalArgumentException("Collection is required.");
        }
        return StreamSupport.stream(values.spliterator(), false).toList();
    }
}
