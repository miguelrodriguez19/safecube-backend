package com.miguelrodriguez19.safecube.user.application.mapper;

import com.miguelrodriguez19.safecube.user.application.dto.UserProfileResponse;
import com.miguelrodriguez19.safecube.user.domain.model.UserProfile;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

@Component
public class UserProfileResponseMapper {

  public UserProfileResponse mapResponse(@NotNull final UserProfile profile) {
    return new UserProfileResponse(
        profile.getUserId(),
        profile.getAccountId(),
        profile.getDisplayName(),
        profile.getCreatedAt(),
        profile.getUpdatedAt());
  }
}
