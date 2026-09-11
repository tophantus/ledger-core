package com.example.ledgercore.withdrawal.query.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.withdrawal.entity.WithdrawalIntent;
import com.example.ledgercore.withdrawal.enums.WithdrawalIntentStatus;
import com.example.ledgercore.withdrawal.query.dto.GetAccountWithdrawalIntentsQuery;
import com.example.ledgercore.withdrawal.query.dto.WithdrawalIntentResponse;
import com.example.ledgercore.withdrawal.query.port.outbound.AccountOwnershipPort;
import com.example.ledgercore.withdrawal.query.repository.WithdrawalIntentQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAccountWithdrawalIntentsHandlerTest {

    @Mock
    private WithdrawalIntentQueryRepository
            withdrawalIntentQueryRepository;

    @Mock
    private AccountOwnershipPort
            accountOwnershipPort;

    private GetAccountWithdrawalIntentsHandler handler;

    private UUID userId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        handler = new GetAccountWithdrawalIntentsHandler(
                withdrawalIntentQueryRepository,
                accountOwnershipPort
        );

        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
    }

    @Test
    void shouldReturnWithdrawalIntents_whenAccountBelongsToUser() {
        GetAccountWithdrawalIntentsQuery query =
                new GetAccountWithdrawalIntentsQuery(
                        userId,
                        accountId,
                        null,
                        0,
                        20
                );

        WithdrawalIntent intent = createIntent();

        PageRequest pageable = pageable(0, 20);

        Page<WithdrawalIntent> page =
                new PageImpl<>(
                        List.of(intent),
                        pageable,
                        1
                );

        when(accountOwnershipPort.checkOwnership(
                userId,
                accountId
        )).thenReturn(true);

        when(withdrawalIntentQueryRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(page);

        PageResponse<WithdrawalIntentResponse> response =
                handler.execute(query);

        assertThat(response.content())
                .hasSize(1);

        WithdrawalIntentResponse result =
                response.content().getFirst();

        assertThat(result.id())
                .isEqualTo(intent.getId());
        assertThat(result.accountId())
                .isEqualTo(intent.getAccountId());
        assertThat(result.withdrawalReference())
                .isEqualTo(intent.getWithdrawalReference());
        assertThat(result.amount())
                .isEqualTo(intent.getAmount().toPlainString());
        assertThat(result.currency())
                .isEqualTo(intent.getCurrency());
        assertThat(result.status())
                .isEqualTo(intent.getStatus());
        assertThat(result.expiresAt())
                .isEqualTo(intent.getExpiresAt());
        assertThat(result.createdAt())
                .isEqualTo(intent.getCreatedAt());
        assertThat(result.completedAt())
                .isEqualTo(intent.getCompletedAt());

        assertThat(response.page())
                .isZero();
        assertThat(response.size())
                .isEqualTo(20);
        assertThat(response.totalElements())
                .isEqualTo(1);
        assertThat(response.totalPages())
                .isEqualTo(1);

        verify(accountOwnershipPort)
                .checkOwnership(userId, accountId);

        verify(withdrawalIntentQueryRepository)
                .findAll(
                        any(Specification.class),
                        eq(pageable)
                );
    }

    @Test
    void shouldThrowAccessDenied_whenAccountDoesNotBelongToUser() {
        GetAccountWithdrawalIntentsQuery query =
                new GetAccountWithdrawalIntentsQuery(
                        userId,
                        accountId,
                        null,
                        0,
                        20
                );

        when(accountOwnershipPort.checkOwnership(
                userId,
                accountId
        )).thenReturn(false);

        assertThatThrownBy(() -> handler.execute(query))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue(
                        "errorCode",
                        ErrorCode.ACCESS_DENIED
                );

        verify(accountOwnershipPort)
                .checkOwnership(userId, accountId);

        verifyNoInteractions(
                withdrawalIntentQueryRepository
        );
    }

    @Test
    void shouldApplyStatusFilter_whenStatusIsProvided() {
        GetAccountWithdrawalIntentsQuery query =
                new GetAccountWithdrawalIntentsQuery(
                        userId,
                        accountId,
                        WithdrawalIntentStatus.READY,
                        0,
                        20
                );

        PageRequest pageable = pageable(0, 20);

        Page<WithdrawalIntent> page =
                new PageImpl<>(
                        List.of(),
                        pageable,
                        0
                );

        when(accountOwnershipPort.checkOwnership(
                userId,
                accountId
        )).thenReturn(true);

        when(withdrawalIntentQueryRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(page);

        handler.execute(query);

        ArgumentCaptor<Specification<WithdrawalIntent>>
                specificationCaptor =
                ArgumentCaptor.forClass(Specification.class);

        verify(withdrawalIntentQueryRepository)
                .findAll(
                        specificationCaptor.capture(),
                        eq(pageable)
                );

        assertThat(specificationCaptor.getValue())
                .isNotNull();
    }

    @Test
    void shouldNotApplyStatusFilter_whenStatusIsNull() {
        GetAccountWithdrawalIntentsQuery query =
                new GetAccountWithdrawalIntentsQuery(
                        userId,
                        accountId,
                        null,
                        0,
                        20
                );

        PageRequest pageable = pageable(0, 20);

        Page<WithdrawalIntent> page =
                new PageImpl<>(
                        List.of(),
                        pageable,
                        0
                );

        when(accountOwnershipPort.checkOwnership(
                userId,
                accountId
        )).thenReturn(true);

        when(withdrawalIntentQueryRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(page);

        handler.execute(query);

        verify(withdrawalIntentQueryRepository)
                .findAll(
                        any(Specification.class),
                        eq(pageable)
                );
    }

    @Test
    void shouldReturnEmptyPage_whenNoWithdrawalIntentsFound() {
        GetAccountWithdrawalIntentsQuery query =
                new GetAccountWithdrawalIntentsQuery(
                        userId,
                        accountId,
                        null,
                        0,
                        20
                );

        PageRequest pageable = pageable(0, 20);

        Page<WithdrawalIntent> page =
                new PageImpl<>(
                        List.of(),
                        pageable,
                        0
                );

        when(accountOwnershipPort.checkOwnership(
                userId,
                accountId
        )).thenReturn(true);

        when(withdrawalIntentQueryRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(page);

        PageResponse<WithdrawalIntentResponse> response =
                handler.execute(query);

        assertThat(response.content())
                .isEmpty();

        assertThat(response.totalElements())
                .isZero();

        assertThat(response.totalPages())
                .isZero();
    }

    @Test
    void shouldUseRequestedPageSizeAndSort() {
        GetAccountWithdrawalIntentsQuery query =
                new GetAccountWithdrawalIntentsQuery(
                        userId,
                        accountId,
                        null,
                        2,
                        10
                );

        PageRequest pageable = pageable(2, 10);

        Page<WithdrawalIntent> page =
                new PageImpl<>(
                        List.of(),
                        pageable,
                        25
                );

        when(accountOwnershipPort.checkOwnership(
                userId,
                accountId
        )).thenReturn(true);

        when(withdrawalIntentQueryRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(page);

        PageResponse<WithdrawalIntentResponse> response =
                handler.execute(query);

        assertThat(response.page())
                .isEqualTo(2);
        assertThat(response.size())
                .isEqualTo(10);
        assertThat(response.totalElements())
                .isEqualTo(25);
        assertThat(response.totalPages())
                .isEqualTo(3);

        verify(withdrawalIntentQueryRepository)
                .findAll(
                        any(Specification.class),
                        eq(pageable)
                );
    }

    @Test
    void shouldMapMultipleWithdrawalIntents() {
        GetAccountWithdrawalIntentsQuery query =
                new GetAccountWithdrawalIntentsQuery(
                        userId,
                        accountId,
                        null,
                        0,
                        20
                );

        WithdrawalIntent first = createIntent();
        WithdrawalIntent second = createIntent();

        PageRequest pageable = pageable(0, 20);

        Page<WithdrawalIntent> page =
                new PageImpl<>(
                        List.of(first, second),
                        pageable,
                        2
                );

        when(accountOwnershipPort.checkOwnership(
                userId,
                accountId
        )).thenReturn(true);

        when(withdrawalIntentQueryRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(page);

        PageResponse<WithdrawalIntentResponse> response =
                handler.execute(query);

        assertThat(response.content())
                .hasSize(2);

        assertThat(response.content().get(0).id())
                .isEqualTo(first.getId());

        assertThat(response.content().get(1).id())
                .isEqualTo(second.getId());
    }

    private PageRequest pageable(
            int page,
            int size
    ) {
        return PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                )
        );
    }

    private WithdrawalIntent createIntent() {
        Instant now = Instant.parse(
                "2026-09-07T10:00:00Z"
        );

        return WithdrawalIntent.builder()
                .id(UUID.randomUUID())
                .withdrawalRequestId(UUID.randomUUID())
                .withdrawalReference("WD-" + UUID.randomUUID())
                .userId(userId)
                .accountId(accountId)
                .holdId(UUID.randomUUID())
                .amount(new BigDecimal("1000000"))
                .currency(Currency.VND)
                .withdrawalCodeHash("hashed-code")
                .status(WithdrawalIntentStatus.READY)
                .expiresAt(now.plusSeconds(600))
                .createdAt(now)
                .build();
    }
}