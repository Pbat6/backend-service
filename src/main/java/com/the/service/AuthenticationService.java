package com.the.service;

import com.the.dto.request.ResetPasswordDTO;
import com.the.dto.request.SignInRequest;
import com.the.dto.response.SignInResponse;
import com.the.dto.response.TokenResponse;
import com.the.exception.InvalidDataException;
import com.the.model.RedisToken;
import com.the.model.User;
import com.the.repository.UserRepository;
import com.the.util.TokenType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RedisTokenService redisTokenService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UserService userService;

    /**
     * Sign in the system
     * @param request
     * @return
     */
    public SignInResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        var accessToken = jwtService.generateToken(user);
        return SignInResponse.builder()
                .accessToken(accessToken)
                .refreshToken("refresh_token")
                .userId(1L)
                .phoneNumber("phoneNumber")
                .role("ROLE_USER")
                .build();
    }



    public TokenResponse accessToken(SignInRequest signInRequest) {
        log.info("---------- accessToken ----------");

        var user = userService.getByUsername(signInRequest.getUsername());
        if (!user.isEnabled()) {
            throw new InvalidDataException("User not active");
        }

        List<String> roles = userService.getAllRolesByUserId(user.getId());
        List<SimpleGrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new).toList();

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword(), authorities));

        // create new access token
        String accessToken = jwtService.generateToken(user);

        // create new refresh token
        String refreshToken = jwtService.generateRefreshToken(user);

        // save token to db
        // tokenService.save(Token.builder().username(user.getUsername()).accessToken(accessToken).refreshToken(refreshToken).build());
        redisTokenService.save(RedisToken.builder().id(user.getUsername()).accessToken(accessToken).refreshToken(refreshToken).build());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
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

        final String refreshToken = request.getHeader(HttpHeaders.REFERER);
        if (StringUtils.isBlank(refreshToken)) {
            throw new InvalidDataException("Token must be not blank");
        }
        final String userName = jwtService.extractUsername(refreshToken, TokenType.REFRESH_TOKEN);
        var user = userService.getByUsername(userName);
        if (!jwtService.isValid(refreshToken, TokenType.REFRESH_TOKEN, user)) {
            throw new InvalidDataException("Not allow access with this token");
        }

        // create new access token
        String accessToken = jwtService.generateToken(user);

        // save token to db
        // tokenService.save(Token.builder().username(user.getUsername()).accessToken(accessToken).refreshToken(refreshToken).build());
        redisTokenService.save(RedisToken.builder().id(user.getUsername()).accessToken(accessToken).refreshToken(refreshToken).build());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .build();
    }

    /**
     * Logout
     *
     * @param request
     * @return
     */
    public String removeToken(HttpServletRequest request) {
        log.info("---------- removeToken ----------");

        final String token = request.getHeader(HttpHeaders.REFERER);
        if (StringUtils.isBlank(token)) {
            throw new InvalidDataException("Token must be not blank");
        }

        final String userName = jwtService.extractUsername(token, TokenType.ACCESS_TOKEN);

        // tokenService.delete(userName);
        redisTokenService.remove(userName);

        return "Removed!";
    }

    /**
     * Forgot password
     *
     * @param email
     */
    public String forgotPassword(String email) {
        log.info("---------- forgotPassword ----------");

        // check email exists or not
        User user = userService.getUserByEmail(email);

        // generate reset token
        String resetToken = jwtService.generateResetToken(user);

        // save to db
        // tokenService.save(Token.builder().username(user.getUsername()).resetToken(resetToken).build());
        redisTokenService.save(RedisToken.builder().id(user.getUsername()).resetToken(resetToken).build());

        // TODO send email to user
        String confirmLink = String.format("curl --location 'http://localhost:80/auth/reset-password' \\\n" +
                "--header 'accept: */*' \\\n" +
                "--header 'Content-Type: application/json' \\\n" +
                "--data '%s'", resetToken);
        log.info("--> confirmLink: {}", confirmLink);

        return resetToken;
    }

    /**
     * Reset password
     *
     * @param secretKey
     * @return
     */
    public String resetPassword(String secretKey) {
        log.info("---------- resetPassword ----------");

        // validate token
        var user = validateToken(secretKey);

        // check token by username
        tokenService.getByUsername(user.getUsername());

        return "Reset";
    }

    public String changePassword(ResetPasswordDTO request) {
        log.info("---------- changePassword ----------");

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidDataException("Passwords do not match");
        }

        // get user by reset token
        var user = validateToken(request.getSecretKey());

        // update password
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userService.saveUser(user);

        return "Changed";
    }

    /**
     * Validate user and reset token
     *
     * @param token
     * @return
     */
    private User validateToken(String token) {
        // validate token
        var userName = jwtService.extractUsername(token, TokenType.RESET_TOKEN);

        // check token in redis
        redisTokenService.isExists(userName);

        // validate user is active or not
        var user = userService.getByUsername(userName);
        if (!user.isEnabled()) {
            throw new InvalidDataException("User not active");
        }

        return user;
    }
}
