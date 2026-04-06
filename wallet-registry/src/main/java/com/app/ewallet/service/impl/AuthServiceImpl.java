package com.app.ewallet.service.impl;

import com.app.ewallet.config.properties.JwtProperties;
import com.app.ewallet.controller.dto.LoginRequest;
import com.app.ewallet.controller.dto.RefreshTokenRequest;
import com.app.ewallet.controller.dto.RegisterRequest;
import com.app.ewallet.controller.dto.RegisterResponse;
import com.app.ewallet.controller.dto.TokenResponse;
import com.app.ewallet.exception.EmailAlreadyExistsException;
import com.app.ewallet.exception.InvalidCredentialsException;
import com.app.ewallet.exception.InvalidRefreshTokenException;
import com.app.ewallet.exception.UserNotFoundException;
import com.app.ewallet.model.RefreshToken;
import com.app.ewallet.model.User;
import com.app.ewallet.model.Wallet;
import com.app.ewallet.repository.RefreshTokenRepository;
import com.app.ewallet.repository.UserRepository;
import com.app.ewallet.repository.WalletRepository;
import com.app.ewallet.security.JwtService;
import com.app.ewallet.security.TokenHasher;
import com.app.ewallet.service.interfaces.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        walletRepository.save(wallet);

        return new RegisterResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                "Đăng ký thành công. Vui lòng đăng nhập qua gateway để nhận access token và refresh token."
        );
    }

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return issueTokens(user);
    }

    @Override
    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        String hash = TokenHasher.sha256Hex(request.refreshToken());
        LocalDateTime now = LocalDateTime.now();
        RefreshToken old = refreshTokenRepository.findActiveByTokenHash(hash, now)
                .orElseThrow(InvalidRefreshTokenException::new);

        User user = old.getUser();
        String newPlain = newSecureRefreshToken();
        RefreshToken fresh = new RefreshToken();
        fresh.setUser(user);
        fresh.setTokenHash(TokenHasher.sha256Hex(newPlain));
        fresh.setExpiresAt(now.plusDays(jwtProperties.refreshTokenExpirationDays()));
        fresh = refreshTokenRepository.save(fresh);

        old.setRevokedAt(now);
        old.setReplacedBy(fresh);
        refreshTokenRepository.save(old);

        return buildTokenResponse(user, newPlain);
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        String hash = TokenHasher.sha256Hex(request.refreshToken());
        LocalDateTime now = LocalDateTime.now();
        RefreshToken rt = refreshTokenRepository.findActiveByTokenHash(hash, now)
                .orElse(null);
        if (rt != null) {
            rt.setRevokedAt(now);
            refreshTokenRepository.save(rt);
        }
    }

    @Override
    @Transactional
    public void logoutAllSessions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        refreshTokenRepository.revokeAllActiveForUser(user, LocalDateTime.now());
    }

    private TokenResponse issueTokens(User user) {
        String plain = newSecureRefreshToken();
        LocalDateTime now = LocalDateTime.now();
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(TokenHasher.sha256Hex(plain));
        rt.setExpiresAt(now.plusDays(jwtProperties.refreshTokenExpirationDays()));
        refreshTokenRepository.save(rt);
        return buildTokenResponse(user, plain);
    }

    private TokenResponse buildTokenResponse(User user, String plainRefresh) {
        String access = jwtService.createAccessToken(user.getId(), user.getEmail());
        long expiresIn = jwtProperties.accessTokenExpirationMinutes() * 60L;
        return new TokenResponse(access, plainRefresh, "Bearer", expiresIn);
    }

    private static String newSecureRefreshToken() {
        byte[] buf = new byte[48];
        RANDOM.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
