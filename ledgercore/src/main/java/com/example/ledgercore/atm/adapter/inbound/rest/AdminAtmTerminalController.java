package com.example.ledgercore.atm.adapter.inbound.rest;

import com.example.ledgercore.atm.command.dto.ActivateAtmTerminalCommand;
import com.example.ledgercore.atm.command.dto.ActivateAtmTerminalResponse;
import com.example.ledgercore.atm.command.dto.DeactivateAtmTerminalCommand;
import com.example.ledgercore.atm.command.dto.DeactivateAtmTerminalResponse;
import com.example.ledgercore.atm.command.dto.RegisterAtmTerminalCommand;
import com.example.ledgercore.atm.command.dto.RegisterAtmTerminalResponse;
import com.example.ledgercore.atm.command.dto.RotateAtmCredentialCommand;
import com.example.ledgercore.atm.command.dto.RotateAtmCredentialResponse;
import com.example.ledgercore.atm.command.port.inbound.ActivateAtmTerminalUseCase;
import com.example.ledgercore.atm.command.port.inbound.DeactivateAtmTerminalUseCase;
import com.example.ledgercore.atm.command.port.inbound.RegisterAtmTerminalUseCase;
import com.example.ledgercore.atm.command.port.inbound.RotateAtmCredentialUseCase;
import com.example.ledgercore.atm.query.dto.AtmTerminalQuery;
import com.example.ledgercore.atm.query.dto.AtmTerminalResponse;
import com.example.ledgercore.atm.query.port.inbound.GetAtmTerminalsUseCase;
import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/atm-terminals")
@RequiredArgsConstructor
@Tag(
        name = "Admin ATM Terminals",
        description = "Administrative ATM terminal management APIs"
)
public class AdminAtmTerminalController {

    private final RegisterAtmTerminalUseCase
            registerAtmTerminalUseCase;

    private final RotateAtmCredentialUseCase
            rotateAtmCredentialUseCase;

    private final ActivateAtmTerminalUseCase
            activateAtmTerminalUseCase;

    private final DeactivateAtmTerminalUseCase
            deactivateAtmTerminalUseCase;

    private final GetAtmTerminalsUseCase
            getAtmTerminalsUseCase;

    @PostMapping
    @Operation(
            summary = "Register ATM terminal",
            description = "Register a new ATM terminal"
    )
    public ResponseEntity<
            ApiResponse<RegisterAtmTerminalResponse>
            > register(
            @Valid @RequestBody RegisterAtmTerminalCommand command
    ) {
        RegisterAtmTerminalResponse response =
                registerAtmTerminalUseCase.execute(command);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "ATM terminal registered successfully"
                )
        );
    }

    @PostMapping("/{terminalId}/credential/rotate")
    @Operation(
            summary = "Rotate ATM credential",
            description = "Generate a new credential for an ATM terminal"
    )
    public ResponseEntity<
            ApiResponse<RotateAtmCredentialResponse>
            > rotateCredential(
            @PathVariable UUID terminalId
    ) {
        RotateAtmCredentialResponse response =
                rotateAtmCredentialUseCase.execute(
                        new RotateAtmCredentialCommand(
                                terminalId
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "ATM credential rotated successfully"
                )
        );
    }

    @PostMapping("/{terminalId}/activate")
    @Operation(
            summary = "Activate ATM terminal",
            description = "Activate an inactive ATM terminal"
    )
    public ResponseEntity<
            ApiResponse<ActivateAtmTerminalResponse>
            > activate(
            @PathVariable UUID terminalId
    ) {
        ActivateAtmTerminalResponse response =
                activateAtmTerminalUseCase.execute(
                        new ActivateAtmTerminalCommand(
                                terminalId
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "ATM terminal activated successfully"
                )
        );
    }

    @PostMapping("/{terminalId}/deactivate")
    @Operation(
            summary = "Deactivate ATM terminal",
            description = "Deactivate an active ATM terminal"
    )
    public ResponseEntity<
            ApiResponse<DeactivateAtmTerminalResponse>
            > deactivate(
            @PathVariable UUID terminalId
    ) {
        DeactivateAtmTerminalResponse response =
                deactivateAtmTerminalUseCase.execute(
                        new DeactivateAtmTerminalCommand(
                                terminalId
                        )
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "ATM terminal deactivated successfully"
                )
        );
    }

    @GetMapping
    @Operation(
            summary = "Get ATM terminals",
            description = "Get paginated ATM terminals with search and filters"
    )
    public ResponseEntity<
            ApiResponse<PageResponse<AtmTerminalResponse>>
            > getAtmTerminals(
            @ModelAttribute AtmTerminalQuery query
    ) {
        PageResponse<AtmTerminalResponse> response =
                getAtmTerminalsUseCase.execute(query);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "ATM terminals retrieved successfully"
                )
        );
    }
}