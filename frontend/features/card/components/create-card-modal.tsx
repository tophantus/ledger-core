"use client";

import {zodResolver} from "@hookform/resolvers/zod";
import {
    Eye,
    EyeOff,
    X,
} from "lucide-react";
import {useLocale, useTranslations} from "next-intl";
import {
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import {useForm, useWatch} from "react-hook-form";

import {Button} from "@/components/ui/button";
import {AccountStatus} from "@/features/account/types/account";
import {useAccountStore} from "@/features/account/stores/account-store";
import {useGetCreditFacility} from "@/features/credit/hooks/use-get-credit-facility";
import type {GetUserCreditFacilityResult} from "@/features/credit/types/credit-facility";

import {useCreateCreditCard} from "../hooks/use-create-credit-card";
import {useCreateDebitCard} from "../hooks/use-create-debit-card";
import {
    createCreateCardSchema,
    type CreateCardFormValues,
} from "../schemas/create-card-schema";
import {
    CardType,
    type CardInfo,
} from "../types/card";
import {useMyAccounts} from "@/features/account/hooks/use-my-accounts";
import {formatMoney} from "@/lib/utils/currency";
import {AccountSelect} from "@/features/account/components/account-select";

interface CreateCardModalProps {
    open: boolean;
    cards: CardInfo[];
    onClose: () => void;
    onCreated: () => void;
}

export function CreateCardModal({
                                    open,
                                    cards,
                                    onClose,
                                    onCreated,
                                }: CreateCardModalProps) {
    const t = useTranslations("card");
    const tErrors = useTranslations("errors");

    const locale = useLocale();

    const accounts = useAccountStore(
        (state) => state.accounts,
    );

    const accountsInitialized =
        useAccountStore(
            (state) => state.initialized,
        );

    const {getMyAccounts} =
        useMyAccounts();

    useEffect(() => {
        if (
            !open ||
            accountsInitialized
        ) {
            return;
        }

        void getMyAccounts();
    }, [
        open,
        accountsInitialized,
        getMyAccounts,
    ]);

    const {getFacility} =
        useGetCreditFacility();

    const {createDebitCard} =
        useCreateDebitCard();

    const {createCreditCard} =
        useCreateCreditCard();

    const [
        creditFacility,
        setCreditFacility,
    ] =
        useState<GetUserCreditFacilityResult | null>(
            null,
        );

    const [
        facilityLoading,
        setFacilityLoading,
    ] = useState(false);

    const [
        errorMessage,
        setErrorMessage,
    ] = useState<string | null>(null);

    const [showPin, setShowPin] =
        useState(false);

    const schema = useMemo(
        () =>
            createCreateCardSchema(
                t(
                    "create.validation.accountRequired",
                ),
                t(
                    "create.validation.creditFacilityRequired",
                ),
                t(
                    "create.validation.pinRequired",
                ),
                t(
                    "create.validation.pinInvalid",
                ),
            ),
        [t],
    );

    const {
        register,
        handleSubmit,
        reset,
        setValue,
        control,
        formState: {
            errors,
            isSubmitting,
        },
    } = useForm<CreateCardFormValues>({
        resolver: zodResolver(schema),
        defaultValues: {
            type: CardType.DEBIT,
            accountId: "",
            creditFacilityId: "",
            pin: "",
        },
    });

    const type = useWatch({
        control,
        name: "type",
    });

    const isDebit =
        type === CardType.DEBIT;

    const isCredit =
        type === CardType.CREDIT;

    const debitCards = useMemo(
        () =>
            cards.filter(
                (card) =>
                    card.type ===
                    CardType.DEBIT,
            ),
        [cards],
    );

    const creditCards = useMemo(
        () =>
            cards.filter(
                (card) =>
                    card.type ===
                    CardType.CREDIT,
            ),
        [cards],
    );

    const availableAccounts =
        useMemo(() => {
            const cardAccountIds =
                new Set(
                    debitCards
                        .map(
                            (card) =>
                                card.accountId,
                        )
                        .filter(
                            (
                                accountId,
                            ): accountId is string =>
                                accountId !==
                                null,
                        ),
                );

            return accounts.filter(
                (account) =>
                    account.status ===
                    AccountStatus.ACTIVE &&
                    !cardAccountIds.has(
                        account.id,
                    ),
            );
        }, [
            accounts,
            debitCards,
        ]);

    const hasAvailableAccount =
        availableAccounts.length > 0;

    const hasCreditCard =
        creditCards.length > 0;

    const loadCreditFacility =
        useCallback(async () => {
            setFacilityLoading(true);
            setErrorMessage(null);

            try {
                const response =
                    await getFacility();

                if (!response.success) {
                    setCreditFacility(null);

                    setErrorMessage(
                        response.code &&
                        tErrors.has(
                            response.code,
                        )
                            ? tErrors(
                                response.code,
                            )
                            : tErrors(
                                "fallback",
                            ),
                    );

                    return;
                }

                setCreditFacility(
                    response.data,
                );

                setValue(
                    "creditFacilityId",
                    response.data.id,
                );
            } catch {
                setCreditFacility(null);

                setErrorMessage(
                    tErrors("fallback"),
                );
            } finally {
                setFacilityLoading(false);
            }
        }, [
            getFacility,
            setValue,
            tErrors,
        ]);

    const handleTypeChange = (
        nextType: CardType,
    ) => {
        setValue(
            "type",
            nextType,
        );

        setValue(
            "accountId",
            "",
        );

        setValue(
            "creditFacilityId",
            "",
        );

        setErrorMessage(null);
        setCreditFacility(null);

        if (
            nextType === CardType.CREDIT &&
            !hasCreditCard
        ) {
            void loadCreditFacility();
        }
    };

    const handleClose = () => {
        if (isSubmitting) {
            return;
        }

        reset({
            type: CardType.DEBIT,
            accountId: "",
            creditFacilityId: "",
            pin: "",
        });

        setCreditFacility(null);
        setErrorMessage(null);
        setShowPin(false);

        onClose();
    };

    const handleCreate = async (
        values: CreateCardFormValues,
    ) => {
        setErrorMessage(null);

        try {
            const response =
                values.type ===
                CardType.DEBIT
                    ? await createDebitCard({
                        accountId:
                        values.accountId,
                        pin: values.pin,
                    })
                    : await createCreditCard({
                        creditFacilityId:
                        values.creditFacilityId,
                        pin: values.pin,
                    });

            if (!response.success) {
                setErrorMessage(
                    response.code &&
                    tErrors.has(
                        response.code,
                    )
                        ? tErrors(
                            response.code,
                        )
                        : tErrors(
                            "fallback",
                        ),
                );

                return;
            }

            onCreated();
            handleClose();
        } catch {
            setErrorMessage(
                tErrors("fallback"),
            );
        }
    };

    const canCreate =
        isDebit
            ? hasAvailableAccount
            : !hasCreditCard &&
            !!creditFacility;

    if (!open) {
        return null;
    }

    return (
        <div
            className="
                fixed
                inset-0
                z-50
                flex
                items-center
                justify-center
                bg-black/60
                p-4
                backdrop-blur-sm
            "
            onMouseDown={(event) => {
                if (
                    event.target ===
                    event.currentTarget
                ) {
                    handleClose();
                }
            }}
        >
            <div
                className="
                    relative
                    w-full
                    max-w-md
                    rounded-2xl
                    border
                    border-border
                    bg-surface
                    p-6
                    shadow-xl
                "
            >
                {/* Header */}
                <div className="flex items-start justify-between">
                    <div>
                        <h2
                            className="
                                text-lg
                                font-semibold
                                text-text-primary
                            "
                        >
                            {t(
                                "create.title",
                            )}
                        </h2>

                        <p
                            className="
                                mt-1
                                text-sm
                                text-text-muted
                            "
                        >
                            {t(
                                "create.description",
                            )}
                        </p>
                    </div>

                    <button
                        type="button"
                        onClick={handleClose}
                        disabled={
                            isSubmitting
                        }
                        className="
                            rounded-full
                            p-2
                            text-text-muted
                            transition-colors
                            hover:bg-surface-subtle
                            hover:text-text-primary
                            disabled:cursor-not-allowed
                            disabled:opacity-50
                        "
                        aria-label={t(
                            "actions.close",
                        )}
                    >
                        <X className="size-5" />
                    </button>
                </div>

                <form
                    onSubmit={handleSubmit(
                        handleCreate,
                    )}
                    className="mt-6 space-y-5"
                >
                    {/* API error */}
                    {errorMessage && (
                        <div
                            className="
                                rounded-md
                                border
                                border-danger/20
                                bg-danger-subtle
                                px-4
                                py-3
                                text-sm
                                text-danger
                            "
                        >
                            {errorMessage}
                        </div>
                    )}

                    {/* Card type */}
                    <div>
                        <p
                            className="
                                text-sm
                                font-medium
                                text-text-primary
                            "
                        >
                            {t(
                                "create.type.label",
                            )}
                        </p>

                        <div className="mt-2 grid grid-cols-2 gap-3">
                            <button
                                type="button"
                                onClick={() =>
                                    handleTypeChange(
                                        CardType.DEBIT,
                                    )
                                }
                                disabled={
                                    isSubmitting
                                }
                                className={`
                                    rounded-lg
                                    border
                                    px-4
                                    py-3
                                    text-sm
                                    font-medium
                                    transition
                                    disabled:cursor-not-allowed
                                    disabled:opacity-60
                                    ${
                                    isDebit
                                        ? "border-primary bg-primary/10 text-primary"
                                        : "border-border text-text-muted hover:bg-surface-subtle"
                                }
                                `}
                            >
                                {t(
                                    "types.DEBIT",
                                )}
                            </button>

                            <button
                                type="button"
                                onClick={() =>
                                    handleTypeChange(
                                        CardType.CREDIT,
                                    )
                                }
                                disabled={
                                    isSubmitting
                                }
                                className={`
                                    rounded-lg
                                    border
                                    px-4
                                    py-3
                                    text-sm
                                    font-medium
                                    transition
                                    disabled:cursor-not-allowed
                                    disabled:opacity-60
                                    ${
                                    isCredit
                                        ? "border-primary bg-primary/10 text-primary"
                                        : "border-border text-text-muted hover:bg-surface-subtle"
                                }
                                `}
                            >
                                {t(
                                    "types.CREDIT",
                                )}
                            </button>
                        </div>
                    </div>

                    {/* Debit */}
                    {isDebit && (
                        <div>
                            {!hasAvailableAccount ? (
                                <div
                                    className="
                                        rounded-lg
                                        border
                                        border-border
                                        bg-surface-subtle
                                        p-4
                                    "
                                >
                                    <p className="text-sm text-text-muted">
                                        {t(
                                            "create.debit.noAvailableAccount",
                                        )}
                                    </p>
                                </div>
                            ) : (
                                <div>
                                    <label
                                        htmlFor="card-account"
                                        className="
                                            text-sm
                                            font-medium
                                            text-text-primary
                                        "
                                    >
                                        {t(
                                            "create.debit.account",
                                        )}
                                    </label>

                                    <AccountSelect
                                        accounts={availableAccounts}
                                        registration={register("accountId")}
                                        disabled={isSubmitting}
                                        id="card-account"
                                        placeholder={t(
                                            "create.debit.accountPlaceholder",
                                        )}
                                        error={errors.accountId?.message}
                                    />
                                </div>
                            )}
                        </div>
                    )}

                    {/* Credit */}
                    {isCredit && (
                        <div>
                            {hasCreditCard ? (
                                <div
                                    className="
                                        rounded-lg
                                        border
                                        border-border
                                        bg-surface-subtle
                                        p-4
                                    "
                                >
                                    <p className="text-sm text-text-muted">
                                        {t(
                                            "create.credit.alreadyHasCard",
                                        )}
                                    </p>
                                </div>
                            ) : facilityLoading ? (
                                <div
                                    className="
                                        rounded-lg
                                        border
                                        border-border
                                        bg-surface-subtle
                                        p-4
                                    "
                                >
                                    <p className="text-sm text-text-muted">
                                        {t(
                                            "create.credit.loading",
                                        )}
                                    </p>
                                </div>
                            ) : !creditFacility ? (
                                <div
                                    className="
                                        rounded-lg
                                        border
                                        border-border
                                        bg-surface-subtle
                                        p-4
                                    "
                                >
                                    <p className="text-sm text-text-muted">
                                        {t(
                                            "create.credit.noFacility",
                                        )}
                                    </p>
                                </div>
                            ) : (
                                <div>
                                    <p
                                        className="
                                            text-sm
                                            font-medium
                                            text-text-primary
                                        "
                                    >
                                        {t(
                                            "create.credit.facility",
                                        )}
                                    </p>

                                    <div
                                        className="
                                            mt-2
                                            rounded-lg
                                            border
                                            border-border
                                            bg-background
                                            p-4
                                        "
                                    >
                                        <p
                                            className="
                                                text-sm
                                                font-medium
                                                text-text-primary
                                            "
                                        >
                                            {formatMoney(
                                                    creditFacility.creditLimit,
                                                    creditFacility.currency,
                                                    locale
                                                )
                                            }
                                        </p>

                                        <p
                                            className="
                                                mt-1
                                                text-xs
                                                text-text-muted
                                            "
                                        >
                                            {t(
                                                "create.credit.outstanding",
                                            )}{" "}
                                            {formatMoney(
                                                creditFacility.outstandingBalance,
                                                creditFacility.currency,
                                                locale
                                            )
                                            }
                                        </p>
                                    </div>

                                    <input
                                        type="hidden"
                                        {...register(
                                            "creditFacilityId",
                                        )}
                                    />
                                </div>
                            )}
                        </div>
                    )}

                    {/* PIN */}
                    {canCreate && (
                        <div>
                            <label
                                htmlFor="create-card-pin"
                                className="
                                    text-sm
                                    font-medium
                                    text-text-primary
                                "
                            >
                                {t("pin")}
                            </label>

                            <div className="relative mt-2">
                                <input
                                    id="create-card-pin"
                                    type={
                                        showPin
                                            ? "text"
                                            : "password"
                                    }
                                    inputMode="numeric"
                                    maxLength={6}
                                    autoComplete="off"
                                    {...register(
                                        "pin",
                                    )}
                                    disabled={
                                        isSubmitting
                                    }
                                    className="
                                        w-full
                                        rounded-lg
                                        border
                                        border-border
                                        bg-background
                                        px-3
                                        py-2.5
                                        pr-10
                                        text-sm
                                        text-text-primary
                                        outline-none
                                        transition
                                        focus:ring-2
                                        focus:ring-primary/20
                                        disabled:cursor-not-allowed
                                        disabled:opacity-60
                                    "
                                    placeholder={t(
                                        "create.pinPlaceholder",
                                    )}
                                />

                                <button
                                    type="button"
                                    onClick={() =>
                                        setShowPin(
                                            (
                                                value,
                                            ) =>
                                                !value,
                                        )
                                    }
                                    disabled={
                                        isSubmitting
                                    }
                                    className="
                                        absolute
                                        right-2
                                        top-1/2
                                        -translate-y-1/2
                                        rounded
                                        p-1.5
                                        text-text-muted
                                        transition-colors
                                        hover:text-text-primary
                                        disabled:cursor-not-allowed
                                        disabled:opacity-50
                                    "
                                    aria-label={
                                        showPin
                                            ? t(
                                                "actions.hidePin",
                                            )
                                            : t(
                                                "actions.showPin",
                                            )
                                    }
                                >
                                    {showPin ? (
                                        <EyeOff className="size-4" />
                                    ) : (
                                        <Eye className="size-4" />
                                    )}
                                </button>
                            </div>

                            {errors.pin && (
                                <p className="mt-1.5 text-sm text-danger">
                                    {
                                        errors
                                            .pin
                                            .message
                                    }
                                </p>
                            )}
                        </div>
                    )}

                    {/* Submit */}
                    {canCreate && (
                        <Button
                            type="submit"
                            className="w-full"
                            loading={
                                isSubmitting
                            }
                        >
                            {t(
                                "create.action",
                            )}
                        </Button>
                    )}
                </form>
            </div>
        </div>
    );
}