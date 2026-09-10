package com.example.ledgercore.withdrawal.query.handler;

import com.example.ledgercore.common.dto.PageResponse;
import com.example.ledgercore.withdrawal.entity.WithdrawalIntent;
import com.example.ledgercore.withdrawal.enums.WithdrawalIntentStatus;
import com.example.ledgercore.withdrawal.query.dto.GetUserWithdrawalIntentsQuery;
import com.example.ledgercore.withdrawal.query.dto.WithdrawalIntentResponse;
import com.example.ledgercore.withdrawal.query.port.outbound.AccountIdsPort;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserWithdrawalIntentsHandlerTest {

    @Mock
    private WithdrawalIntentQueryRepository
            withdrawalIntentQueryRepository;

    @Mock
    private AccountIdsPort accountIdsPort;

    private GetUserWithdrawalIntentsHandler handler;

    private UUID userId;
    private UUID accountId1;
    private UUID accountId2;

    @BeforeEach
    void setUp() {
        handler = new GetUserWithdrawalIntentsHandler(
                withdrawalIntentQueryRepository,
                accountIdsPort
        );

        userId = UUID.randomUUID();
        accountId1 = UUID.randomUUID();
        accountId2 = UUID.randomUUID();
    }

    @Test
    void shouldReturnWithdrawalIntents_whenUserHasAccounts() {
        GetUserWithdrawalIntentsQuery query =
                new GetUserWithdrawalIntentsQuery(
                        userId,
                        null,
                        0,
                        20
                );

        WithdrawalIntent intent =
                createIntent(accountId1);

        PageRequest pageable =
                pageable(0, 20);

        Page<WithdrawalIntent> page =
                new PageImpl<>(
                        List.of(intent),
                        pageable,
                        1
                );

        when(accountIdsPort.getAccountIdsByUser(userId))
                .thenReturn(List.of(accountId1, accountId2));

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

        verify(accountIdsPort)
                .getAccountIdsByUser(userId);

        verify(withdrawalIntentQueryRepository)
                .findAll(
                        any(Specification.class),
                        eq(pageable)
                );
    }

    @Test
    void shouldReturnEmptyPage_whenUserHasNoAccounts() {
        GetUserWithdrawalIntentsQuery query =
                new GetUserWithdrawalIntentsQuery(
                        userId,
                        null,
                        0,
                        20
                );

        when(accountIdsPort.getAccountIdsByUser(userId))
                .thenReturn(List.of());

        PageResponse<WithdrawalIntentResponse> response =
                handler.execute(query);

        assertThat(response.content())
                .isEmpty();

        assertThat(response.page())
                .isZero();

        assertThat(response.size())
                .isEqualTo(20);

        assertThat(response.totalElements())
                .isZero();

        assertThat(response.totalPages())
                .isZero();

        verify(accountIdsPort)
                .getAccountIdsByUser(userId);

        verifyNoInteractions(
                withdrawalIntentQueryRepository
        );
    }

    @Test
    void shouldApplyStatusFilter_whenStatusIsProvided() {
        GetUserWithdrawalIntentsQuery query =
                new GetUserWithdrawalIntentsQuery(
                        userId,
                        WithdrawalIntentStatus.READY,
                        0,
                        20
                );

        PageRequest pageable =
                pageable(0, 20);

        Page<WithdrawalIntent> page =
                new PageImpl<>(
                        List.of(),
                        pageable,
                        0
                );

        when(accountIdsPort.getAccountIdsByUser(userId))
                .thenReturn(List.of(accountId1));

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
        GetUserWithdrawalIntentsQuery query =
                new GetUserWithdrawalIntentsQuery(
                        userId,
                        null,
                        0,
                        20
                );

        PageRequest pageable =
                pageable(0, 20);

        Page<WithdrawalIntent> page =
                new PageImpl<>(
                        List.of(),
                        pageable,
                        0
                );

        when(accountIdsPort.getAccountIdsByUser(userId))
                .thenReturn(List.of(accountId1));

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
    void shouldUseRequestedPageSizeAndSort() {
        GetUserWithdrawalIntentsQuery query =
                new GetUserWithdrawalIntentsQuery(
                        userId,
                        null,
                        2,
                        10
                );

        PageRequest pageable =
                pageable(2, 10);

        Page<WithdrawalIntent> page =
                new PageImpl<>(
                        List.of(),
                        pageable,
                        25
                );

        when(accountIdsPort.getAccountIdsByUser(userId))
                .thenReturn(List.of(accountId1));

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
    void shouldReturnEmptyPage_whenNoWithdrawalIntentsFound() {
        GetUserWithdrawalIntentsQuery query =
                new GetUserWithdrawalIntentsQuery(
                        userId,
                        null,
                        0,
                        20
                );

        PageRequest pageable =
                pageable(0, 20);

        Page<WithdrawalIntent> page =
                new PageImpl<>(
                        List.of(),
                        pageable,
                        0
                );

        when(accountIdsPort.getAccountIdsByUser(userId))
                .thenReturn(List.of(accountId1));

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
    void shouldMapMultipleWithdrawalIntents() {
        GetUserWithdrawalIntentsQuery query =
                new GetUserWithdrawalIntentsQuery(
                        userId,
                        null,
                        0,
                        20
                );

        WithdrawalIntent first =
                createIntent(accountId1);

        WithdrawalIntent second =
                createIntent(accountId2);

        PageRequest pageable =
                pageable(0, 20);

        Page<WithdrawalIntent> page =
                new PageImpl<>(
                        List.of(first, second),
                        pageable,
                        2
                );

        when(accountIdsPort.getAccountIdsByUser(userId))
                .thenReturn(List.of(accountId1, accountId2));

        when(withdrawalIntentQueryRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(page);

        PageResponse<WithdrawalIntentResponse> response =
                handler.execute(query);

        assertThat(response.content())
                .hasSize(2);

        WithdrawalIntentResponse firstResponse =
                response.content().get(0);

        WithdrawalIntentResponse secondResponse =
                response.content().get(1);

        assertThat(firstResponse.id())
                .isEqualTo(first.getId());

        assertThat(firstResponse.accountId())
                .isEqualTo(first.getAccountId());

        assertThat(secondResponse.id())
                .isEqualTo(second.getId());

        assertThat(secondResponse.accountId())
                .isEqualTo(second.getAccountId());
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

    private WithdrawalIntent createIntent(
            UUID accountId
    ) {
        Instant now =
                Instant.parse("2026-09-07T10:00:00Z");

        return WithdrawalIntent.builder()
                .id(UUID.randomUUID())
                .withdrawalRequestId(UUID.randomUUID())
                .withdrawalReference(
                        "WD-" + UUID.randomUUID()
                )
                .userId(userId)
                .accountId(accountId)
                .holdId(UUID.randomUUID())
                .amount(new BigDecimal("1000000"))
                .currency("VND")
                .withdrawalCodeHash("hashed-code")
                .status(WithdrawalIntentStatus.READY)
                .expiresAt(now.plusSeconds(600))
                .createdAt(now)
                .build();
    }
}