package com.example.ledgercore.interest.command.service.impl;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.interest.command.dto.AccrueCreditInterestCommand;
import com.example.ledgercore.interest.command.port.outbound.InterestJournalPort;
import com.example.ledgercore.interest.command.port.outbound.credit.CreditDailyBalanceInfo;
import com.example.ledgercore.interest.command.port.outbound.credit.CreditDailyBalancePort;
import com.example.ledgercore.interest.command.repository.CreditInterestAccrualCommandRepository;
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
    private CreditDailyBalancePort creditDailyBalancePort;

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
                creditDailyBalancePort,
                interestConfigService,
                interestCalculationService,
                repository,
                interestJournalPort
        );
        runId = UUID.randomUUID();
        creditFacilityId = UUID.randomUUID();
        productId = UUID.randomUUID();
        businessDate = LocalDate.of(2026, 9, 28);
        command = new AccrueCreditInterestCommand(
                runId,
                creditFacilityId,
                productId,
                Currency.VND,
                businessDate
        );
    }

    @Test
    void shouldAccrueCreditInterestAndRecordJournal() {
        BigDecimal principal = new BigDecimal("100000000.00");
        BigDecimal interestAmount = new BigDecimal("8219.1781");
        InterestConfig config = interestConfig();
        CreditInterestAccrual savedAccrual = CreditInterestAccrual.builder()
                .id(UUID.randomUUID())
                .build();
        UUID journalEntryId = UUID.randomUUID();

        when(repository.findByCreditFacilityIdAndBusinessDate(
                creditFacilityId, businessDate
        )).thenReturn(Optional.empty());
        when(creditDailyBalancePort.findClosingBalance(
                creditFacilityId, businessDate
        )).thenReturn(new CreditDailyBalanceInfo(
                creditFacilityId, businessDate, principal
        ));
        when(interestConfigService.getApplicableConfig(
                productId, Currency.VND, businessDate
        )).thenReturn(config);
        when(interestCalculationService.calculateDailyInterest(
                principal, config.getInterestRate(), config.getDayCountConvention()
        )).thenReturn(interestAmount);
        when(repository.save(any(CreditInterestAccrual.class)))
                .thenReturn(savedAccrual);
        when(interestJournalPort.recordAccrualJournal(
                savedAccrual.getId(), businessDate, Currency.VND, interestAmount
        )).thenReturn(journalEntryId);

        handler.execute(command);

        ArgumentCaptor<CreditInterestAccrual> captor =
                ArgumentCaptor.forClass(CreditInterestAccrual.class);
        verify(repository, times(2)).save(captor.capture());

        CreditInterestAccrual created = captor.getAllValues().getFirst();
        assertThat(created.getRunId()).isEqualTo(runId);
        assertThat(created.getCreditFacilityId()).isEqualTo(creditFacilityId);
        assertThat(created.getBusinessDate()).isEqualTo(businessDate);
        assertThat(created.getPrincipalAmount())
                .isEqualByComparingTo(principal);
        assertThat(created.getInterestRate())
                .isEqualByComparingTo("0.030000");
        assertThat(created.getInterestAmount())
                .isEqualByComparingTo(interestAmount);
        assertThat(captor.getAllValues().get(1).getJournalEntryId())
                .isEqualTo(journalEntryId);
    }

    @Test
    void shouldSaveZeroInterestWithoutRecordingJournal() {
        InterestConfig config = interestConfig();
        CreditInterestAccrual savedAccrual = CreditInterestAccrual.builder()
                .id(UUID.randomUUID())
                .build();

        when(repository.findByCreditFacilityIdAndBusinessDate(
                creditFacilityId, businessDate
        )).thenReturn(Optional.empty());
        when(creditDailyBalancePort.findClosingBalance(
                creditFacilityId, businessDate
        )).thenReturn(new CreditDailyBalanceInfo(
                creditFacilityId, businessDate, BigDecimal.ZERO
        ));
        when(interestConfigService.getApplicableConfig(
                productId, Currency.VND, businessDate
        )).thenReturn(config);
        when(interestCalculationService.calculateDailyInterest(
                BigDecimal.ZERO,
                config.getInterestRate(),
                config.getDayCountConvention()
        )).thenReturn(BigDecimal.ZERO);
        when(repository.save(any(CreditInterestAccrual.class)))
                .thenReturn(savedAccrual);

        handler.execute(command);

        verify(repository).save(any(CreditInterestAccrual.class));
        verifyNoInteractions(interestJournalPort);
    }

    @Test
    void shouldSkipWhenAccrualAlreadyExists() {
        when(repository.findByCreditFacilityIdAndBusinessDate(
                creditFacilityId, businessDate
        )).thenReturn(Optional.of(CreditInterestAccrual.builder().build()));

        handler.execute(command);

        verifyNoInteractions(
                creditDailyBalancePort,
                interestConfigService,
                interestCalculationService,
                interestJournalPort
        );
        verify(repository, never()).save(any(CreditInterestAccrual.class));
    }

    @Test
    void shouldRejectCommandWithoutCreditFacilityId() {
        AccrueCreditInterestCommand invalidCommand =
                new AccrueCreditInterestCommand(
                        runId,
                        null,
                        productId,
                        Currency.VND,
                        businessDate
                );

        assertThatThrownBy(() -> handler.execute(invalidCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("creditFacilityId must not be null");

        verifyNoInteractions(
                repository,
                creditDailyBalancePort,
                interestConfigService,
                interestCalculationService,
                interestJournalPort
        );
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
}
