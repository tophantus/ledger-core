package com.example.ledgercore.interest.command.handler;

import com.example.ledgercore.interest.command.dto.PostInterestCommand;
import com.example.ledgercore.interest.command.port.outbound.InterestTransactionPort;
import com.example.ledgercore.interest.command.repository.InterestAccrualCommandRepository;
import com.example.ledgercore.interest.command.repository.InterestPostingCommandRepository;
import com.example.ledgercore.interest.entity.InterestAccrual;
import com.example.ledgercore.interest.entity.InterestPosting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostInterestHandlerTest {

    @Mock
    private InterestAccrualCommandRepository
            interestAccrualCommandRepository;

    @Mock
    private InterestPostingCommandRepository
            interestPostingCommandRepository;

    @Mock
    private InterestTransactionPort
            interestTransactionPort;

    private PostInterestHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PostInterestHandler(
                interestAccrualCommandRepository,
                interestPostingCommandRepository,
                interestTransactionPort
        );
    }

    @Test
    void shouldPostInterestSuccessfully() {
        UUID runId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        UUID postingId = UUID.randomUUID();

        LocalDate periodStart =
                LocalDate.of(2026, 8, 1);

        LocalDate periodEnd =
                LocalDate.of(2026, 8, 31);

        InterestAccrual accrual1 =
                createAccrual(
                        accountId,
                        LocalDate.of(2026, 8, 30),
                        "1000.0000",
                        "VND"
                );

        InterestAccrual accrual2 =
                createAccrual(
                        accountId,
                        LocalDate.of(2026, 8, 31),
                        "2000.0000",
                        "VND"
                );

        PostInterestCommand command =
                new PostInterestCommand(
                        runId,
                        accountId,
                        periodStart,
                        periodEnd
                );

        when(
                interestPostingCommandRepository
                        .existsByAccountIdAndPeriodStartAndPeriodEnd(
                                accountId,
                                periodStart,
                                periodEnd
                        )
        ).thenReturn(false);

        when(
                interestAccrualCommandRepository
                        .findByAccountIdAndBusinessDateBetweenAndPostingIdIsNull(
                                accountId,
                                periodStart,
                                periodEnd
                        )
        ).thenReturn(List.of(accrual1, accrual2));

        InterestPosting savedPosting =
                InterestPosting.builder()
                        .id(postingId)
                        .runId(runId)
                        .accountId(accountId)
                        .periodStart(periodStart)
                        .periodEnd(periodEnd)
                        .interestAmount(
                                new BigDecimal("3000.0000")
                        )
                        .build();

        when(
                interestPostingCommandRepository.save(
                        any(InterestPosting.class)
                )
        ).thenReturn(savedPosting);

        when(
                interestTransactionPort.postInterest(
                        accountId,
                        new BigDecimal("3000.0000"),
                        "VND",
                        periodEnd
                )
        ).thenReturn(transactionId);

        handler.execute(command);

        ArgumentCaptor<InterestPosting> postingCaptor =
                ArgumentCaptor.forClass(InterestPosting.class);

        verify(
                interestPostingCommandRepository,
                times(2)
        ).save(postingCaptor.capture());

        InterestPosting createdPosting =
                postingCaptor.getAllValues().getFirst();

        assertThat(createdPosting.getRunId())
                .isEqualTo(runId);

        assertThat(createdPosting.getAccountId())
                .isEqualTo(accountId);

        assertThat(createdPosting.getPeriodStart())
                .isEqualTo(periodStart);

        assertThat(createdPosting.getPeriodEnd())
                .isEqualTo(periodEnd);

        assertThat(createdPosting.getInterestAmount())
                .isEqualByComparingTo("3000.0000");

        verify(interestTransactionPort)
                .postInterest(
                        accountId,
                        new BigDecimal("3000.0000"),
                        "VND",
                        periodEnd
                );

        assertThat(savedPosting.getRunId())
                .isEqualTo(runId);

        assertThat(savedPosting.getTransactionId())
                .isEqualTo(transactionId);

        assertThat(savedPosting.getPostedAt())
                .isNotNull();

        verify(interestPostingCommandRepository, times(2))
                .save(any(InterestPosting.class));

        verify(interestAccrualCommandRepository)
                .saveAll(List.of(accrual1, accrual2));

        assertThat(accrual1.getPostingId())
                .isEqualTo(postingId);

        assertThat(accrual2.getPostingId())
                .isEqualTo(postingId);
    }

    @Test
    void shouldReturnWhenInterestAlreadyPosted() {
        UUID runId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        LocalDate periodStart =
                LocalDate.of(2026, 8, 1);

        LocalDate periodEnd =
                LocalDate.of(2026, 8, 31);

        PostInterestCommand command =
                new PostInterestCommand(
                        runId,
                        accountId,
                        periodStart,
                        periodEnd
                );

        when(
                interestPostingCommandRepository
                        .existsByAccountIdAndPeriodStartAndPeriodEnd(
                                accountId,
                                periodStart,
                                periodEnd
                        )
        ).thenReturn(true);

        handler.execute(command);

        verify(
                interestPostingCommandRepository
        ).existsByAccountIdAndPeriodStartAndPeriodEnd(
                accountId,
                periodStart,
                periodEnd
        );

        verifyNoInteractions(
                interestAccrualCommandRepository,
                interestTransactionPort
        );

        verify(
                interestPostingCommandRepository,
                never()
        ).save(any());
    }

    @Test
    void shouldReturnWhenNoUnpostedAccrualsExist() {
        UUID runId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        LocalDate periodStart =
                LocalDate.of(2026, 8, 1);

        LocalDate periodEnd =
                LocalDate.of(2026, 8, 31);

        PostInterestCommand command =
                new PostInterestCommand(
                        runId,
                        accountId,
                        periodStart,
                        periodEnd
                );

        when(
                interestPostingCommandRepository
                        .existsByAccountIdAndPeriodStartAndPeriodEnd(
                                accountId,
                                periodStart,
                                periodEnd
                        )
        ).thenReturn(false);

        when(
                interestAccrualCommandRepository
                        .findByAccountIdAndBusinessDateBetweenAndPostingIdIsNull(
                                accountId,
                                periodStart,
                                periodEnd
                        )
        ).thenReturn(List.of());

        handler.execute(command);

        verify(interestAccrualCommandRepository)
                .findByAccountIdAndBusinessDateBetweenAndPostingIdIsNull(
                        accountId,
                        periodStart,
                        periodEnd
                );

        verifyNoInteractions(
                interestTransactionPort
        );

        verify(
                interestPostingCommandRepository,
                never()
        ).save(any());

        verify(
                interestAccrualCommandRepository,
                never()
        ).saveAll(any());
    }

    @Test
    void shouldCreatePostingWithoutTransactionWhenInterestIsZero() {
        UUID runId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID postingId = UUID.randomUUID();

        LocalDate periodStart =
                LocalDate.of(2026, 8, 1);

        LocalDate periodEnd =
                LocalDate.of(2026, 8, 31);

        InterestAccrual accrual =
                createAccrual(
                        accountId,
                        LocalDate.of(2026, 8, 31),
                        "0.0000",
                        "VND"
                );

        PostInterestCommand command =
                new PostInterestCommand(
                        runId,
                        accountId,
                        periodStart,
                        periodEnd
                );

        when(
                interestPostingCommandRepository
                        .existsByAccountIdAndPeriodStartAndPeriodEnd(
                                accountId,
                                periodStart,
                                periodEnd
                        )
        ).thenReturn(false);

        when(
                interestAccrualCommandRepository
                        .findByAccountIdAndBusinessDateBetweenAndPostingIdIsNull(
                                accountId,
                                periodStart,
                                periodEnd
                        )
        ).thenReturn(List.of(accrual));

        InterestPosting savedPosting =
                InterestPosting.builder()
                        .id(postingId)
                        .runId(runId)
                        .accountId(accountId)
                        .periodStart(periodStart)
                        .periodEnd(periodEnd)
                        .interestAmount(BigDecimal.ZERO)
                        .build();

        when(
                interestPostingCommandRepository.save(
                        any(InterestPosting.class)
                )
        ).thenReturn(savedPosting);

        handler.execute(command);

        ArgumentCaptor<InterestPosting> postingCaptor =
                ArgumentCaptor.forClass(InterestPosting.class);

        verify(
                interestPostingCommandRepository,
                times(1)
        ).save(postingCaptor.capture());

        InterestPosting posting =
                postingCaptor.getValue();

        assertThat(posting.getRunId())
                .isEqualTo(runId);

        assertThat(posting.getAccountId())
                .isEqualTo(accountId);

        assertThat(posting.getInterestAmount())
                .isZero();

        verifyNoInteractions(
                interestTransactionPort
        );

        verify(interestAccrualCommandRepository)
                .saveAll(List.of(accrual));

        assertThat(accrual.getPostingId())
                .isEqualTo(postingId);

        assertThat(savedPosting.getRunId())
                .isEqualTo(runId);

        assertThat(savedPosting.getTransactionId())
                .isNull();
    }

    @Test
    void shouldRejectDifferentCurrencies() {
        UUID runId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        LocalDate periodStart =
                LocalDate.of(2026, 8, 1);

        LocalDate periodEnd =
                LocalDate.of(2026, 8, 31);

        InterestAccrual vndAccrual =
                createAccrual(
                        accountId,
                        LocalDate.of(2026, 8, 30),
                        "1000.0000",
                        "VND"
                );

        InterestAccrual usdAccrual =
                createAccrual(
                        accountId,
                        LocalDate.of(2026, 8, 31),
                        "10.0000",
                        "USD"
                );

        PostInterestCommand command =
                new PostInterestCommand(
                        runId,
                        accountId,
                        periodStart,
                        periodEnd
                );

        when(
                interestPostingCommandRepository
                        .existsByAccountIdAndPeriodStartAndPeriodEnd(
                                accountId,
                                periodStart,
                                periodEnd
                        )
        ).thenReturn(false);

        when(
                interestAccrualCommandRepository
                        .findByAccountIdAndBusinessDateBetweenAndPostingIdIsNull(
                                accountId,
                                periodStart,
                                periodEnd
                        )
        ).thenReturn(List.of(vndAccrual, usdAccrual));

        assertThatThrownBy(
                () -> handler.execute(command)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Interest accruals must have the same currency"
                );

        verify(
                interestPostingCommandRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                interestTransactionPort
        );

        verify(
                interestAccrualCommandRepository,
                never()
        ).saveAll(any());
    }

    @Test
    void shouldRejectNullCommand() {
        assertThatThrownBy(
                () -> handler.execute(null)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("command must not be null");

        verifyNoInteractions(
                interestAccrualCommandRepository,
                interestPostingCommandRepository,
                interestTransactionPort
        );
    }

    @Test
    void shouldRejectMissingRunId() {
        PostInterestCommand command =
                new PostInterestCommand(
                        null,
                        UUID.randomUUID(),
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        assertThatThrownBy(
                () -> handler.execute(command)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("runId must not be null");

        verifyNoInteractions(
                interestAccrualCommandRepository,
                interestPostingCommandRepository,
                interestTransactionPort
        );
    }

    @Test
    void shouldRejectMissingAccountId() {
        PostInterestCommand command =
                new PostInterestCommand(
                        UUID.randomUUID(),
                        null,
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)
                );

        assertThatThrownBy(
                () -> handler.execute(command)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("accountId must not be null");

        verifyNoInteractions(
                interestAccrualCommandRepository,
                interestPostingCommandRepository,
                interestTransactionPort
        );
    }

    @Test
    void shouldRejectMissingPeriodStart() {
        PostInterestCommand command =
                new PostInterestCommand(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        LocalDate.of(2026, 8, 31)
                );

        assertThatThrownBy(
                () -> handler.execute(command)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("periodStart must not be null");

        verifyNoInteractions(
                interestAccrualCommandRepository,
                interestPostingCommandRepository,
                interestTransactionPort
        );
    }

    @Test
    void shouldRejectMissingPeriodEnd() {
        PostInterestCommand command =
                new PostInterestCommand(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        LocalDate.of(2026, 8, 1),
                        null
                );

        assertThatThrownBy(
                () -> handler.execute(command)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("periodEnd must not be null");

        verifyNoInteractions(
                interestAccrualCommandRepository,
                interestPostingCommandRepository,
                interestTransactionPort
        );
    }

    @Test
    void shouldRejectInvalidPeriod() {
        PostInterestCommand command =
                new PostInterestCommand(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        LocalDate.of(2026, 8, 31),
                        LocalDate.of(2026, 8, 1)
                );

        assertThatThrownBy(
                () -> handler.execute(command)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "periodStart must not be after periodEnd"
                );

        verifyNoInteractions(
                interestAccrualCommandRepository,
                interestPostingCommandRepository,
                interestTransactionPort
        );
    }

    private InterestAccrual createAccrual(
            UUID accountId,
            LocalDate businessDate,
            String interestAmount,
            String currency
    ) {
        return InterestAccrual.builder()
                .id(UUID.randomUUID())
                .accountId(accountId)
                .currency(currency)
                .businessDate(businessDate)
                .interestAmount(
                        new BigDecimal(interestAmount)
                )
                .build();
    }
}