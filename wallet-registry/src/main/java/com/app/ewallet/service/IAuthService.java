package com.app.ewallet.service;

import com.app.ewallet.controller.dto.LoginRequest;
import com.app.ewallet.controller.dto.RefreshTokenRequest;
import com.app.ewallet.controller.dto.RegisterRequest;
import com.app.ewallet.controller.dto.TokenResponse;
public interface IAuthService {

    TokenResponse register(RegisterRequest request);

    TokenResponse login(LoginRequest request);

    TokenResponse refresh(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    void logoutAllSessions(Long userId);
}
