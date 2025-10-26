package com.example.authservice.service;

import com.example.authservice.exception.*;
import com.example.authservice.kafka.KafkaAuthProducer;
import com.example.authservice.kafka.UserRegisteredEventDTO;
import com.example.authservice.mapper.UserMapper;
import com.example.authservice.model.dto.*;
import com.example.authservice.model.entity.ConfirmationToken;
import com.example.authservice.model.entity.RefreshToken;
import com.example.authservice.model.entity.User;
import com.example.authservice.model.enumeration.Role;
import com.example.authservice.repository.ConfirmationTokenRepository;
import com.example.authservice.repository.RefreshTokenRepository;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.jwt.JwtUtils;
import com.example.authservice.security.services.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;
    private final ConfirmationTokenRepository confirmationTokenRepository;

    @Value("${otp.expiration-seconds}")
    private long otpExpirationSeconds;
    private final KafkaAuthProducer kafkaProducer;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper,
                       AuthenticationManager authenticationManager, JwtUtils jwtUtils,
                       RefreshTokenRepository refreshTokenRepository, EmailService emailService,
                       ConfirmationTokenRepository confirmationTokenRepository, KafkaAuthProducer kafkaProducer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.refreshTokenRepository = refreshTokenRepository;
        this.emailService = emailService;
        this.confirmationTokenRepository = confirmationTokenRepository;
        this.kafkaProducer = kafkaProducer;
    }

    // --- Qeydiyyat ---

    @Transactional
    public User registerUser(SignupRequest signupRequest) {
        if (userRepository.existsByFin(signupRequest.getFin())) {
            throw new UserAlreadyExistsException("FIN", signupRequest.getFin());
        }
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new UserAlreadyExistsException("Username", signupRequest.getUsername());
        }
        if (signupRequest.getEmail() != null && !signupRequest.getEmail().trim().isEmpty() &&
                userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email", signupRequest.getEmail());
        }
        if (signupRequest.getPhone() != null && !signupRequest.getPhone().trim().isEmpty() &&
                userRepository.existsByPhone(signupRequest.getPhone())) {
            throw new UserAlreadyExistsException("Phone", signupRequest.getPhone());
        }

        User user = userMapper.toEntity(signupRequest);
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setRole(Role.USER);
        user.setEnabled(false);

        User savedUser = userRepository.save(user);

        UserRegisteredEventDTO event = new UserRegisteredEventDTO(
                savedUser.getId().toString(),
                savedUser.getEmail(),
                UUID.randomUUID().toString(),
                java.time.LocalDateTime.now()
        );

        kafkaProducer.sendUserRegistrationEvent(event);
        log.info("Kafka user registration event sent for user: {}", savedUser.getUsername());

        return savedUser;
    }

    // --- OTP Prosesləri ---

    @Transactional
    public void sendOtp(OtpSendRequest otpSendRequest) {
        User user = findUserByIdentifier(otpSendRequest.getIdentifier());

        String otpType = otpSendRequest.getOtpType().toUpperCase();
        String purpose = "";

        if (otpType.equalsIgnoreCase("ACCOUNT_CONFIRMATION")) {
            if (user.isEnabled()) {
                throw new OtpException("Account already verified.");
            }
            purpose = "account verification";
        } else if (otpType.equalsIgnoreCase("PASSWORD_RESET")) {
            purpose = "password reset";
        } else {
            throw new OtpException("Invalid OTP type: " + otpType);
        }

        confirmationTokenRepository.deleteAllByUserIdAndType(user.getId(), otpType);

        String otpCode = generateOtpCode();
        ConfirmationToken confirmationToken = new ConfirmationToken(user, otpCode, otpType, otpExpirationSeconds);
        confirmationTokenRepository.save(confirmationToken);

        String sendMethod = otpSendRequest.getSendMethod().toLowerCase();

        switch (sendMethod) {
            case "email":
                if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                    throw new OtpException("No registered email address found for this user.");
                }
                String emailBody = "Hello " + user.getActualUsername() + ",<br><br>"
                        + "Your verification code for " + purpose + " is: <h1>" + otpCode + "</h1><br>"
                        + "This code is valid for " + (otpExpirationSeconds / 60) + " minutes.<br><br>"
                        + "Best regards,<br>"
                        + "AuthService Team";
                emailService.sendEmail(user.getEmail(), "Verification Code - AuthService", emailBody);
                log.info("Email OTP sent to {}: {}", user.getEmail(), otpCode);
                break;
            default:
                throw new OtpException("Invalid sending method. Must be 'email'.");
        }

    }

    @Transactional
    public void verifyOtp(OtpVerificationRequest otpVerificationRequest) {
        User user = findUserByIdentifier(otpVerificationRequest.getIdentifier());

        ConfirmationToken confirmationToken = confirmationTokenRepository
                .findByTokenAndTypeAndUsedFalseAndExpiresAtAfter(
                        otpVerificationRequest.getOtpCode(),
                        otpVerificationRequest.getOtpType().toUpperCase(),
                        Instant.now())
                .orElseThrow(() -> new OtpException("Invalid, used, or expired OTP code."));

        if (!Objects.equals(confirmationToken.getUser().getId(), user.getId())) {
            throw new OtpException("This OTP code belongs to another user.");
        }

        if (otpVerificationRequest.getOtpType().equalsIgnoreCase("ACCOUNT_CONFIRMATION")) {
            user.setEnabled(true);
            userRepository.save(user);
            confirmationToken.setConfirmedAt(Instant.now());
            confirmationToken.setUsed(true);
            confirmationTokenRepository.save(confirmationToken);
            log.info("Account confirmation successful for user: {}", user.getFin());
        } else if (otpVerificationRequest.getOtpType().equalsIgnoreCase("PASSWORD_RESET")) {
            confirmationToken.setConfirmedAt(Instant.now());
            confirmationTokenRepository.save(confirmationToken);
            log.info("Password Reset OTP verified for user: {}", user.getFin());
        } else {
            throw new OtpException("Invalid OTP type.");
        }
    }

    @Transactional
    public UserResponseDTO resetPassword(ResetPasswordRequest request) {
        log.info("Attempting to reset password for identifier: {}", request.getIdentifier());

        User user = userRepository.findByEmail(request.getIdentifier())
                .orElseGet(() -> userRepository.findByUsername(request.getIdentifier())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with identifier: " + request.getIdentifier())));

        ConfirmationToken verifiedToken = confirmationTokenRepository
                .findByTokenAndTypeAndUsedFalseAndExpiresAtAfter(
                        request.getOtpCode(),
                        "PASSWORD_RESET",
                        Instant.now())
                .orElseThrow(() -> new OtpException("Password reset authorization is invalid, expired, or has already been used."));

        if (!Objects.equals(verifiedToken.getUser().getId(), user.getId())) {
            throw new OtpException("This OTP code belongs to another user.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password for user '{}' reset successfully.", user.getUsername());

        confirmationTokenRepository.delete(verifiedToken);

        jwtUtils.deleteByUserId(user.getId());
        log.info("All refresh tokens for user '{}' deleted after password reset.", user.getUsername());

        return UserResponseDTO.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fin(user.getFin())
                .role(Role.valueOf(user.getRole().name()))
                .build();
    }

    // --- Autentifikasiya və Tokenlər ---

    @Transactional
    public AuthResponse authenticateUser(LoginRequest loginRequest, HttpServletRequest request) {
        User user = findUserByIdentifier(loginRequest.getIdentifier());

        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("Account is not activated. Please verify your account.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getIdentifier(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateTokenFromUsername(userDetails.getUsername());

        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        // UserAgent null olarsa, NOT NULL məhdudiyyətini pozmamaq üçün default dəyər təyin edilir.
        if (userAgent == null || userAgent.trim().isEmpty()) {
            userAgent = "Unknown";
        }

        // Lambda daxilində istifadə üçün effektiv final dəyişənə köçürülür.
        final String finalUserAgent = userAgent;

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .map(existingToken -> {
                    existingToken.setExpiryDate(Instant.now().plusMillis(jwtUtils.getRefreshTokenExpirationMs()));
                    existingToken.setIpAddress(ipAddress);
                    existingToken.setUserAgent(finalUserAgent); // Effektiv final dəyişən istifadə edilir
                    return refreshTokenRepository.save(existingToken);
                })
                .orElseGet(() -> jwtUtils.createRefreshToken(user, ipAddress, finalUserAgent)); // Effektiv final dəyişən ötürülür

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new AuthResponse(
                jwt,
                refreshToken.getToken(),
                userDetails.getId(),
                userDetails.getActualUsername(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getPhone(),
                roles
        );
    }

    @Transactional
    public AuthResponse refreshAccessToken(String requestRefreshToken, HttpServletRequest request) {
        RefreshToken refreshToken = jwtUtils.verifyRefreshToken(requestRefreshToken);

        User user = refreshToken.getUser();

        String currentIpAddress = request.getRemoteAddr();
        // UserAgent check'i əlavə etmək tövsiyə olunur (yoxdur, amma yoxlamaq lazımdır)

        if (!refreshToken.getIpAddress().equals(currentIpAddress)) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException(requestRefreshToken, "Refresh token used from a different IP address!");
        }

        String newAccessToken = jwtUtils.generateTokenFromUsername(user.getFin());

        refreshToken.setExpiryDate(Instant.now().plusMillis(jwtUtils.getRefreshTokenExpirationMs()));
        refreshTokenRepository.save(refreshToken);

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new AuthResponse(
                newAccessToken,
                refreshToken.getToken(),
                userDetails.getId(),
                userDetails.getActualUsername(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getPhone(),
                roles
        );
    }

    @Transactional
    public void logoutUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for logout."));

        int deletedCount = refreshTokenRepository.deleteByUser(user);
        log.info("Deleted {} refresh tokens for user {}.", deletedCount, userId);
    }

    // --- Yardımçı Metodlar ---

    private User findUserByIdentifier(String identifier) {
        return userRepository.findByFin(identifier)
                .orElseGet(() -> userRepository.findByEmail(identifier)
                        .orElseGet(() -> userRepository.findByUsername(identifier)
                                .orElseThrow(() -> new InvalidCredentialsException("User not found."))));
    }

    private String generateOtpCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }

    public void sendTestEmail(String email) {
        emailService.sendTestEmail(email);
    }
}