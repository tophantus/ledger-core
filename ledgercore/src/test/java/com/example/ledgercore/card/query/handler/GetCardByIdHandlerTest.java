package com.example.ledgercore.card.query.handler;

import com.example.ledgercore.card.entity.Card;
import com.example.ledgercore.card.enums.CardForm;
import com.example.ledgercore.card.enums.CardStatus;
import com.example.ledgercore.card.enums.CardType;
import com.example.ledgercore.card.query.dto.CardInfo;
import com.example.ledgercore.card.query.dto.GetCardByIdQuery;
import com.example.ledgercore.card.query.repository.CardQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCardByIdHandlerTest {

    @Mock
    private CardQueryRepository cardQueryRepository;

    private GetCardByIdHandler handler;

    private UUID customerId;
    private UUID cardId;

    private GetCardByIdQuery query;

    @BeforeEach
    void setUp() {
        handler = new GetCardByIdHandler(
                cardQueryRepository
        );

        customerId = UUID.randomUUID();
        cardId = UUID.randomUUID();

        query = new GetCardByIdQuery(
                customerId,
                cardId
        );
    }

    @Test
    void shouldGetCardByIdSuccessfully() {
        Card card = givenCard();

        when(cardQueryRepository.findByIdAndCustomerId(
                cardId,
                customerId
        )).thenReturn(Optional.of(card));

        CardInfo result = handler.execute(query);

        assertNotNull(result);

        assertEquals(card.getId(), result.cardId());
        assertEquals(
                card.getCustomerId(),
                result.customerId()
        );
        assertEquals(
                card.getType(),
                result.type()
        );
        assertEquals(
                card.getForm(),
                result.form()
        );
        assertEquals(
                card.getStatus(),
                result.status()
        );
        assertEquals(
                card.getAccountId(),
                result.accountId()
        );
        assertEquals(
                card.getCreditFacilityId(),
                result.creditFacilityId()
        );
        assertEquals(
                card.getPanLast4(),
                result.panLast4()
        );
        assertEquals(
                card.getExpiryMonth(),
                result.expiryMonth()
        );
        assertEquals(
                card.getExpiryYear(),
                result.expiryYear()
        );
        assertEquals(
                card.getIssuedAt(),
                result.issuedAt()
        );
        assertEquals(
                card.getActivatedAt(),
                result.activatedAt()
        );
        assertEquals(
                card.getClosedAt(),
                result.closedAt()
        );

        verify(cardQueryRepository)
                .findByIdAndCustomerId(
                        cardId,
                        customerId
                );

        verifyNoMoreInteractions(cardQueryRepository);
    }

    @Test
    void shouldThrowWhenCardDoesNotExist() {
        when(cardQueryRepository.findByIdAndCustomerId(
                cardId,
                customerId
        )).thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(query)
                );

        assertEquals(
                ErrorCode.CARD_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(cardQueryRepository)
                .findByIdAndCustomerId(
                        cardId,
                        customerId
                );

        verifyNoMoreInteractions(cardQueryRepository);
    }

    @Test
    void shouldReturnCardInfoWithNullableFields() {
        Card card = givenCardWithNullableFields();

        when(cardQueryRepository.findByIdAndCustomerId(
                cardId,
                customerId
        )).thenReturn(Optional.of(card));

        CardInfo result = handler.execute(query);

        assertNotNull(result);

        assertEquals(cardId, result.cardId());
        assertEquals(customerId, result.customerId());

        assertNull(result.accountId());
        assertNull(result.creditFacilityId());
        assertNull(result.activatedAt());
        assertNull(result.closedAt());

        assertEquals("1111", result.panLast4());

        verify(cardQueryRepository)
                .findByIdAndCustomerId(
                        cardId,
                        customerId
                );

        verifyNoMoreInteractions(cardQueryRepository);
    }

    private Card givenCard() {
        UUID accountId = UUID.randomUUID();

        Instant issuedAt = Instant.parse(
                "2026-09-20T10:00:00Z"
        );

        Instant activatedAt = Instant.parse(
                "2026-09-20T10:05:00Z"
        );

        Instant closedAt = Instant.parse(
                "2026-09-20T10:10:00Z"
        );

        return Card.builder()
                .id(cardId)
                .customerId(customerId)
                .type(CardType.DEBIT)
                .form(CardForm.PHYSICAL)
                .status(CardStatus.ACTIVE)
                .accountId(accountId)
                .creditFacilityId(null)
                .panLast4("1111")
                .expiryMonth((short) 9)
                .expiryYear((short) 2031)
                .issuedAt(issuedAt)
                .activatedAt(activatedAt)
                .closedAt(closedAt)
                .createdAt(issuedAt)
                .updatedAt(activatedAt)
                .build();
    }

    private Card givenCardWithNullableFields() {
        Instant issuedAt = Instant.parse(
                "2026-09-20T10:00:00Z"
        );

        return Card.builder()
                .id(cardId)
                .customerId(customerId)
                .type(CardType.DEBIT)
                .form(CardForm.PHYSICAL)
                .status(CardStatus.ACTIVE)
                .accountId(null)
                .creditFacilityId(null)
                .panLast4("1111")
                .expiryMonth((short) 9)
                .expiryYear((short) 2031)
                .issuedAt(issuedAt)
                .activatedAt(null)
                .closedAt(null)
                .createdAt(issuedAt)
                .updatedAt(issuedAt)
                .build();
    }
}