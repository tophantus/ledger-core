
package com.example.ledgercore.interest.command.service.impl;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.interest.command.port.outbound.InterestJournalPort;
import com.example.ledgercore.interest.command.repository.InterestAccrualCommandRepository;
import com.example.ledgercore.interest.command.service.dto.AccrueInterestCommand;
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
    private BigDecimal closingBalance;
    private AccrueInterestCommand command;

    private static final BigDecimal INTEREST_RATE =
            new BigDecimal("0.030000");

    private static final BigDecimal INTEREST_AMOUNT =
            new BigDecimal("8219.1781");

    @BeforeEach
    void setUp() {
        handler = new AccrueInterestHandler(
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

        closingBalance = new BigDecimal("100000000");
        businessDate = LocalDate.of(2026, 9, 7);

        command = validCommand();
    }

    // -------------------------------------------------------------------------
    // Success cases
    // -------------------------------------------------------------------------

    @Test
    void execute_shouldAccrueInterestAndRecordJournal_whenInterestIsPositive() {
        InterestConfig config = validConfig();
        UUID accrualId = UUID.randomUUID();

        InterestAccrual savedAccrual = InterestAccrual.builder()
                .id(accrualId)
                .runId(runId)
                .accountId(accountId)
                .currency(Currency.VND)
                .businessDate(businessDate)
                .interestConfigId(configId)
                .principalAmount(closingBalance)
                .interestRate(INTEREST_RATE)
                .interestAmount(INTEREST_AMOUNT)
                .build();

        givenNoExistingAccrual();
        givenApplicableConfig(config);
        givenCalculatedInterest(
                closingBalance,
                config,
                INTEREST_AMOUNT
        );
        when(interestAccrualCommandRepository.save(any(InterestAccrual.class)))
                .thenReturn(savedAccrual);

        when(interestJournalPort.recordAccrualJournal(
                accrualId,
                businessDate,
                Currency.VND,
                INTEREST_AMOUNT
        )).thenReturn(journalEntryId);

        handler.execute(command);

        ArgumentCaptor<InterestAccrual> captor =
                ArgumentCaptor.forClass(InterestAccrual.class);

        verify(interestAccrualCommandRepository, times(2))
                .save(captor.capture());

        InterestAccrual firstSaved = captor.getAllValues().get(0);
        assertThat(firstSaved.getId()).isNull();
        assertThat(firstSaved.getRunId()).isEqualTo(runId);
        assertThat(firstSaved.getAccountId()).isEqualTo(accountId);
        assertThat(firstSaved.getCurrency()).isEqualTo(Currency.VND);
        assertThat(firstSaved.getBusinessDate()).isEqualTo(businessDate);
        assertThat(firstSaved.getInterestConfigId()).isEqualTo(configId);
        assertThat(firstSaved.getPrincipalAmount())
                .isEqualByComparingTo(closingBalance);
        assertThat(firstSaved.getInterestRate())
                .isEqualByComparingTo(INTEREST_RATE);
        assertThat(firstSaved.getInterestAmount())
                .isEqualByComparingTo(INTEREST_AMOUNT);

        verify(interestJournalPort).recordAccrualJournal(
                accrualId,
                businessDate,
                Currency.VND,
                INTEREST_AMOUNT
        );

        InterestAccrual secondSaved = captor.getAllValues().get(1);
        assertThat(secondSaved).isSameAs(savedAccrual);
        assertThat(secondSaved.getJournalEntryId())
                .isEqualTo(journalEntryId);

        verify(interestAccrualCommandRepository)
                .findByAccountIdAndBusinessDate(accountId, businessDate);
        verify(interestConfigService)
                .getApplicableConfig(productId, Currency.VND, businessDate);
        verify(interestCalculationService)
                .calculateDailyInterest(
                        closingBalance,
                        config.getInterestRate(),
                        config.getDayCountConvention()
                );
    }

    @Test
    void execute_shouldSaveZeroInterestWithoutRecordingJournal() {
        BigDecimal principal = BigDecimal.ZERO;
        BigDecimal zeroInterest = BigDecimal.ZERO;
        InterestConfig config = validConfig();

        InterestAccrual savedAccrual = InterestAccrual.builder()
                .id(UUID.randomUUID())
                .runId(runId)
                .accountId(accountId)
                .currency(Currency.VND)
                .businessDate(businessDate)
                .interestConfigId(configId)
                .principalAmount(principal)
                .interestRate(INTEREST_RATE)
                .interestAmount(zeroInterest)
                .build();

        givenNoExistingAccrual();
        givenApplicableConfig(config);
        givenCalculatedInterest(principal, config, zeroInterest);
        when(interestAccrualCommandRepository.save(any(InterestAccrual.class)))
                .thenReturn(savedAccrual);

        handler.execute(commandWithBalance(principal));

        ArgumentCaptor<InterestAccrual> captor =
                ArgumentCaptor.forClass(InterestAccrual.class);

        verify(interestAccrualCommandRepository)
                .save(captor.capture());

        InterestAccrual saved = captor.getValue();
        assertThat(saved.getRunId()).isEqualTo(runId);
        assertThat(saved.getAccountId()).isEqualTo(accountId);
        assertThat(saved.getPrincipalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saved.getInterestAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saved.getJournalEntryId()).isNull();

        verifyNoInteractions(interestJournalPort);
    }

    @Test
    void execute_shouldSkip_whenAccrualAlreadyExists() {
        InterestAccrual existing = InterestAccrual.builder()
                .id(UUID.randomUUID())
                .runId(runId)
                .accountId(accountId)
                .businessDate(businessDate)
                .build();

        when(interestAccrualCommandRepository
                .findByAccountIdAndBusinessDate(accountId, businessDate))
                .thenReturn(Optional.of(existing));

        handler.execute(command);

        verify(interestAccrualCommandRepository)
                .findByAccountIdAndBusinessDate(accountId, businessDate);

        verifyNoInteractions(
                interestConfigService,
                interestCalculationService,
                interestJournalPort
        );

        verify(interestAccrualCommandRepository, never())
                .save(any(InterestAccrual.class));
    }

    // -------------------------------------------------------------------------
    // Command validation
    // -------------------------------------------------------------------------

    @Test
    void execute_shouldRejectNullCommand() {
        assertInvalidCommand(
                null,
                "command must not be null"
        );
    }

    @Test
    void execute_shouldRejectNullRunId() {
        assertInvalidCommand(
                new AccrueInterestCommand(
                        null,
                        accountId,
                        productId,
                        Currency.VND,
                        closingBalance,
                        businessDate
                ),
                "runId must not be null"
        );
    }

    @Test
    void execute_shouldRejectNullAccountId() {
        assertInvalidCommand(
                new AccrueInterestCommand(
                        runId,
                        null,
                        productId,
                        Currency.VND,
                        closingBalance,
                        businessDate
                ),
                "accountId must not be null"
        );
    }

    @Test
    void execute_shouldRejectNullProductId() {
        assertInvalidCommand(
                new AccrueInterestCommand(
                        runId,
                        accountId,
                        null,
                        Currency.VND,
                        closingBalance,
                        businessDate
                ),
                "productId must not be null"
        );
    }

    @Test
    void execute_shouldRejectNullCurrency() {
        assertInvalidCommand(
                new AccrueInterestCommand(
                        runId,
                        accountId,
                        productId,
                        null,
                        closingBalance,
                        businessDate
                ),
                "currency must not be blank"
        );
    }

    @Test
    void execute_shouldRejectNullClosingBalance() {
        assertInvalidCommand(
                new AccrueInterestCommand(
                        runId,
                        accountId,
                        productId,
                        Currency.VND,
                        null,
                        businessDate
                ),
                "closingBalance must not be null"
        );
    }

    @Test
    void execute_shouldRejectNegativeClosingBalance() {
        assertInvalidCommand(
                new AccrueInterestCommand(
                        runId,
                        accountId,
                        productId,
                        Currency.VND,
                        new BigDecimal("-0.01"),
                        businessDate
                ),
                "closingBalance must not be negative"
        );
    }

    @Test
    void execute_shouldRejectNullBusinessDate() {
        assertInvalidCommand(
                new AccrueInterestCommand(
                        runId,
                        accountId,
                        productId,
                        Currency.VND,
                        closingBalance,
                        null
                ),
                "businessDate must not be null"
        );
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    private AccrueInterestCommand validCommand() {
        return new AccrueInterestCommand(
                runId,
                accountId,
                productId,
                Currency.VND,
                closingBalance,
                businessDate
        );
    }

    private AccrueInterestCommand commandWithBalance(BigDecimal balance) {
        return new AccrueInterestCommand(
                runId,
                accountId,
                productId,
                Currency.VND,
                balance,
                businessDate
        );
    }

    private InterestConfig validConfig() {
        return InterestConfig.builder()
                .id(configId)
                .productId(productId)
                .currency(Currency.VND)
                .interestRate(INTEREST_RATE)
                .dayCountConvention(DayCountConvention.ACTUAL_365)
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();
    }

    private void givenNoExistingAccrual() {
        when(interestAccrualCommandRepository
                .findByAccountIdAndBusinessDate(accountId, businessDate))
                .thenReturn(Optional.empty());
    }

    private void givenApplicableConfig(InterestConfig config) {
        when(interestConfigService.getApplicableConfig(
                productId,
                Currency.VND,
                businessDate
        )).thenReturn(config);
    }

    private void givenCalculatedInterest(
            BigDecimal principal,
            InterestConfig config,
            BigDecimal interest
    ) {
        when(interestCalculationService.calculateDailyInterest(
                principal,
                config.getInterestRate(),
                config.getDayCountConvention()
        )).thenReturn(interest);
    }

    private void assertInvalidCommand(
            AccrueInterestCommand invalidCommand,
            String expectedMessage
    ) {
        assertThatThrownBy(() -> handler.execute(invalidCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expectedMessage);

        verifyNoInteractions(
                interestAccrualCommandRepository,
                interestConfigService,
                interestCalculationService,
                interestJournalPort
        );
    }
}