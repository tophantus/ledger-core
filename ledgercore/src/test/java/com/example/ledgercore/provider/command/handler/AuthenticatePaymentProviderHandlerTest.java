package com.example.ledgercore.provider.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.provider.command.dto.AuthenticatePaymentProviderCommand;
import com.example.ledgercore.provider.command.dto.AuthenticatePaymentProviderResult;
import com.example.ledgercore.provider.command.repository.PaymentProviderCommandRepository;
import com.example.ledgercore.provider.command.service.PaymentProviderCredentialHashService;
import com.example.ledgercore.provider.entity.PaymentProvider;
import com.example.ledgercore.provider.enums.ProviderStatus;
import com.example.ledgercore.provider.enums.ProviderType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticatePaymentProviderHandlerTest {

    @Mock
    private PaymentProviderCommandRepository paymentProviderCommandRepository;

    @Mock
    private PaymentProviderCredentialHashService credentialHashService;

    @InjectMocks
    private AuthenticatePaymentProviderHandler handler;

    @Test
    void shouldAuthenticateActiveProviderSuccessfully() {
        AuthenticatePaymentProviderCommand command =
                new AuthenticatePaymentProviderCommand(
                        "  client-123  ",
                        "secret-credential"
                );

        UUID providerId = UUID.randomUUID();
        PaymentProvider provider = PaymentProvider.builder()
                .id(providerId)
                .code("PAY-1001")
                .name("Acme Payments")
                .type(ProviderType.PSP)
                .status(ProviderStatus.ACTIVE)
                .clientId("client-123")
                .credentialHash("hashed-credential")
                .build();

        when(paymentProviderCommandRepository.findByClientId("client-123"))
                .thenReturn(Optional.of(provider));
        when(credentialHashService.matches(
                "secret-credential",
                "hashed-credential"
        )).thenReturn(true);

        AuthenticatePaymentProviderResult result =
                handler.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.providerId()).isEqualTo(providerId);
        assertThat(result.code()).isEqualTo("PAY-1001");
        assertThat(result.name()).isEqualTo("Acme Payments");
        assertThat(result.type()).isEqualTo(ProviderType.PSP);
        assertThat(result.status()).isEqualTo(ProviderStatus.ACTIVE);

        verify(paymentProviderCommandRepository)
                .findByClientId("client-123");
        verify(credentialHashService)
                .matches("secret-credential", "hashed-credential");
    }

    @Test
    void shouldRejectNullCommand() {
        assertThatThrownBy(() -> handler.execute(null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        verifyNoInteractions(paymentProviderCommandRepository, credentialHashService);
    }

    @Test
    void shouldRejectBlankClientId() {
        AuthenticatePaymentProviderCommand command =
                new AuthenticatePaymentProviderCommand(
                        "   ",
                        "secret-credential"
                );

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROVIDER_CLIENT_ID_REQUIRED);

        verifyNoInteractions(paymentProviderCommandRepository, credentialHashService);
    }

    @Test
    void shouldRejectBlankCredential() {
        AuthenticatePaymentProviderCommand command =
                new AuthenticatePaymentProviderCommand(
                        "client-456",
                        "  "
                );

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROVIDER_CREDENTIAL_REQUIRED);

        verifyNoInteractions(paymentProviderCommandRepository, credentialHashService);
    }

    @Test
    void shouldRejectWhenProviderIsNotFound() {
        AuthenticatePaymentProviderCommand command =
                new AuthenticatePaymentProviderCommand(
                        "client-789",
                        "secret-credential"
                );

        when(paymentProviderCommandRepository.findByClientId("client-789"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROVIDER_AUTHENTICATION_FAILED);

        verify(paymentProviderCommandRepository)
                .findByClientId("client-789");
        verifyNoInteractions(credentialHashService);
    }

    @Test
    void shouldRejectInactiveProvider() {
        AuthenticatePaymentProviderCommand command =
                new AuthenticatePaymentProviderCommand(
                        "client-abc",
                        "secret-credential"
                );

        PaymentProvider provider = PaymentProvider.builder()
                .id(UUID.randomUUID())
                .code("PAY-2001")
                .name("Suspended Gateway")
                .type(ProviderType.ACQUIRER)
                .status(ProviderStatus.SUSPENDED)
                .clientId("client-abc")
                .credentialHash("hashed-credential")
                .build();

        when(paymentProviderCommandRepository.findByClientId("client-abc"))
                .thenReturn(Optional.of(provider));

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROVIDER_NOT_ACTIVE);

        verify(paymentProviderCommandRepository)
                .findByClientId("client-abc");
        verify(credentialHashService, never()).matches(anyString(), anyString());
    }

    @Test
    void shouldRejectWhenCredentialDoesNotMatch() {
        AuthenticatePaymentProviderCommand command =
                new AuthenticatePaymentProviderCommand(
                        "client-def",
                        "wrong-secret"
                );

        PaymentProvider provider = PaymentProvider.builder()
                .id(UUID.randomUUID())
                .code("PAY-3001")
                .name("Acme Payments")
                .type(ProviderType.PSP)
                .status(ProviderStatus.ACTIVE)
                .clientId("client-def")
                .credentialHash("hashed-credential")
                .build();

        when(paymentProviderCommandRepository.findByClientId("client-def"))
                .thenReturn(Optional.of(provider));
        when(credentialHashService.matches("wrong-secret", "hashed-credential"))
                .thenReturn(false);

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROVIDER_AUTHENTICATION_FAILED);

        verify(paymentProviderCommandRepository)
                .findByClientId("client-def");
        verify(credentialHashService)
                .matches("wrong-secret", "hashed-credential");
    }
}
