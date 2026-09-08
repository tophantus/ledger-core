"use client";

import {
    Loader2,
} from "lucide-react";
import {Controller, UseFormReturn} from "react-hook-form";
import {
    useLocale,
    useTranslations,
} from "next-intl";

import {Button} from "@/components/ui/button";
import {MoneyInput} from "@/components/ui/money-input";

import type {
    AccountHolder,
    AccountSummary,
} from "@/features/account/types/account";

import type {
    TransferDestinationForm,
    TransferDetailsForm,
} from "@/features/transaction/schemas/transfer-schema";

import {formatMoney} from "@/lib/utils/currency";

interface TransferFormProps {
    accounts: AccountSummary[];
    selectedAccount:
        | AccountSummary
        | null;
    holder: AccountHolder | null;

    isAccountsLoading: boolean;
    isHolderLoading: boolean;
    isIntentLoading: boolean;

    destinationForm:
        UseFormReturn<TransferDestinationForm>;

    detailsForm:
        UseFormReturn<TransferDetailsForm>;

    onAccountChange: (
        account: AccountSummary,
    ) => void;

    onFindHolder: () => void;
    onCreateIntent: () => void;
}

export function TransferForm({
                                 accounts,
                                 selectedAccount,
                                 holder,
                                 isAccountsLoading,
                                 isHolderLoading,
                                 isIntentLoading,
                                 destinationForm,
                                 detailsForm,
                                 onAccountChange,
                                 onFindHolder,
                                 onCreateIntent,
                             }: TransferFormProps) {
    const t =
        useTranslations("transaction");

    const locale = useLocale();

    const handleFindHolder = () => {
        const destination =
            destinationForm.getValues(
                "destinationAccountNo",
            );

        if (
            selectedAccount &&
            destination === selectedAccount.accountNo
        ) {
            destinationForm.setError(
                "destinationAccountNo",
                {
                    type: "manual",
                    message: t(
                        "transfer.sameSourceDestination",
                    ),
                },
            );

            destinationForm.setValue(
                "destinationAccountNo",
                "",
            );

            return;
        }

        destinationForm.clearErrors(
            "destinationAccountNo",
        );

        onFindHolder();
    };

    return (
        <div className="rounded-lg border border-border bg-surface p-6">
            {/* Source account */}

            <div>
                <label
                    htmlFor="source-account"
                    className="
                        mb-2
                        block
                        text-sm
                        font-medium
                        text-foreground
                    "
                >
                    {t(
                        "transfer.sourceAccount",
                    )}
                </label>

                {isAccountsLoading ? (
                    <div className="flex h-11 items-center rounded-md border border-border bg-background px-3">
                        <Loader2 className="h-4 w-4 animate-spin text-muted" />
                    </div>
                ) : accounts.length === 0 ? (
                    <div className="rounded-md border border-border bg-background-subtle p-3">
                        <p className="text-sm text-muted">
                            {t(
                                "transfer.noAccounts",
                            )}
                        </p>
                    </div>
                ) : (
                    <select
                        id="source-account"
                        value={
                            selectedAccount?.id ??
                            ""
                        }
                        onChange={(event) => {
                            const account =
                                accounts.find(
                                    (item) =>
                                        item.id ===
                                        event
                                            .target
                                            .value,
                                );

                            if (account) {
                                onAccountChange(
                                    account,
                                );
                            }
                        }}
                        className="
                            w-full
                            rounded-md
                            border
                            border-border
                            bg-background
                            px-3
                            py-2.5
                            text-sm
                            text-foreground
                            outline-none
                        "
                    >
                        {accounts.map(
                            (account) => (
                                <option
                                    key={
                                        account.id
                                    }
                                    value={
                                        account.id
                                    }
                                >
                                    {
                                        account.accountNo
                                    }{" "}
                                    -{" "}
                                    {formatMoney(
                                        account.balance,
                                        account.currency,
                                        locale,
                                    )}
                                </option>
                            ),
                        )}
                    </select>
                )}
            </div>

            {/* Destination */}

            <form
                onSubmit={(event) => {
                    event.preventDefault();
                    handleFindHolder();
                }}
            >
                <div className="mt-6">
                    <label
                        htmlFor="destination-account-no"
                        className="
                            mb-2
                            block
                            text-sm
                            font-medium
                            text-foreground
                        "
                    >
                        {t(
                            "transfer.destinationAccount",
                        )}
                    </label>

                    <div className="flex gap-3">
                        <input
                            id="destination-account-no"
                            {...destinationForm.register(
                                "destinationAccountNo",
                            )}
                            className="
                                min-w-0
                                flex-1
                                rounded-md
                                border
                                border-border
                                bg-background
                                px-3
                                py-2.5
                                text-sm
                                text-foreground
                                outline-none
                            "
                            placeholder={t(
                                "transfer.destinationPlaceholder",
                            )}
                        />

                        <Button
                            type="submit"
                            variant="outline"
                            loading={
                                isHolderLoading
                            }
                        >
                            {t(
                                "transfer.check",
                            )}
                        </Button>
                    </div>

                    {destinationForm
                        .formState.errors
                        .destinationAccountNo && (
                        <p className="mt-1 text-xs text-danger">
                            {
                                destinationForm
                                    .formState
                                    .errors
                                    .destinationAccountNo
                                    .message
                            }
                        </p>
                    )}
                </div>
            </form>

            {/* Recipient */}

            {holder && (
                <div
                    className="
                        mt-4
                        rounded-lg
                        border
                        border-border
                        bg-background-subtle
                        p-4
                    "
                >
                    <p className="text-xs text-muted">
                        {t(
                            "transfer.recipient",
                        )}
                    </p>

                    <p className="mt-1 text-sm font-semibold text-primary">
                        {
                            holder.fullName
                        }
                    </p>

                    <p className="mt-1 text-xs text-muted">
                        {
                            holder.accountNo
                        }
                    </p>
                </div>
            )}

            {/* Transfer details */}

            {holder && (
                <form
                    onSubmit={(event) => {
                        event.preventDefault();
                        onCreateIntent();
                    }}
                >
                    <div className="mt-6 space-y-5">
                        {/* Amount */}

                        <div>
                            <label
                                htmlFor="transfer-amount"
                                className="
                                    mb-2
                                    block
                                    text-sm
                                    font-medium
                                    text-foreground
                                "
                            >
                                {t(
                                    "transfer.amount",
                                )}
                            </label>

                            <Controller
                                name="amount"
                                control={
                                    detailsForm.control
                                }
                                render={({
                                             field,
                                             fieldState,
                                         }) => (
                                    <MoneyInput
                                        id="transfer-amount"
                                        value={
                                            field.value
                                        }
                                        currency={
                                            selectedAccount
                                                ?.currency ??
                                            "VND"
                                        }
                                        onChange={
                                            field.onChange
                                        }
                                        error={
                                            fieldState
                                                .error
                                                ?.message
                                        }
                                        placeholder="0"
                                        disabled={
                                            isIntentLoading
                                        }
                                    />
                                )}
                            />
                        </div>

                        {/* Description */}

                        <div>
                            <label
                                htmlFor="transfer-description"
                                className="
                                    mb-2
                                    block
                                    text-sm
                                    font-medium
                                    text-foreground
                                "
                            >
                                {t(
                                    "transfer.descriptionField",
                                )}
                            </label>

                            <textarea
                                id="transfer-description"
                                rows={3}
                                {...detailsForm.register(
                                    "description",
                                )}
                                className="
                                    w-full
                                    resize-none
                                    rounded-md
                                    border
                                    border-border
                                    bg-background
                                    px-3
                                    py-2.5
                                    text-sm
                                    text-foreground
                                    outline-none
                                "
                                placeholder={t(
                                    "transfer.descriptionPlaceholder",
                                )}
                            />

                            {detailsForm
                                .formState
                                .errors
                                .description && (
                                <p className="mt-1 text-xs text-danger">
                                    {
                                        detailsForm
                                            .formState
                                            .errors
                                            .description
                                            .message
                                    }
                                </p>
                            )}
                        </div>
                    </div>

                    <div className="mt-6 flex justify-end">
                        <Button
                            type="submit"
                            loading={
                                isIntentLoading
                            }
                            disabled={
                                !selectedAccount
                            }
                        >
                            {t(
                                "transfer.continue",
                            )}
                        </Button>
                    </div>
                </form>
            )}
        </div>
    );
}