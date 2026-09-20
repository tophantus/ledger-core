package com.example.ledgercore.card.query.handler;

import com.example.ledgercore.card.entity.Card;
import com.example.ledgercore.card.enums.CardForm;
import com.example.ledgercore.card.enums.CardStatus;
import com.example.ledgercore.card.enums.CardType;
import com.example.ledgercore.card.query.dto.CardInfo;
import com.example.ledgercore.card.query.dto.GetUserCardsQuery;
import com.example.ledgercore.card.query.repository.CardQueryRepository;
import com.example.ledgercore.common.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserCardsHandlerTest {

    @Mock
    private CardQueryRepository cardQueryRepository;

    private GetUserCardsHandler handler;

    private UUID customerId;

    @BeforeEach
    void setUp() {
        handler = new GetUserCardsHandler(
                cardQueryRepository
        );

        customerId = UUID.randomUUID();
    }

    @Test
    void shouldGetUserCardsSuccessfully() {
        int page = 0;
        int size = 20;

        GetUserCardsQuery query =
                new GetUserCardsQuery(
                        customerId,
                        page,
                        size
                );

        Card firstCard = givenCard();
        Card secondCard = givenCard();

        Page<Card> cardPage =
                new PageImpl<>(
                        List.of(
                                firstCard,
                                secondCard
                        ),
                        PageRequestHelper.of(page, size),
                        2
                );

        when(cardQueryRepository.findByCustomerId(
                eq(customerId),
                any(Pageable.class)
        )).thenReturn(cardPage);

        PageResponse<CardInfo> result =
                handler.execute(query);

        assertNotNull(result);

        assertEquals(2, result.content().size());
        assertEquals(0, result.page());
        assertEquals(20, result.size());
        assertEquals(2, result.totalElements());
        assertEquals(1, result.totalPages());

        CardInfo firstResult =
                result.content().get(0);

        assertEquals(
                firstCard.getId(),
                firstResult.cardId()
        );

        assertEquals(
                firstCard.getCustomerId(),
                firstResult.customerId()
        );

        assertEquals(
                firstCard.getType(),
                firstResult.type()
        );

        assertEquals(
                firstCard.getForm(),
                firstResult.form()
        );

        assertEquals(
                firstCard.getStatus(),
                firstResult.status()
        );

        CardInfo secondResult =
                result.content().get(1);

        assertEquals(
                secondCard.getId(),
                secondResult.cardId()
        );

        assertEquals(
                secondCard.getPanLast4(),
                secondResult.panLast4()
        );

        verify(cardQueryRepository)
                .findByCustomerId(
                        eq(customerId),
                        any(Pageable.class)
                );

        verifyNoMoreInteractions(cardQueryRepository);
    }

    @Test
    void shouldCreatePageableWithCorrectPageAndSize() {
        int page = 2;
        int size = 10;

        GetUserCardsQuery query =
                new GetUserCardsQuery(
                        customerId,
                        page,
                        size
                );

        Page<Card> cardPage =
                new PageImpl<>(
                        List.of(),
                        PageRequestHelper.of(page, size),
                        25
                );

        when(cardQueryRepository.findByCustomerId(
                eq(customerId),
                any(Pageable.class)
        )).thenReturn(cardPage);

        handler.execute(query);

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(cardQueryRepository)
                .findByCustomerId(
                        eq(customerId),
                        pageableCaptor.capture()
                );

        Pageable pageable =
                pageableCaptor.getValue();

        assertEquals(page, pageable.getPageNumber());
        assertEquals(size, pageable.getPageSize());

        assertEquals(
                "createdAt",
                pageable.getSort()
                        .getOrderFor("createdAt")
                        .getProperty()
        );

        assertEquals(
                org.springframework.data.domain.Sort.Direction.DESC,
                pageable.getSort()
                        .getOrderFor("createdAt")
                        .getDirection()
        );

        verifyNoMoreInteractions(cardQueryRepository);
    }

    @Test
    void shouldReturnEmptyPageWhenUserHasNoCards() {
        int page = 0;
        int size = 20;

        GetUserCardsQuery query =
                new GetUserCardsQuery(
                        customerId,
                        page,
                        size
                );

        Page<Card> emptyPage =
                new PageImpl<>(
                        List.of(),
                        PageRequestHelper.of(page, size),
                        0
                );

        when(cardQueryRepository.findByCustomerId(
                eq(customerId),
                any(Pageable.class)
        )).thenReturn(emptyPage);

        PageResponse<CardInfo> result =
                handler.execute(query);

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.page());
        assertEquals(20, result.size());
        assertEquals(0, result.totalElements());
        assertEquals(0, result.totalPages());

        verify(cardQueryRepository)
                .findByCustomerId(
                        eq(customerId),
                        any(Pageable.class)
                );

        verifyNoMoreInteractions(cardQueryRepository);
    }

    @Test
    void shouldMapAllCardFields() {
        int page = 0;
        int size = 20;

        GetUserCardsQuery query =
                new GetUserCardsQuery(
                        customerId,
                        page,
                        size
                );

        Card card = Card.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .type(CardType.CREDIT)
                .form(CardForm.PHYSICAL)
                .status(CardStatus.BLOCKED)
                .accountId(null)
                .creditFacilityId(UUID.randomUUID())
                .panLast4("1234")
                .expiryMonth((short) 12)
                .expiryYear((short) 2031)
                .issuedAt(
                        Instant.parse(
                                "2026-01-01T10:00:00Z"
                        )
                )
                .activatedAt(
                        Instant.parse(
                                "2026-01-01T10:05:00Z"
                        )
                )
                .closedAt(
                        Instant.parse(
                                "2026-06-01T10:00:00Z"
                        )
                )
                .createdAt(
                        Instant.parse(
                                "2026-01-01T10:00:00Z"
                        )
                )
                .updatedAt(
                        Instant.parse(
                                "2026-06-01T10:00:00Z"
                        )
                )
                .build();

        Page<Card> cardPage =
                new PageImpl<>(
                        List.of(card),
                        PageRequestHelper.of(page, size),
                        1
                );

        when(cardQueryRepository.findByCustomerId(
                eq(customerId),
                any(Pageable.class)
        )).thenReturn(cardPage);

        PageResponse<CardInfo> result =
                handler.execute(query);

        assertEquals(1, result.content().size());

        CardInfo cardInfo =
                result.content().getFirst();

        assertEquals(
                card.getId(),
                cardInfo.cardId()
        );

        assertEquals(
                card.getCustomerId(),
                cardInfo.customerId()
        );

        assertEquals(
                card.getType(),
                cardInfo.type()
        );

        assertEquals(
                card.getForm(),
                cardInfo.form()
        );

        assertEquals(
                card.getStatus(),
                cardInfo.status()
        );

        assertEquals(
                card.getAccountId(),
                cardInfo.accountId()
        );

        assertEquals(
                card.getCreditFacilityId(),
                cardInfo.creditFacilityId()
        );

        assertEquals(
                card.getPanLast4(),
                cardInfo.panLast4()
        );

        assertEquals(
                card.getExpiryMonth(),
                cardInfo.expiryMonth()
        );

        assertEquals(
                card.getExpiryYear(),
                cardInfo.expiryYear()
        );

        assertEquals(
                card.getIssuedAt(),
                cardInfo.issuedAt()
        );

        assertEquals(
                card.getActivatedAt(),
                cardInfo.activatedAt()
        );

        assertEquals(
                card.getClosedAt(),
                cardInfo.closedAt()
        );

        verify(cardQueryRepository)
                .findByCustomerId(
                        eq(customerId),
                        any(Pageable.class)
                );

        verifyNoMoreInteractions(cardQueryRepository);
    }

    private Card givenCard() {
        return Card.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .type(CardType.DEBIT)
                .form(CardForm.PHYSICAL)
                .status(CardStatus.ACTIVE)
                .accountId(UUID.randomUUID())
                .creditFacilityId(null)
                .panLast4("1111")
                .expiryMonth((short) 9)
                .expiryYear((short) 2031)
                .issuedAt(
                        Instant.parse(
                                "2026-09-20T10:00:00Z"
                        )
                )
                .activatedAt(
                        Instant.parse(
                                "2026-09-20T10:05:00Z"
                        )
                )
                .closedAt(null)
                .createdAt(
                        Instant.parse(
                                "2026-09-20T10:00:00Z"
                        )
                )
                .updatedAt(
                        Instant.parse(
                                "2026-09-20T10:05:00Z"
                        )
                )
                .build();
    }

    private static final class PageRequestHelper {

        private PageRequestHelper() {
        }

        static Pageable of(int page, int size) {
            return org.springframework.data.domain.PageRequest.of(
                    page,
                    size
            );
        }
    }
}