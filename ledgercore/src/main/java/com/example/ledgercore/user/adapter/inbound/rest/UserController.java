package com.example.ledgercore.user.adapter.inbound.rest;

import com.example.ledgercore.auth.security.AuthPrincipal;
import com.example.ledgercore.common.response.ApiResponse;
import com.example.ledgercore.user.query.dto.CurrentUserResponse;
import com.example.ledgercore.user.query.port.inbound.GetCurrentUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(
        name = "User",
        description = "User management APIs"
)
public class UserController {

    private final GetCurrentUserUseCase getCurrentUserUseCase;

    @GetMapping("/me")
    @Operation(
            summary = "Get current user",
            description = "Get information of the currently authenticated user"
    )
    public ResponseEntity<ApiResponse<CurrentUserResponse>> getCurrentUser(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        CurrentUserResponse response =
                getCurrentUserUseCase.execute(
                        principal.getUserId()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "User information retrieved successfully"
                )
        );
    }
}