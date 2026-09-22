package com.example.ledgercore.provider.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.provider.command.dto.RegisterPaymentProviderCommand;
import com.example.ledgercore.provider.command.dto.RegisterPaymentProviderResult;
import com.example.ledgercore.provider.command.repository.PaymentProviderCommandRepository;
import com.example.ledgercore.provider.command.service.PaymentProviderClientIdGenerator;
import com.example.ledgercore.provider.command.service.PaymentProviderCredentialGenerator;
import com.example.ledgercore.provider.command.service.PaymentProviderCredentialHashService;
import com.example.ledgercore.provider.entity.PaymentProvider;
import com.example.ledgercore.provider.enums.ProviderStatus;
import com.example.ledgercore.provider.enums.ProviderType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterPaymentProviderHandlerTest {

    @Mock
    private PaymentProviderCommandRepository paymentProviderCommandRepository;

    @Mock
    private PaymentProviderClientIdGenerator clientIdGenerator;

    @Mock
    private PaymentProviderCredentialGenerator credentialGenerator;

    @Mock
    private PaymentProviderCredentialHashService credentialHashService;

    @InjectMocks
    private RegisterPaymentProviderHandler handler;

    @Test
    void shouldRegisterPaymentProviderSuccessfully() {
        RegisterPaymentProviderCommand command =
                new RegisterPaymentProviderCommand(
                        "  PAY-1001  ",
                        "  Acme Payments  ",
                        ProviderType.PSP
                );

        String generatedClientId = "client-123";
        String generatedCredential = "secure-credential";
        String hashedCredential = "hashed-credential";
        UUID providerId = UUID.randomUUID();

        when(paymentProviderCommandRepository.existsByCode("PAY-1001"))
                .thenReturn(false);
        when(clientIdGenerator.generate())
                .thenReturn(generatedClientId);
        when(paymentProviderCommandRepository.existsByClientId(generatedClientId))
                .thenReturn(false);
        when(credentialGenerator.generate())
                .thenReturn(generatedCredential);
        when(credentialHashService.hash(generatedCredential))
                .thenReturn(hashedCredential);
        when(paymentProviderCommandRepository.save(any(PaymentProvider.class)))
                .thenAnswer(invocation -> {
                    PaymentProvider provider = invocation.getArgument(0);
                    provider.setId(providerId);
                    return provider;
                });

        RegisterPaymentProviderResult result =
                handler.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.providerId()).isEqualTo(providerId);
        assertThat(result.code()).isEqualTo("PAY-1001");
        assertThat(result.name()).isEqualTo("Acme Payments");
        assertThat(result.type()).isEqualTo(ProviderType.PSP);
        assertThat(result.status()).isEqualTo(ProviderStatus.ACTIVE);
        assertThat(result.clientId()).isEqualTo(generatedClientId);
        assertThat(result.credential()).isEqualTo(generatedCredential);

        ArgumentCaptor<PaymentProvider> captor =
                ArgumentCaptor.forClass(PaymentProvider.class);

        verify(paymentProviderCommandRepository)
                .save(captor.capture());

        PaymentProvider savedProvider = captor.getValue();
        assertThat(savedProvider.getCode()).isEqualTo("PAY-1001");
        assertThat(savedProvider.getName()).isEqualTo("Acme Payments");
        assertThat(savedProvider.getType()).isEqualTo(ProviderType.PSP);
        assertThat(savedProvider.getStatus()).isEqualTo(ProviderStatus.ACTIVE);
        assertThat(savedProvider.getClientId()).isEqualTo(generatedClientId);
        assertThat(savedProvider.getCredentialHash()).isEqualTo(hashedCredential);
        assertThat(savedProvider.getCreatedAt()).isNotNull();
        assertThat(savedProvider.getUpdatedAt()).isNotNull();

        verify(paymentProviderCommandRepository)
                .existsByCode("PAY-1001");
        verify(clientIdGenerator).generate();
        verify(paymentProviderCommandRepository)
                .existsByClientId(generatedClientId);
        verify(credentialGenerator).generate();
        verify(credentialHashService).hash(generatedCredential);
    }

    @Test
    void shouldRetryClientIdGenerationUntilUnique() {
        RegisterPaymentProviderCommand command =
                new RegisterPaymentProviderCommand(
                        "PAY-2002",
                        "Northwind",
                        ProviderType.ACQUIRER
                );

        when(paymentProviderCommandRepository.existsByCode("PAY-2002"))
                .thenReturn(false);
        when(clientIdGenerator.generate())
                .thenReturn("duplicate-client-id", "unique-client-id");
        when(paymentProviderCommandRepository.existsByClientId("duplicate-client-id"))
                .thenReturn(true);
        when(paymentProviderCommandRepository.existsByClientId("unique-client-id"))
                .thenReturn(false);
        when(credentialGenerator.generate())
                .thenReturn("credential-456");
        when(credentialHashService.hash("credential-456"))
                .thenReturn("hashed-456");
        when(paymentProviderCommandRepository.save(any(PaymentProvider.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegisterPaymentProviderResult result =
                handler.execute(command);

        assertThat(result.clientId()).isEqualTo("unique-client-id");
        verify(paymentProviderCommandRepository, times(2))
                .existsByClientId(anyString());
        verify(clientIdGenerator, times(2)).generate();
    }

    @Test
    void shouldRejectNullCommand() {
        assertThatThrownBy(() -> handler.execute(null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        verifyNoInteractions(
                paymentProviderCommandRepository,
                clientIdGenerator,
                credentialGenerator,
                credentialHashService
        );
    }

    @Test
    void shouldRejectBlankCode() {
        RegisterPaymentProviderCommand command =
                new RegisterPaymentProviderCommand(
                        "   ",
                        "Acme",
                        ProviderType.PSP
                );

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROVIDER_CODE_REQUIRED);

        verifyNoInteractions(
                paymentProviderCommandRepository,
                clientIdGenerator,
                credentialGenerator,
                credentialHashService
        );
    }

    @Test
    void shouldRejectBlankName() {
        RegisterPaymentProviderCommand command =
                new RegisterPaymentProviderCommand(
                        "PAY-3003",
                        null,
                        ProviderType.PSP
                );

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROVIDER_NAME_REQUIRED);

        verifyNoInteractions(
                paymentProviderCommandRepository,
                clientIdGenerator,
                credentialGenerator,
                credentialHashService
        );
    }

    @Test
    void shouldRejectNullType() {
        RegisterPaymentProviderCommand command =
                new RegisterPaymentProviderCommand(
                        "PAY-3004",
                        "Acme",
                        null
                );

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROVIDER_TYPE_REQUIRED);

        verifyNoInteractions(
                paymentProviderCommandRepository,
                clientIdGenerator,
                credentialGenerator,
                credentialHashService
        );
    }

    @Test
    void shouldRejectDuplicateCode() {
        RegisterPaymentProviderCommand command =
                new RegisterPaymentProviderCommand(
                        "PAY-4001",
                        "Duplication",
                        ProviderType.PSP
                );

        when(paymentProviderCommandRepository.existsByCode("PAY-4001"))
                .thenReturn(true);

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROVIDER_CODE_ALREADY_EXISTS);

        verify(paymentProviderCommandRepository)
                .existsByCode("PAY-4001");
        verifyNoInteractions(clientIdGenerator, credentialGenerator, credentialHashService);
        verify(paymentProviderCommandRepository, never()).save(any(PaymentProvider.class));
    }
}
