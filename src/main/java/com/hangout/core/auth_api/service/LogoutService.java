package com.hangout.core.auth_api.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.hangout.core.auth_api.dto.request.DeviceDetails;
import com.hangout.core.auth_api.dto.response.DefaultResponse;
import com.hangout.core.auth_api.entity.AccessRecord;
import com.hangout.core.auth_api.entity.Action;
import com.hangout.core.auth_api.entity.Device;
import com.hangout.core.auth_api.entity.User;
import com.hangout.core.auth_api.exceptions.UnauthorizedAccessException;
import com.hangout.core.auth_api.exceptions.UnIndentifiedDeviceException;
import com.hangout.core.auth_api.repository.AccessRecordRepo;
import com.hangout.core.auth_api.repository.DeviceRepo;
import com.hangout.core.auth_api.repository.UserRepo;
import com.hangout.core.auth_api.utils.DeviceUtil;
import com.hangout.core.auth_api.utils.JwtUtil;

import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
class LogoutService {
    @Autowired
    @Qualifier("accessTokenUtil")
    private JwtUtil accessTokenUtil;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private AccessRecordRepo accessRecordRepo;
    @Autowired
    private DeviceUtil deviceUtil;
    @Autowired
    private DeviceRepo deviceRepo;

    @Observed(name = "logout", contextualName = "service")
    public DefaultResponse logout(String accessToken, DeviceDetails deviceDetails) {
        String userName = this.accessTokenUtil.getUsername(accessToken);
        UUID deviceId = this.accessTokenUtil.getDeviceId(accessToken);
        User user = this.userRepo.findByUserName(userName).get();
        Device device = checkIfTheDeviceIsSameAsUsedForLogin(deviceId, deviceDetails, user);
        Optional<AccessRecord> access = this.accessRecordRepo.getLatestAccessRecord(user.getUserId(), deviceId);
        if (access.isPresent() && access.get().getUserAction() != Action.LOGOUT) {
            this.accessRecordRepo.save(new AccessRecord(access.get().getAccessToken(),
                    access.get().getAccessTokenExpiryTime(), access.get().getRefreshToken(),
                    access.get().getRefreshTokenExpiryTime(), Action.LOGOUT, device, user));
            return new DefaultResponse("Successfully Logged out from this device");
        } else {
            throw new UnauthorizedAccessException("There is no active sesion in the current device");
        }
    }

    private Device checkIfTheDeviceIsSameAsUsedForLogin(UUID incomingDeviceId, DeviceDetails incomingDeviceDetails,
            User user) {
        Device currentDevice = this.deviceUtil.getDevice(incomingDeviceDetails, user);
        Optional<Device> deviceFromDb = this.deviceRepo.findById(incomingDeviceId);
        log.debug("device similarity:{}", this.deviceUtil.calculateDeviceSimilarity(currentDevice, deviceFromDb.get()));
        if (deviceFromDb.isPresent()
                && this.deviceUtil.calculateDeviceSimilarity(currentDevice, deviceFromDb.get()) >= 70.0) {
            return deviceFromDb.get();
        } else {
            throw new UnIndentifiedDeviceException("Device being used is different from what was used to login");
        }
    }
}
