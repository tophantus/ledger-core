package com.example.ledgercore.interest.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.interest.command.dto.AccrueInterestCommand;
import com.example.ledgercore.interest.command.port.outbound.AccountDailyBalanceInfo;
import com.example.ledgercore.interest.command.port.outbound.AccountDailyBalancePort;
import com.example.ledgercore.interest.command.port.outbound.InterestJournalPort;
import com.example.ledgercore.interest.command.repository.InterestAccrualCommandRepository;
import com.example.ledgercore.interest.entity.InterestAccrual;
import com.example.ledgercore.interest.entity.InterestConfig;
import com.example.ledgercore.interest.enums.DayCountConvention;
import com.example.ledgercore.interest.service.InterestCalculationService;
import com.example.ledgercore.interest.service.InterestConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccrueInterestHandlerTest {

    @Mock
    private AccountDailyBalancePort accountDailyBalancePort;

    @Mock
    private InterestConfigService interestConfigService;

    @Mock
    private InterestCalculationService interestCalculationService;

    @Mock
    private InterestAccrualCommandRepository interestAccrualCommandRepository;

    @Mock
    private InterestJournalPort interestJournalPort;

    private AccrueInterestHandler handler;

    private UUID runId;
    private UUID accountId;
    private UUID productId;
    private UUID configId;
    private UUID journalEntryId;
    private LocalDate businessDate;

    private AccrueInterestCommand command;

    @BeforeEach
    void setUp() {
        handler = new AccrueInterestHandler(
                accountDailyBalancePort,
                interestConfigService,
                interestCalculationService,
                interestAccrualCommandRepository,
                interestJournalPort
        );

        runId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        productId = UUID.randomUUID();
        configId = UUID.randomUUID();
        journalEntryId = UUID.randomUUID();
        businessDate = LocalDate.of(2026, 9, 7);

        command = new AccrueInterestCommand(
                runId,
                accountId,
                productId,
                Currency.VND,
                businessDate
        );
    }

    @Test
    void shouldAccrueInterestAndRecordJournal() {
        BigDecimal principal =
                new BigDecimal("100000000");

        BigDecimal interestAmount =
                new BigDecimal("8219.1781");

        AccountDailyBalanceInfo dailyBalance =
                new AccountDailyBalanceInfo(
                        accountId,
                        businessDate,
                        principal
                );

        InterestConfig config =
                InterestConfig.builder()
                        .id(configId)
                        .productId(productId)
                        .currency(Currency.VND)
                        .interestRate(
                                new BigDecimal("0.030000")
                        )
                        .dayCountConvention(
                                DayCountConvention.ACTUAL_365
                        )
                        .effectiveFrom(
                                LocalDate.of(2026, 1, 1)
                        )
                        .build();

        InterestAccrual savedAccrual =
                InterestAccrual.builder()
                        .id(UUID.randomUUID())
                        .runId(runId)
                        .accountId(accountId)
                        .currency(Currency.VND)
                        .businessDate(businessDate)
                        .interestConfigId(configId)
                        .principalAmount(principal)
                        .interestRate(
                                new BigDecimal("0.030000")
                        )
                        .interestAmount(interestAmount)
                        .build();

        when(
                interestAccrualCommandRepository
                        .findByAccountIdAndBusinessDate(
                                accountId,
                                businessDate
                        )
        ).thenReturn(Optional.empty());

        when(
                accountDailyBalancePort.findClosingBalance(
                        accountId,
                        businessDate
                )
        ).thenReturn(dailyBalance);

        when(
                interestConfigService.getApplicableConfig(
                        productId,
                        Currency.VND,
                        businessDate
                )
        ).thenReturn(config);

        when(
                interestCalculationService.calculateDailyInterest(
                        principal,
                        config.getInterestRate(),
                        config.getDayCountConvention()
                )
        ).thenReturn(interestAmount);

        when(
                interestAccrualCommandRepository.save(
                        any(InterestAccrual.class)
                )
        ).thenReturn(savedAccrual);

        when(
                interestJournalPort.recordAccrualJournal(
                        savedAccrual.getId(),
                        businessDate,
                        Currency.VND,
                        interestAmount
                )
        ).thenReturn(journalEntryId);

        handler.execute(command);

        ArgumentCaptor<InterestAccrual> captor =
                ArgumentCaptor.forClass(InterestAccrual.class);

        verify(
                interestAccrualCommandRepository,
                times(2)
        ).save(captor.capture());

        InterestAccrual firstSaved =
                captor.getAllValues().getFirst();

        assertThat(firstSaved.getRunId())
                .isEqualTo(runId);
        assertThat(firstSaved.getAccountId())
                .isEqualTo(accountId);
        assertThat(firstSaved.getBusinessDate())
                .isEqualTo(businessDate);
        assertThat(firstSaved.getInterestConfigId())
                .isEqualTo(configId);
        assertThat(firstSaved.getPrincipalAmount())
                .isEqualByComparingTo(principal);
        assertThat(firstSaved.getInterestRate())
                .isEqualByComparingTo("0.030000");
        assertThat(firstSaved.getInterestAmount())
                .isEqualByComparingTo(interestAmount);

        verify(interestJournalPort)
                .recordAccrualJournal(
                        savedAccrual.getId(),
                        businessDate,
                        Currency.VND,
                        interestAmount
                );

        InterestAccrual secondSaved =
                captor.getAllValues().get(1);

        assertThat(secondSaved.getJournalEntryId())
                .isEqualTo(journalEntryId);
    }

    @Test
    void shouldSaveZeroInterestWithoutRecordingJournal() {
        BigDecimal principal = BigDecimal.ZERO;
        BigDecimal interestAmount = BigDecimal.ZERO;

        AccountDailyBalanceInfo dailyBalance =
                new AccountDailyBalanceInfo(
                        accountId,
                        businessDate,
                        principal
                );

        InterestConfig config =
                InterestConfig.builder()
                        .id(configId)
                        .productId(productId)
                        .currency(Currency.VND)
                        .interestRate(
                                new BigDecimal("0.030000")
                        )
                        .dayCountConvention(
                                DayCountConvention.ACTUAL_365
                        )
                        .effectiveFrom(
                                LocalDate.of(2026, 1, 1)
                        )
                        .build();

        UUID accrualId = UUID.randomUUID();

        InterestAccrual savedAccrual =
                InterestAccrual.builder()
                        .id(accrualId)
                        .runId(runId)
                        .accountId(accountId)
                        .currency(Currency.VND)
                        .businessDate(businessDate)
                        .interestConfigId(configId)
                        .principalAmount(principal)
                        .interestRate(
                                new BigDecimal("0.030000")
                        )
                        .interestAmount(interestAmount)
                        .build();

        when(
                interestAccrualCommandRepository
                        .findByAccountIdAndBusinessDate(
                                accountId,
                                businessDate
                        )
        ).thenReturn(Optional.empty());

        when(
                accountDailyBalancePort.findClosingBalance(
                        accountId,
                        businessDate
                )
        ).thenReturn(dailyBalance);

        when(
                interestConfigService.getApplicableConfig(
                        productId,
                        Currency.VND,
                        businessDate
                )
        ).thenReturn(config);

        when(
                interestCalculationService.calculateDailyInterest(
                        principal,
                        config.getInterestRate(),
                        config.getDayCountConvention()
                )
        ).thenReturn(interestAmount);

        when(
                interestAccrualCommandRepository.save(
                        any(InterestAccrual.class)
                )
        ).thenReturn(savedAccrual);

        handler.execute(command);

        verify(
                interestAccrualCommandRepository
        ).save(any(InterestAccrual.class));

        verify(
                interestAccrualCommandRepository,
                times(1)
        ).save(any(InterestAccrual.class));

        verifyNoInteractions(interestJournalPort);
    }

    @Test
    void shouldSkipWhenAccrualAlreadyExists() {
        InterestAccrual existing =
                InterestAccrual.builder()
                        .id(UUID.randomUUID())
                        .runId(runId)
                        .accountId(accountId)
                        .businessDate(businessDate)
                        .build();

        when(
                interestAccrualCommandRepository
                        .findByAccountIdAndBusinessDate(
                                accountId,
                                businessDate
                        )
        ).thenReturn(Optional.of(existing));

        handler.execute(command);

        verify(
                interestAccrualCommandRepository
        ).findByAccountIdAndBusinessDate(
                accountId,
                businessDate
        );

        verifyNoInteractions(
                accountDailyBalancePort,
                interestConfigService,
                interestCalculationService,
                interestJournalPort
        );

        verify(
                interestAccrualCommandRepository,
                never()
        ).save(any(InterestAccrual.class));
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
                accountDailyBalancePort,
                interestConfigService,
                interestCalculationService,
                interestJournalPort
        );
    }

    @Test
    void shouldRejectNullRunId() {
        AccrueInterestCommand invalidCommand =
                new AccrueInterestCommand(
                        null,
                        accountId,
                        productId,
                        Currency.VND,
                        businessDate
                );

        assertThatThrownBy(
                () -> handler.execute(invalidCommand)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("runId must not be null");

        verifyNoInteractions(
                interestAccrualCommandRepository,
                accountDailyBalancePort,
                interestConfigService,
                interestCalculationService,
                interestJournalPort
        );
    }

    @Test
    void shouldRejectNullAccountId() {
        AccrueInterestCommand invalidCommand =
                new AccrueInterestCommand(
                        runId,
                        null,
                        productId,
                        Currency.VND,
                        businessDate
                );

        assertThatThrownBy(
                () -> handler.execute(invalidCommand)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("accountId must not be null");

        verifyNoInteractions(
                interestAccrualCommandRepository,
                accountDailyBalancePort,
                interestConfigService,
                interestCalculationService,
                interestJournalPort
        );
    }

    @Test
    void shouldRejectNullProductId() {
        AccrueInterestCommand invalidCommand =
                new AccrueInterestCommand(
                        runId,
                        accountId,
                        null,
                        Currency.VND,
                        businessDate
                );

        assertThatThrownBy(
                () -> handler.execute(invalidCommand)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("productId must not be null");

        verifyNoInteractions(
                interestAccrualCommandRepository,
                accountDailyBalancePort,
                interestConfigService,
                interestCalculationService,
                interestJournalPort
        );
    }

    @Test
    void shouldRejectNullProductCode() {
        AccrueInterestCommand invalidCommand =
                new AccrueInterestCommand(
                        runId,
                        accountId,
                        null,
                        Currency.VND,
                        businessDate
                );

        assertThatThrownBy(
                () -> handler.execute(invalidCommand)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("productId must not be null");
    }

    @Test
    void shouldRejectNullCurrency() {
        AccrueInterestCommand invalidCommand =
                new AccrueInterestCommand(
                        runId,
                        accountId,
                        productId,
                        null,
                        businessDate
                );

        assertThatThrownBy(
                () -> handler.execute(invalidCommand)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("currency must not be blank");
    }

    @Test
    void shouldRejectNullBusinessDate() {
        AccrueInterestCommand invalidCommand =
                new AccrueInterestCommand(
                        runId,
                        accountId,
                        productId,
                        Currency.VND,
                        null
                );

        assertThatThrownBy(
                () -> handler.execute(invalidCommand)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("businessDate must not be null");

        verifyNoInteractions(
                interestAccrualCommandRepository,
                accountDailyBalancePort,
                interestConfigService,
                interestCalculationService,
                interestJournalPort
        );
    }
}
