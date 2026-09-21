import type {CreditOfferStatus} from "@/features/credit/types/credit-offer";
import type {CreditFacilityStatus} from "@/features/credit/types/credit-facility";

type Variant =
    | "text"
    | "background"
    | "text-background";

const CREDIT_OFFER_STATUS_COLORS: Record<
    CreditOfferStatus,
    Record<Variant, string>
> = {
    OFFERED: {
        text: "text-primary",
        background: "bg-primary",
        "text-background":
            "text-primary bg-primary/10",
    },
    ACCEPTED: {
        text: "text-success",
        background: "bg-success",
        "text-background":
            "text-success bg-success/10",
    },
    EXPIRED: {
        text: "text-warning",
        background: "bg-warning",
        "text-background":
            "text-warning bg-warning/10",
    },
    REJECTED: {
        text: "text-danger",
        background: "bg-danger",
        "text-background":
            "text-danger bg-danger/10",
    },
    CANCELLED: {
        text: "text-danger",
        background: "bg-danger",
        "text-background":
            "text-danger bg-danger/10",
    },
};

const CREDIT_FACILITY_STATUS_COLORS: Record<
    CreditFacilityStatus,
    Record<Variant, string>
> = {
    ACTIVE: {
        text: "text-success",
        background: "bg-success",
        "text-background":
            "text-success bg-success/10",
    },
    SUSPENDED: {
        text: "text-warning",
        background: "bg-warning",
        "text-background":
            "text-warning bg-warning/10",
    },
    CLOSED: {
        text: "text-danger",
        background: "bg-danger",
        "text-background":
            "text-danger bg-danger/10",
    },
};

export function getCreditOfferStatusColor(
    status: CreditOfferStatus,
    variant: Variant,
): string {
    return CREDIT_OFFER_STATUS_COLORS[status][
        variant
        ];
}

export function getCreditFacilityStatusColor(
    status: CreditFacilityStatus,
    variant: Variant,
): string {
    return CREDIT_FACILITY_STATUS_COLORS[status][
        variant
        ];
}