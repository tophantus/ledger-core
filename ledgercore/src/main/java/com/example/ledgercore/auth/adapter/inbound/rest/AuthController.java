package com.example.ledgercore.auth.adapter.inbound.rest;

import com.example.ledgercore.auth.command.dto.*;
import com.example.ledgercore.auth.command.port.inbound.*;
import com.example.ledgercore.auth.security.AuthPrincipal;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = "Authentication and authorization APIs"
)
public class AuthController {

    private final SignUpUseCase signUpUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ResendVerificationCodeUseCase resendVerificationCodeUseCase;
    private final UpdatePasswordUseCase updatePasswordUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RefreshTokenCookieService refreshTokenCookieService;

    @PostMapping("/sign-up")
    @Operation(
            summary = "Sign up",
            description = "Create a new user account and send verification OTP"
    )
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(
            @RequestBody SignUpCommand command
    ) {
        SignUpResponse response =
                signUpUseCase.execute(command);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Sign up successfully"
                )
        );
    }

    @PostMapping("/verify-email")
    @Operation(
            summary = "Verify email",
            description = "Verify the email verification OTP and activate the user account"
    )
    public ResponseEntity<ApiResponse<TokenResponse>> verifyOtp(
            @RequestBody VerifyEmailCommand command,
            HttpServletResponse httpResponse
    ) {
        TokenResponse response =
                verifyEmailUseCase.execute(command);

        refreshTokenCookieService.set(
                httpResponse,
                response.refreshToken()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "OTP verified successfully"
                )
        );
    }

    @PostMapping("/verify-email/resend")
    @Operation(
            summary = "Resend email verification code",
            description = "Resend the verification code to the user's email"
    )
    public ResponseEntity<ApiResponse<Void>> resendVerificationCode(
            @RequestBody ResendVerificationCodeCommand command
    ) {
        resendVerificationCodeUseCase.execute(command);

        return ResponseEntity.ok(
                ApiResponse.success(
                        null,
                        "Verification code sent successfully"
                )
        );
    }

    @PostMapping("/change-password")
    @Operation(
            summary = "Change password",
            description = "Change the password of the authenticated user"
    )
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody UpdatePasswordCommand command
    ) {
        updatePasswordUseCase.execute(
                principal.getUserId(),
                command
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        null,
                        "Password changed successfully"
                )
        );
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "Authenticate user and issue access and refresh tokens"
    )
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestBody LoginCommand command,
            HttpServletResponse httpResponse
    ) {
        LoginResponse response =
                loginUseCase.execute(command);

        if (response.token() != null) {
            refreshTokenCookieService.set(
                    httpResponse,
                    response.token().refreshToken()
            );
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Login successfully"
                )
        );
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh token",
            description = "Rotate refresh token and issue a new access token"
    )
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @RequestBody RefreshTokenCommand command,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String refreshToken =
                resolveRefreshToken(
                        command.refreshToken(),
                        httpRequest
                );

        TokenResponse response =
                refreshTokenUseCase.execute(
                        new RefreshTokenCommand(refreshToken)
                );

        refreshTokenCookieService.set(
                httpResponse,
                response.refreshToken()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Token refreshed successfully"
                )
        );
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout",
            description = "Revoke the current refresh token"
    )
    public ResponseEntity<ApiResponse<LogoutResponse>> logout(
            @RequestBody LogoutCommand command,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String refreshToken =
                resolveRefreshToken(
                        command.refreshToken(),
                        httpRequest
                );

        LogoutResponse response =
                logoutUseCase.execute(
                        new LogoutCommand(refreshToken)
                );
        refreshTokenCookieService.delete(httpResponse);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Logout successfully"
                )
        );
    }

    private String resolveRefreshToken(
            String commandRefreshToken,
            HttpServletRequest request
    ) {
        return refreshTokenCookieService
                .get(request)
                .orElseGet(() -> {
                    if (commandRefreshToken != null
                            && !commandRefreshToken.isBlank()) {
                        return commandRefreshToken;
                    }

                    throw new BusinessException(
                            ErrorCode.INVALID_REFRESH_TOKEN
                    );
                });
    }
}