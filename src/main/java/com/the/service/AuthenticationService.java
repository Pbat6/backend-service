package com.the.service;

import com.the.dto.request.ChangePasswordDTO;
import com.the.dto.request.ResetPasswordDTO;
import com.the.dto.request.ResetPasswordEvent;
import com.the.dto.request.SignInDTO;
import com.the.dto.response.TokenResponse;
import com.the.exception.BadCredentialsException;
import com.the.exception.InvalidDataException;
import com.the.model.RedisToken;
import com.the.model.User;
import com.the.repository.UserRepository;
import com.the.util.CookieUtil;
import com.the.util.TokenType;
import com.the.util.UserStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RedisTokenService redisTokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final CookieUtil cookieUtil;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${jwt.expiryMinute}")
    private Integer expiryMinute;

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    public TokenResponse signIn(SignInDTO signInRequest, HttpServletResponse response) {
        log.info("---------- accessToken ----------");
        User user = userService.getByUsername(signInRequest.getUsername());
        if (!user.isEnabled()) {
            throw new InvalidDataException("User not active");
        }
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword()));
        // create new access token
        String accessToken = jwtService.generateAccessToken(user);
        // create new refresh token
        String refreshToken = jwtService.generateRefreshToken(user);
        redisTokenService.save(RedisToken.builder().id(user.getUsername()).refreshToken(refreshToken).build());
        cookieUtil.create(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, expiryMinute);
        return TokenResponse.builder()
                .accessToken(accessToken)
                .userId(user.getId())
                .roles(user.getRoles().stream().map(uhr -> uhr.getRole().getName()).toList())
                .build();
    }

    /**
     * Refresh token
     *
     * @param request
     * @return
     */
    public TokenResponse refreshToken(HttpServletRequest request) {
        log.info("---------- refreshToken ----------");
        User user = validateAndGetUserFromRefreshToken(request);
        // create new access token
        String accessToken = jwtService.generateAccessToken(user);
        return TokenResponse.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .roles(user.getRoles().stream().map(uhr -> uhr.getRole().getName()).toList())
                .build();
    }

    /**
     * Logout
     *
     * @param request
     * @return
     */
    public void removeToken(HttpServletRequest request, HttpServletResponse response) {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            User user = validateAndGetUserFromRefreshToken(request);
            redisTokenService.remove(user.getUsername());
            cookieUtil.clear(response, REFRESH_TOKEN_COOKIE_NAME);
        }else{
            final String accessToken = authHeader.substring(7);

            try {
                Date expirationDate = jwtService.extractExpiration(accessToken, TokenType.ACCESS_TOKEN);

                // 2. Tính thời gian còn lại của token
                long remainingTime = expirationDate.getTime() - System.currentTimeMillis();

                // 3. Thêm token vào blacklist
                redisTokenService.blacklistToken(accessToken, remainingTime);

                User user = validateAndGetUserFromRefreshToken(request);
                redisTokenService.remove(user.getUsername());
                cookieUtil.clear(response, REFRESH_TOKEN_COOKIE_NAME);

            } catch (Exception e) {
                // Có thể token đã hết hạn hoặc không hợp lệ, không cần làm gì thêm
                log.error("Error while blacklisting token: {}", e.getMessage());
            }
        }
    }

    /**
     * Forgot password
     *
     * @param email
     */
    public void forgotPassword(String email) {
        try{
            log.info("---------- forgotPassword ----------");
            // check email exists or not
            User user = userService.getUserByEmail(email);
            // generate reset token
            String resetToken = jwtService.generateResetToken(user);
            redisTokenService.save(RedisToken.builder().id(user.getUsername()).resetToken(resetToken).build());
            ResetPasswordEvent event = new ResetPasswordEvent(user.getEmail(), user.getUsername(), resetToken, "reset");
            kafkaTemplate.send("reset-password-topic", event);
        }catch (Exception e){
            log.error("Exception occurred in forgotPassword for email {}", email, e);
        }
    }

    /**
     * Reset password
     *
     * @param resetPasswordDTO
     * @return
     */
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        log.info("---------- resetPassword ----------");
        if (!resetPasswordDTO.getPassword().equals(resetPasswordDTO.getConfirmPassword())) {
            throw new InvalidDataException("Passwords do not match. Please ensure both password fields are identical.");
        }
        String resetToken = resetPasswordDTO.getResetToken();
        String username = jwtService.extractUsername(resetToken, TokenType.RESET_TOKEN);
        jwtService.isTokenExpired(resetToken, TokenType.RESET_TOKEN);
        String tokenInRedis = redisTokenService.get(username).getResetToken();
        if (StringUtils.isEmpty(tokenInRedis) || !tokenInRedis.equals(resetToken)) {
            throw new InvalidDataException("Invalid or expired reset token. Please request a new one.");
        }
        User user = userService.getByUsername(username);
        if (!user.isEnabled()) {
            user.setStatus(UserStatus.ACTIVE);
        }
        user.setPassword(passwordEncoder.encode(resetPasswordDTO.getPassword()));
        userRepository.save(user);
        // 6. Xóa token khỏi Redis sau khi đã sử dụng xong
        redisTokenService.remove(username);
        log.info("Password has been successfully reset for user: {}", username);
    }

    public void changePassword(ChangePasswordDTO request) {
        log.info("---------- changePassword ----------");
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new InvalidDataException("New password and confirmation password do not match.");
        }
        // Lấy thông tin người dùng đang được xác thực từ Security Context
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getByUsername(username);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Incorrect old password.");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

    }

    private User validateAndGetUserFromRefreshToken(HttpServletRequest request) {
        String refreshToken = cookieUtil.get(request, REFRESH_TOKEN_COOKIE_NAME).orElseThrow(() -> new InvalidDataException("Refresh token not found or invalid"));
        String username = jwtService.extractUsername(refreshToken, TokenType.REFRESH_TOKEN);
        jwtService.isTokenExpired(refreshToken, TokenType.REFRESH_TOKEN);
        String tokenInRedis = redisTokenService.get(username).getRefreshToken();
        if (!tokenInRedis.equals(refreshToken)) {
            throw new InvalidDataException("Invalid Refresh Token");
        }
        return userService.getByUsername(username);
    }
}


