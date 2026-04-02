package com.app.ewallet.service.interfaces;

import com.app.ewallet.controller.dto.LoginRequest;
import com.app.ewallet.controller.dto.RefreshTokenRequest;
import com.app.ewallet.controller.dto.RegisterRequest;
import com.app.ewallet.controller.dto.RegisterResponse;
import com.app.ewallet.controller.dto.TokenResponse;

public interface IAuthService {

    RegisterResponse register(RegisterRequest request);

    TokenResponse login(LoginRequest request);

    TokenResponse refresh(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    void logoutAllSessions(Long userId);
}
