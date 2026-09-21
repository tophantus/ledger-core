export enum CardType {
    DEBIT = "DEBIT",
    CREDIT = "CREDIT",
}

export enum CardForm {
    PHYSICAL = "PHYSICAL",
    VIRTUAL = "VIRTUAL",
}

export enum CardStatus {
    ACTIVE = "ACTIVE",
    INACTIVE = "INACTIVE",
    BLOCKED = "BLOCKED",
    CLOSED = "CLOSED",
}

export interface CardInfo {
    cardId: string;
    customerId: string;
    type: CardType;
    form: CardForm;
    status: CardStatus;
    accountId: string | null;
    creditFacilityId: string | null;
    panLast4: string;
    expiryMonth: number;
    expiryYear: number;
    issuedAt: string;
    activatedAt: string | null;
    closedAt: string | null;
}

export interface CreateDebitCardRequest {
    accountId: string;
    pin: string;
}

export interface CreateCreditCardRequest {
    creditFacilityId: string;
    pin: string;
}

export interface CreateDebitCardResult {
    cardId: string;
    accountId: string;
    type: CardType;
    form: CardForm;
    status: CardStatus;
    panLast4: string;
    expiryMonth: number;
    expiryYear: number;
    issuedAt: string;
}

export interface CreateCreditCardResult {
    cardId: string;
    creditFacilityId: string;
    type: CardType;
    form: CardForm;
    status: CardStatus;
    panLast4: string;
    expiryMonth: number;
    expiryYear: number;
    issuedAt: string;
}

export interface RevealedCardDetails {
    cardId: string;
    pan: string;
    cvv: string;
}