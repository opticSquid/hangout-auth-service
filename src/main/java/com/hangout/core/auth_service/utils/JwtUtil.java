package com.hangout.core.auth_service.utils;

import java.util.Date;

public interface JwtUtil {
    String generateToken(String username);

    Boolean validateToken(String token);

    Date getExpiresAt(String token);

    String getUsername(String token);
}
