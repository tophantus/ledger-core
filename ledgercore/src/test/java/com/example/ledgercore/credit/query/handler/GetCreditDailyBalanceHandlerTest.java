package com.example.ledgercore.credit.query.handler;

import com.example.ledgercore.credit.query.dto.CreditDailyBalanceResponse;
import com.example.ledgercore.credit.query.repository.CreditDailyBalanceQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCreditDailyBalanceHandlerTest {

    @Mock
    private CreditDailyBalanceQueryRepository repository;

    private GetCreditDailyBalanceHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetCreditDailyBalanceHandler(repository);
    }

    @Test
    void shouldReturnEffectiveClosingBalance() {
        UUID creditFacilityId = UUID.randomUUID();
        LocalDate businessDate = LocalDate.of(2026, 9, 28);
        BigDecimal closingBalance = new BigDecimal("123456.78");
        when(repository.findEffectiveClosingBalance(
                creditFacilityId, businessDate
        )).thenReturn(closingBalance);

        CreditDailyBalanceResponse result = handler.execute(
                creditFacilityId,
                businessDate
        );

        assertThat(result.creditFacilityId()).isEqualTo(creditFacilityId);
        assertThat(result.businessDate()).isEqualTo(businessDate);
        assertThat(result.closingBalance())
                .isEqualByComparingTo(closingBalance);
        verify(repository).findEffectiveClosingBalance(
                creditFacilityId,
                businessDate
        );
    }

    @Test
    void shouldRejectNullCreditFacilityId() {
        assertThatThrownBy(() -> handler.execute(
                null,
                LocalDate.of(2026, 9, 28)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("creditFacilityId must not be null");

        verifyNoInteractions(repository);
    }
}
