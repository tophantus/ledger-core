package com.example.ledgercore.auth.adapter.inbound.rest;

import com.example.ledgercore.auth.command.dto.*;
import com.example.ledgercore.auth.command.port.inbound.*;
import com.example.ledgercore.auth.security.AuthPrincipal;
import com.example.ledgercore.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
            @RequestBody VerifyEmailCommand command
    ) {
        TokenResponse response =
                verifyEmailUseCase.execute(command);

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
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @RequestBody LoginCommand command
    ) {
        TokenResponse response =
                loginUseCase.execute(command);

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
            @RequestBody RefreshTokenCommand command
    ) {
        TokenResponse response =
                refreshTokenUseCase.execute(command);

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
            @RequestBody LogoutCommand command
    ) {
        LogoutResponse response =
                logoutUseCase.execute(command);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Logout successfully"
                )
        );
    }
}