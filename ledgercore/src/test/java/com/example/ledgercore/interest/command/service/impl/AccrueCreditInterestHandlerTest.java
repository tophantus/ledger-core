
package com.example.ledgercore.interest.command.service.impl;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.interest.command.port.outbound.InterestJournalPort;
import com.example.ledgercore.interest.command.repository.CreditInterestAccrualCommandRepository;
import com.example.ledgercore.interest.command.service.dto.AccrueCreditInterestCommand;
import com.example.ledgercore.interest.entity.CreditInterestAccrual;
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
class AccrueCreditInterestHandlerTest {

    @Mock
    private InterestConfigService interestConfigService;

    @Mock
    private InterestCalculationService interestCalculationService;

    @Mock
    private CreditInterestAccrualCommandRepository repository;

    @Mock
    private InterestJournalPort interestJournalPort;

    private AccrueCreditInterestHandler handler;

    private UUID runId;
    private UUID creditFacilityId;
    private UUID productId;
    private LocalDate businessDate;
    private AccrueCreditInterestCommand command;

    @BeforeEach
    void setUp() {
        handler = new AccrueCreditInterestHandler(
                interestConfigService,
                interestCalculationService,
                repository,
                interestJournalPort
        );

        runId = UUID.randomUUID();
        creditFacilityId = UUID.randomUUID();
        productId = UUID.randomUUID();
        businessDate = LocalDate.of(2026, 9, 28);

        command = validCommand(
                runId,
                creditFacilityId,
                productId,
                Currency.VND,
                new BigDecimal("100000000.00"),
                businessDate
        );
    }

    @Test
    void shouldAccrueCreditInterestAndRecordJournal() {
        BigDecimal principal = new BigDecimal("100000000.00");
        BigDecimal interestAmount = new BigDecimal("8219.18");
        InterestConfig config = interestConfig();

        UUID accrualId = UUID.randomUUID();
        UUID journalEntryId = UUID.randomUUID();

        CreditInterestAccrual savedAccrual =
                CreditInterestAccrual.builder()
                        .id(accrualId)
                        .build();

        givenNoExistingAccrual();
        givenApplicableConfig(config);
        givenCalculatedInterest(
                principal,
                config,
                interestAmount
        );

        when(repository.save(any(CreditInterestAccrual.class)))
                .thenReturn(savedAccrual);

        when(interestJournalPort.recordAccrualJournal(
                accrualId,
                businessDate,
                Currency.VND,
                interestAmount
        )).thenReturn(journalEntryId);

        handler.execute(command);

        ArgumentCaptor<CreditInterestAccrual> captor =
                ArgumentCaptor.forClass(CreditInterestAccrual.class);

        verify(repository, times(2)).save(captor.capture());

        CreditInterestAccrual firstSaved =
                captor.getAllValues().get(0);
        CreditInterestAccrual secondSaved =
                captor.getAllValues().get(1);

        assertThat(firstSaved.getRunId()).isEqualTo(runId);
        assertThat(firstSaved.getCreditFacilityId())
                .isEqualTo(creditFacilityId);
        assertThat(firstSaved.getBusinessDate())
                .isEqualTo(businessDate);
        assertThat(firstSaved.getPrincipalAmount())
                .isEqualByComparingTo(principal);
        assertThat(firstSaved.getInterestRate())
                .isEqualByComparingTo("0.030000");
        assertThat(firstSaved.getInterestAmount())
                .isEqualByComparingTo(interestAmount);

        assertThat(secondSaved.getJournalEntryId())
                .isEqualTo(journalEntryId);

        verify(interestJournalPort).recordAccrualJournal(
                accrualId,
                businessDate,
                Currency.VND,
                interestAmount
        );

        verify(repository).findByCreditFacilityIdAndBusinessDate(
                creditFacilityId,
                businessDate
        );
        verify(interestConfigService).getApplicableConfig(
                productId,
                Currency.VND,
                businessDate
        );
        verify(interestCalculationService).calculateDailyInterest(
                principal,
                config.getInterestRate(),
                config.getDayCountConvention()
        );
        verifyNoMoreInteractions(
                repository,
                interestJournalPort,
                interestConfigService,
                interestCalculationService
        );
    }

    @Test
    void shouldSaveZeroInterestWithoutRecordingJournal() {
        BigDecimal principal = BigDecimal.ZERO;
        BigDecimal interestAmount = BigDecimal.ZERO;
        InterestConfig config = interestConfig();

        CreditInterestAccrual savedAccrual =
                CreditInterestAccrual.builder()
                        .id(UUID.randomUUID())
                        .build();

        givenNoExistingAccrual();
        givenApplicableConfig(config);
        givenCalculatedInterest(
                principal,
                config,
                interestAmount
        );

        when(repository.save(any(CreditInterestAccrual.class)))
                .thenReturn(savedAccrual);

        handler.execute(commandWithBalance(principal));

        ArgumentCaptor<CreditInterestAccrual> captor =
                ArgumentCaptor.forClass(CreditInterestAccrual.class);

        verify(repository).save(captor.capture());

        CreditInterestAccrual accrual = captor.getValue();

        assertThat(accrual.getRunId()).isEqualTo(runId);
        assertThat(accrual.getCreditFacilityId())
                .isEqualTo(creditFacilityId);
        assertThat(accrual.getPrincipalAmount())
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(accrual.getInterestAmount())
                .isEqualByComparingTo(BigDecimal.ZERO);

        verifyNoInteractions(interestJournalPort);
        verify(repository, never()).save(
                argThat(saved -> saved.getJournalEntryId() != null)
        );
    }

