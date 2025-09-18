package com.example.notification.service;

import com.example.notification.dto.UserPreferenceRequest;
import com.example.notification.dto.UserPreferenceResponse;
import com.example.notification.dto.UserRequest;
import com.example.notification.entity.User;
import com.example.notification.entity.UserNotificationPreference;
import com.example.notification.enums.NotificationType;
import com.example.notification.repository.UserRepository;
import com.example.notification.repository.UserNotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserNotificationPreferenceRepository preferenceRepository;

    @Transactional
    public User createOrUpdateUser(UserRequest request) {
        User user = userRepository.findByTenantId(request.getTenantId())
                .orElse(new User());

        user.setTenantId(request.getTenantId());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setName(request.getName());

        return userRepository.save(user);
    }

    public User getUserByTenantId(String tenantId) {
        return userRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("User not found with tenantId: " + tenantId));
    }

    @Transactional
    public UserPreferenceResponse saveUserPreference(String tenantId, UserPreferenceRequest request) {
        User user = getUserByTenantId(tenantId);

        UserNotificationPreference preference = preferenceRepository
                .findByUserTenantIdAndNotificationType(tenantId, request.getNotificationType())
                .orElse(new UserNotificationPreference());

        preference.setUser(user);
        preference.setNotificationType(request.getNotificationType());
        preference.setEnabled(request.getEnabled());
        preference.setDeliveryHourStart(request.getDeliveryHourStart());
        preference.setDeliveryHourEnd(request.getDeliveryHourEnd());
        preference.setMaxFrequencyPerDay(request.getMaxFrequencyPerDay());

        preference = preferenceRepository.save(preference);

        return convertToResponse(preference);
    }

    public List<UserPreferenceResponse> getUserPreferences(String tenantId) {
        return preferenceRepository.findByUserTenantId(tenantId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public UserPreferenceResponse getUserPreference(String tenantId, NotificationType notificationType) {
        return preferenceRepository.findByUserTenantIdAndNotificationType(tenantId, notificationType)
                .map(this::convertToResponse)
                .orElse(null);
    }

    @Transactional
    public void deleteUserPreference(String tenantId, NotificationType notificationType) {
        preferenceRepository.deleteByUserTenantIdAndNotificationType(tenantId, notificationType);
    }

    private UserPreferenceResponse convertToResponse(UserNotificationPreference preference) {
        UserPreferenceResponse response = new UserPreferenceResponse();
        response.setId(preference.getId());
        response.setNotificationType(preference.getNotificationType());
        response.setEnabled(preference.getEnabled());
        response.setDeliveryHourStart(preference.getDeliveryHourStart());
        response.setDeliveryHourEnd(preference.getDeliveryHourEnd());
        response.setMaxFrequencyPerDay(preference.getMaxFrequencyPerDay());
        response.setCreatedAt(preference.getCreatedAt());
        response.setUpdatedAt(preference.getUpdatedAt());
        return response;
    }
}