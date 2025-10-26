package com.example.authservice.service;

import com.example.authservice.exception.*;
import com.example.authservice.kafka.KafkaAuthProducer;
import com.example.authservice.kafka.UserRegisteredEventDTO;
import com.example.authservice.mapper.UserMapper;
import com.example.authservice.model.dto.*;
import com.example.authservice.model.entity.ConfirmationToken;
import com.example.authservice.model.entity.RefreshToken;
import com.example.authservice.model.entity.User;
import com.example.authservice.model.enumeration.Role; // Role enum-u import edirik
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

    // Konstruktor Spring tərəfindən inject edilən dependency-ləri alır.
    // TelegramService artıq yoxdur.
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

    /**
     * Yeni istifadəçini qeydiyyatdan keçirir.
     * FIN, Username, Email və ya Phone artıq mövcuddursa UserAlreadyExistsException atır.
     * İstifadəçiyə default olaraq `ROLE_USER` rolunu təyin edir.
     *
     * @param signupRequest Qeydiyyat məlumatları (username, fin, password, email, phone, whatsappId)
     * @return Yaradılan istifadəçi obyekti
     */
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
        user.setRole(Role.USER); // Default role USER at registration
        user.setEnabled(false); // Account is inactive by default, must be confirmed via OTP

        User savedUser = userRepository.save(user);

        // ✅ Yeni: Kafka event-i burada göndəririk
        UserRegisteredEventDTO event = new UserRegisteredEventDTO(
                savedUser.getId().toString(),
                savedUser.getEmail(),
                UUID.randomUUID().toString(), // verificationToken (opsional)
                java.time.LocalDateTime.now()
        );

        kafkaProducer.sendUserRegistrationEvent(event);
        log.info("Kafka user registration event sent for user: {}", savedUser.getUsername());

        return savedUser;    }

    /**
     * İstifadəçiyə OTP kodu göndərir.
     * OTP tipi (ACCOUNT_CONFIRMATION, PASSWORD_RESET) və göndərmə metodu (email, phone) seçilir.
     *
     * @param otpSendRequest OTP sending request (identifier, otpType, sendMethod)
     */
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

        // Delete previous OTPs of the same type
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

    /**
     * Göndərilmiş OTP kodunu təsdiqləyir.
     * Hesab təsdiqlənməsi və ya şifrə sıfırlaması üçün istifadə olunur.
     *
     * @param otpVerificationRequest OTP verification request (identifier, otpCode, otpType)
     */
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
            confirmationToken.setUsed(true); // Account confirmation: Mark OTP as used
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

    /**
     * Şifrə sıfırlama OTP-si təsdiqləndikdən sonra istifadəçinin şifrəsini sıfırlayır.
     *
     * @param request Yeni şifrə və OTP kodu
     */
    @Transactional
    public UserResponseDTO resetPassword(ResetPasswordRequest request) {
        log.info("Attempting to reset password for identifier: {}", request.getIdentifier());

        User user = userRepository.findByEmail(request.getIdentifier())
                .orElseGet(() -> userRepository.findByUsername(request.getIdentifier())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with identifier: " + request.getIdentifier())));

        // ✅ DÜZƏLİŞ 1: Təsdiqlənmiş OTP-ni yoxlamaq
        ConfirmationToken verifiedToken = confirmationTokenRepository
                .findByTokenAndTypeAndUsedFalseAndExpiresAtAfter(
                request.getOtpCode(), // DTO-dan birbaşa otpCode-u götürürük
                "PASSWORD_RESET",
                Instant.now())
                .orElseThrow(() -> new OtpException("Password reset authorization is invalid, expired, or has already been used."));

        // İstifadəçi tokenin sahibidir?
        if (!Objects.equals(verifiedToken.getUser().getId(), user.getId())) {
            throw new OtpException("This OTP code belongs to another user.");
        }
        // ✅ DÜZƏLİŞ 2: Təkrar istifadənin qarşısını almaq
        // Tokeni tamamilə silmək və ya bir daha istifadə olunmaması üçün xüsusi bir sahə təyin etmək.
        // Ən sadə həlli: istifadə olunduqdan sonra onu silmək.
        confirmationTokenRepository.delete(verifiedToken);

        // Şifrəni yenilə
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password for user '{}' reset successfully.", user.getUsername());

        // Refresh tokenləri ləğv et
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

    /**
     * İstifadəçinin identifikasiya məlumatları ilə (FIN, username, email) autentifikasiya edir.
     * JWT Access Token və Refresh Token qaytarır.
     *
     * @param loginRequest Login məlumatları (identifier, password)
     * @param request HTTP request obyekti (User-Agent və IP almaq üçün)
     * @return Autentifikasiya cavabı (JWT tokenlər və istifadəçi məlumatları)
     */
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

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .map(existingToken -> {
                    existingToken.setExpiryDate(Instant.now().plusMillis(jwtUtils.getRefreshTokenExpirationMs()));
                    existingToken.setIpAddress(ipAddress);
                    return refreshTokenRepository.save(existingToken);
                })
                .orElseGet(() -> jwtUtils.createRefreshToken(user, ipAddress));

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new AuthResponse(
                jwt,
                refreshToken.getToken(),
                userDetails.getId(),
                userDetails.getActualUsername(),
                userDetails.getUsername(), // FIN
                userDetails.getEmail(),
                userDetails.getPhone(),
                roles
        );
    }

    /**
     * Refresh Token vasitəsilə yeni Access Token yaradır.
     * Refresh Token-in etibarlılığını və istifadəçi agenti/IP adresini yoxlayır.
     *
     * @param requestRefreshToken Refresh Token stringi
     * @param request HTTP request obyekti (User-Agent və IP almaq üçün)
     * @return Autentifikasiya cavabı (yeni Access Token və eyni Refresh Token)
     */
    @Transactional
    public AuthResponse refreshAccessToken(String requestRefreshToken, HttpServletRequest request) {
        RefreshToken refreshToken = jwtUtils.verifyRefreshToken(requestRefreshToken);

        User user = refreshToken.getUser();

        String currentIpAddress = request.getRemoteAddr();

        // Check if refresh token is used from a different IP
        if (!refreshToken.getIpAddress().equals(currentIpAddress)) {
            refreshTokenRepository.delete(refreshToken); // Delete suspicious token
            throw new TokenRefreshException(requestRefreshToken, "Refresh token used from a different IP address!");
        }

        String newAccessToken = jwtUtils.generateTokenFromUsername(user.getFin());

        // Update refresh token expiry
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

    /**
     * Cari istifadəçinin bütün refresh tokenlərini ləğv edərək çıxışını təmin edir.
     *
     * @param userId Çıxış edən istifadəçinin ID-si
     */
    @Transactional
    public void logoutUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for logout."));

        int deletedCount = refreshTokenRepository.deleteByUser(user);
        log.info("Deleted {} refresh tokens for user {}.", deletedCount, userId);
    }

    /**
     * FIN, email və ya username vasitəsilə istifadəçini tapır.
     *
     * @param identifier İstifadəçinin FIN, email və ya username-i
     * @return Tapılan istifadəçi obyekti
     * @throws InvalidCredentialsException İstifadəçi tapılmazsa
     */
    private User findUserByIdentifier(String identifier) {
        return userRepository.findByFin(identifier)
                .orElseGet(() -> userRepository.findByEmail(identifier)
                        .orElseGet(() -> userRepository.findByUsername(identifier)
                                .orElseThrow(() -> new InvalidCredentialsException("User not found."))));
    }

    /**
     * 6 rəqəmli OTP kodu yaradır.
     *
     * @return Yaradılan OTP kodu
     */
    private String generateOtpCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }

    public void sendTestEmail(String email) {
        emailService.sendTestEmail(email);
    }
}
