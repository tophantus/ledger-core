package com.example.ledgercore.interest.command.handler;

import com.example.ledgercore.common.currency.Currency;
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
                        Currency.VND
                );

        InterestAccrual accrual2 =
                createAccrual(
                        accountId,
                        LocalDate.of(2026, 8, 31),
                        "2000.0000",
                        Currency.VND
                );

        PostInterestCommand command =
                command(
                        runId,
                        accountId,
                        periodStart,
                        periodEnd
                );

        mockNoExistingPosting(
                accountId,
                periodStart,
                periodEnd
        );

        mockUnpostedAccruals(
                accountId,
                periodStart,
                periodEnd,
                accrual1,
                accrual2
        );

        InterestPosting savedPosting =
                savedPosting(
                        postingId,
                        runId,
                        accountId,
                        periodStart,
                        periodEnd,
                        "3000"
                );

        when(
                interestPostingCommandRepository.save(
                        any(InterestPosting.class)
                )
        ).thenReturn(savedPosting);

        when(
                interestTransactionPort.postInterest(
                        accountId,
                        new BigDecimal("3000"),
                        Currency.VND,
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
                .isEqualByComparingTo("3000");

        verify(interestTransactionPort)
                .postInterest(
                        accountId,
                        new BigDecimal("3000"),
                        Currency.VND,
                        periodEnd
                );

        InterestPosting updatedPosting =
                postingCaptor.getAllValues().get(1);

        assertThat(updatedPosting.getTransactionId())
                .isEqualTo(transactionId);

        assertThat(updatedPosting.getPostedAt())
                .isNotNull();

        verify(interestAccrualCommandRepository)
                .saveAll(List.of(accrual1, accrual2));

        assertThat(accrual1.getPostingId())
                .isEqualTo(postingId);

        assertThat(accrual2.getPostingId())
                .isEqualTo(postingId);
    }

    @Test
    void shouldRoundTotalInterestBeforePosting() {
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
                        "10.123",
                        Currency.USD
                );

        InterestAccrual accrual2 =
                createAccrual(
                        accountId,
                        LocalDate.of(2026, 8, 31),
                        "20.456",
                        Currency.USD
                );

        mockNoExistingPosting(
                accountId,
                periodStart,
                periodEnd
        );

        mockUnpostedAccruals(
                accountId,
                periodStart,
                periodEnd,
                accrual1,
                accrual2
        );

        InterestPosting savedPosting =
                savedPosting(
                        postingId,
                        runId,
                        accountId,
                        periodStart,
                        periodEnd,
                        "30.58"
                );

        when(
                interestPostingCommandRepository.save(
                        any(InterestPosting.class)
                )
        ).thenReturn(savedPosting);

        when(
                interestTransactionPort.postInterest(
                        accountId,
                        new BigDecimal("30.58"),
                        Currency.USD,
                        periodEnd
                )
        ).thenReturn(transactionId);

        handler.execute(
                command(
                        runId,
                        accountId,
                        periodStart,
                        periodEnd
                )
        );

        ArgumentCaptor<InterestPosting> captor =
                ArgumentCaptor.forClass(InterestPosting.class);

        verify(
                interestPostingCommandRepository,
                times(2)
        ).save(captor.capture());

        InterestPosting posting =
                captor.getAllValues().getFirst();

        assertThat(posting.getInterestAmount())
                .isEqualByComparingTo("30.58");

        verify(interestTransactionPort)
                .postInterest(
                        accountId,
                        new BigDecimal("30.58"),
                        Currency.USD,
                        periodEnd
                );
    }

    @Test
    void shouldRoundTotalInterestAfterSummingAccruals() {
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
                        "10.005",
                        Currency.USD
                );

        InterestAccrual accrual2 =
                createAccrual(
                        accountId,
                        LocalDate.of(2026, 8, 31),
                        "10.005",
                        Currency.USD
                );

        mockNoExistingPosting(
                accountId,
                periodStart,
                periodEnd
        );

        mockUnpostedAccruals(
                accountId,
                periodStart,
                periodEnd,
                accrual1,
                accrual2
        );

        InterestPosting savedPosting =
                savedPosting(
                        postingId,
                        runId,
                        accountId,
                        periodStart,
                        periodEnd,
                        "20.01"
                );

        when(
                interestPostingCommandRepository.save(
                        any(InterestPosting.class)
                )
        ).thenReturn(savedPosting);

        when(
                interestTransactionPort.postInterest(
                        accountId,
                        new BigDecimal("20.01"),
                        Currency.USD,
                        periodEnd
                )
        ).thenReturn(transactionId);

        handler.execute(
                command(
                        runId,
                        accountId,
                        periodStart,
                        periodEnd
                )
        );

        ArgumentCaptor<InterestPosting> captor =
                ArgumentCaptor.forClass(InterestPosting.class);

        verify(
                interestPostingCommandRepository,
                times(2)
        ).save(captor.capture());

        assertThat(
                captor.getAllValues()
                        .getFirst()
                        .getInterestAmount()
        ).isEqualByComparingTo("20.01");

        verify(interestTransactionPort)
                .postInterest(
                        accountId,
                        new BigDecimal("20.01"),
                        Currency.USD,
                        periodEnd
                );
    }

    @Test
    void shouldCreatePostingWithoutTransactionWhenInterestRoundsToZero() {
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
                        "0.004",
                        Currency.USD
                );

        mockNoExistingPosting(
                accountId,
                periodStart,
                periodEnd
        );

        mockUnpostedAccruals(
                accountId,
                periodStart,
                periodEnd,
                accrual
        );

        InterestPosting savedPosting =
                savedPosting(
                        postingId,
                        runId,
                        accountId,
                        periodStart,
                        periodEnd,
                        "0.00"
                );

        when(
                interestPostingCommandRepository.save(
                        any(InterestPosting.class)
                )
        ).thenReturn(savedPosting);

        handler.execute(
                command(
                        runId,
                        accountId,
                        periodStart,
                        periodEnd
                )
        );

        ArgumentCaptor<InterestPosting> captor =
                ArgumentCaptor.forClass(InterestPosting.class);

        verify(
                interestPostingCommandRepository
        ).save(captor.capture());

        assertThat(captor.getValue().getInterestAmount())
                .isZero();

        verifyNoInteractions(
                interestTransactionPort
        );

        verify(interestAccrualCommandRepository)
                .saveAll(List.of(accrual));

        assertThat(accrual.getPostingId())
                .isEqualTo(postingId);

        assertThat(savedPosting.getTransactionId())
                .isNull();
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
                command(
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
                command(
                        runId,
                        accountId,
                        periodStart,
                        periodEnd
                );

        mockNoExistingPosting(
                accountId,
                periodStart,
                periodEnd
        );

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
                        Currency.VND
                );

        InterestAccrual usdAccrual =
                createAccrual(
                        accountId,
                        LocalDate.of(2026, 8, 31),
                        "10.0000",
                        Currency.USD
                );

        mockNoExistingPosting(
                accountId,
                periodStart,
                periodEnd
        );

        mockUnpostedAccruals(
                accountId,
                periodStart,
                periodEnd,
                vndAccrual,
                usdAccrual
        );

        assertThatThrownBy(
                () -> handler.execute(
                        command(
                                runId,
                                accountId,
                                periodStart,
                                periodEnd
                        )
                )
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

    private PostInterestCommand command(
            UUID runId,
            UUID accountId,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        return new PostInterestCommand(
                runId,
                accountId,
                periodStart,
                periodEnd
        );
    }

    private void mockNoExistingPosting(
            UUID accountId,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        when(
                interestPostingCommandRepository
                        .existsByAccountIdAndPeriodStartAndPeriodEnd(
                                accountId,
                                periodStart,
                                periodEnd
                        )
        ).thenReturn(false);
    }

    private void mockUnpostedAccruals(
            UUID accountId,
            LocalDate periodStart,
            LocalDate periodEnd,
            InterestAccrual... accruals
    ) {
        when(
                interestAccrualCommandRepository
                        .findByAccountIdAndBusinessDateBetweenAndPostingIdIsNull(
                                accountId,
                                periodStart,
                                periodEnd
                        )
        ).thenReturn(List.of(accruals));
    }

    private InterestPosting savedPosting(
            UUID postingId,
            UUID runId,
            UUID accountId,
            LocalDate periodStart,
            LocalDate periodEnd,
            String interestAmount
    ) {
        return InterestPosting.builder()
                .id(postingId)
                .runId(runId)
                .accountId(accountId)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .interestAmount(
                        new BigDecimal(interestAmount)
                )
                .build();
    }

    private InterestAccrual createAccrual(
            UUID accountId,
            LocalDate businessDate,
            String interestAmount,
            Currency currency
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