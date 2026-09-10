"use client";

import {Loader2} from "lucide-react";
import {Controller, UseFormReturn} from "react-hook-form";
import {
    useLocale,
    useTranslations,
} from "next-intl";

import {Button} from "@/components/ui/button";
import {MoneyInput} from "@/components/ui/money-input";

import type {AccountSummary} from "@/features/account/types/account";
import type {WithdrawalFormValues} from "../schemas/withdrawal-schema";

import {formatMoney} from "@/lib/utils/currency";
import {isAmountGreaterThanZero} from "@/lib/utils/money";

interface WithdrawalFormProps {
    accounts: AccountSummary[];
    selectedAccount:
        | AccountSummary
        | null;

    isAccountsLoading: boolean;
    isCreateLoading: boolean;

    form: UseFormReturn<WithdrawalFormValues>;

    onAccountChange: (
        account: AccountSummary,
    ) => void;

    onCreateRequest: () => void;
}

export function WithdrawalForm({
                                   accounts,
                                   selectedAccount,
                                   isAccountsLoading,
                                   isCreateLoading,
                                   form,
                                   onAccountChange,
                                   onCreateRequest,
                               }: WithdrawalFormProps) {
    const t =
        useTranslations("withdrawal");

    const locale = useLocale();

    const availableAccounts =
        accounts.filter((account) =>
            isAmountGreaterThanZero(
                account.availableBalance,
            ),
        );

    return (
        <form
            onSubmit={(event) => {
                event.preventDefault();
                onCreateRequest();
            }}
            className="
                rounded-lg
                border
                border-border
                bg-surface
                p-6
            "
        >
            {/* Source account */}

            <div>
                <label
                    htmlFor="withdrawal-source-account"
                    className="
                        mb-2
                        block
                        text-sm
                        font-medium
                        text-foreground
                    "
                >
                    {t(
                        "sourceAccount",
                    )}
                </label>

                {isAccountsLoading ? (
                    <div className="
                        flex
                        h-11
                        items-center
                        rounded-md
                        border
                        border-border
                        bg-background
                        px-3
                    ">
                        <Loader2
                            className="
                                h-4
                                w-4
                                animate-spin
                                text-muted
                            "
                        />
                    </div>
                ) : availableAccounts.length ===
                0 ? (
                    <div className="
                        rounded-md
                        border
                        border-border
                        bg-background-subtle
                        p-3
                    ">
                        <p className="text-sm text-muted">
                            {t(
                                "noAccounts",
                            )}
                        </p>
                    </div>
                ) : (
                    <select
                        id="withdrawal-source-account"
                        value={
                            selectedAccount?.id ??
                            ""
                        }
                        onChange={(event) => {
                            const account =
                                availableAccounts.find(
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
                        disabled={
                            isCreateLoading
                        }
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
                            disabled:cursor-not-allowed
                            disabled:opacity-60
                        "
                    >
                        {availableAccounts.map(
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
                                        account.availableBalance,
                                        account.currency,
                                        locale,
                                    )}
                                </option>
                            ),
                        )}
                    </select>
                )}
            </div>

            {/* Amount */}

            {selectedAccount &&
                availableAccounts.length > 0 && (
                    <div className="mt-6">
                        <label
                            htmlFor="withdrawal-amount"
                            className="
                                mb-2
                                block
                                text-sm
                                font-medium
                                text-foreground
                            "
                        >
                            {t(
                                "amount",
                            )}
                        </label>

                        <Controller
                            name="amount"
                            control={
                                form.control
                            }
                            render={({
                                         field,
                                         fieldState,
                                     }) => (
                                <MoneyInput
                                    id="withdrawal-amount"
                                    value={
                                        field.value
                                    }
                                    currency={
                                        selectedAccount.currency
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
                                        isCreateLoading
                                    }
                                />
                            )}
                        />

                        <p className="mt-1 text-xs text-muted">
                            {t(
                                "availableBalance",
                            )}{" "}
                            {formatMoney(
                                selectedAccount.availableBalance,
                                selectedAccount.currency,
                                locale,
                            )}
                        </p>
                    </div>
                )}

            {/* Continue */}

            <div className="mt-6 flex justify-end">
                <Button
                    type="submit"
                    loading={isCreateLoading}
                    disabled={
                        !selectedAccount ||
                        availableAccounts.length ===
                        0
                    }
                >
                    {t(
                        "continue",
                    )}
                </Button>
            </div>
        </form>
    );
}