    @Test
    void shouldSkipWhenAccrualAlreadyExists() {
        when(repository.findByCreditFacilityIdAndBusinessDate(
                creditFacilityId,
                businessDate
        )).thenReturn(
                Optional.of(CreditInterestAccrual.builder().build())
        );

        handler.execute(command);

        verify(repository).findByCreditFacilityIdAndBusinessDate(
                creditFacilityId,
                businessDate
        );

        verify(repository, never()).save(any());
        verifyNoInteractions(
                interestConfigService,
                interestCalculationService,
                interestJournalPort
        );
    }

    @Test
    void shouldRejectNullCommand() {
        assertInvalidCommand(
                null,
                "command must not be null"
        );
    }

    @Test
    void shouldRejectNullRunId() {
        assertInvalidCommand(
                validCommand(
                        null,
                        creditFacilityId,
                        productId,
                        Currency.VND,
                        BigDecimal.TEN,
                        businessDate
                ),
                "runId must not be null"
        );
    }

    @Test
    void shouldRejectNullCreditFacilityId() {
        assertInvalidCommand(
                validCommand(
                        runId,
                        null,
                        productId,
                        Currency.VND,
                        BigDecimal.TEN,
                        businessDate
                ),
                "creditFacilityId must not be null"
        );
    }

    @Test
    void shouldRejectNullProductId() {
        assertInvalidCommand(
                validCommand(
                        runId,
                        creditFacilityId,
                        null,
                        Currency.VND,
                        BigDecimal.TEN,
                        businessDate
                ),
                "productId must not be null"
        );
    }

    @Test
    void shouldRejectNullCurrency() {
        assertInvalidCommand(
                validCommand(
                        runId,
                        creditFacilityId,
                        productId,
                        null,
                        BigDecimal.TEN,
                        businessDate
                ),
                "currency must not be null"
        );
    }

    @Test
    void shouldRejectNullClosingBalance() {
        assertInvalidCommand(
                validCommand(
                        runId,
                        creditFacilityId,
                        productId,
                        Currency.VND,
                        null,
                        businessDate
                ),
                "closingBalance must not be null"
        );
    }

    @Test
    void shouldRejectNegativeClosingBalance() {
        assertInvalidCommand(
                commandWithBalance(new BigDecimal("-0.01")),
                "closingBalance must not be negative"
        );
    }

    @Test
    void shouldRejectNullBusinessDate() {
        assertInvalidCommand(
                validCommand(
                        runId,
                        creditFacilityId,
                        productId,
                        Currency.VND,
                        BigDecimal.TEN,
                        null
                ),
                "businessDate must not be null"
        );
    }

    private void givenNoExistingAccrual() {
        when(repository.findByCreditFacilityIdAndBusinessDate(
                creditFacilityId,
                businessDate
        )).thenReturn(Optional.empty());
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
            BigDecimal interestAmount
    ) {
        when(interestCalculationService.calculateDailyInterest(
                principal,
                config.getInterestRate(),
                config.getDayCountConvention()
        )).thenReturn(interestAmount);
    }

    private InterestConfig interestConfig() {
        return InterestConfig.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .currency(Currency.VND)
                .interestRate(new BigDecimal("0.030000"))
                .dayCountConvention(DayCountConvention.ACTUAL_365)
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .build();
    }

    private AccrueCreditInterestCommand commandWithBalance(
            BigDecimal closingBalance
    ) {
        return validCommand(
                runId,
                creditFacilityId,
                productId,
                Currency.VND,
                closingBalance,
                businessDate
        );
    }

    private AccrueCreditInterestCommand validCommand(
            UUID commandRunId,
            UUID commandCreditFacilityId,
            UUID commandProductId,
            Currency currency,
            BigDecimal closingBalance,
            LocalDate date
    ) {
        return new AccrueCreditInterestCommand(
                commandRunId,
                commandCreditFacilityId,
                commandProductId,
                currency,
                closingBalance,
                date
        );
    }

    private void assertInvalidCommand(
            AccrueCreditInterestCommand invalidCommand,
            String expectedMessage
    ) {
        assertThatThrownBy(() -> handler.execute(invalidCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expectedMessage);

        verifyNoInteractions(
                repository,
                interestConfigService,
                interestCalculationService,
                interestJournalPort
        );
    }
}