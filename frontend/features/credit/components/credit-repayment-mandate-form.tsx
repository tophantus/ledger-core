"use client";

import type {
    Control,
    FieldErrors,
    UseFormRegister,
} from "react-hook-form";
import {useTranslations} from "next-intl";
import {Controller} from "react-hook-form";

import {AccountSelect} from "@/features/account/components/account-select";
import type {AccountSummary} from "@/features/account/types/account";

import type {CreditRepaymentMandateFormValues} from "../schemas/credit-repayment-mandate-schema";
import {RepaymentType} from "../types/credit-repayment-mandate";
import {CreditRepaymentMandate} from "@/features/credit/types/credit-facility";

interface CreditRepaymentMandateFormProps {
    mandate: CreditRepaymentMandate | null;
    availableAccounts: AccountSummary[];
    control: Control<CreditRepaymentMandateFormValues>;
    register: UseFormRegister<CreditRepaymentMandateFormValues>;
    errors: FieldErrors<CreditRepaymentMandateFormValues>;
    isSubmitting: boolean;
    onSubmit: (
        event?: React.BaseSyntheticEvent,
    ) => Promise<void>;
    onCancel: () => void;
}

export function CreditRepaymentMandateForm({
                                               mandate,
                                               availableAccounts,
                                               control,
                                               register,
                                               errors,
                                               isSubmitting,
                                               onSubmit,
                                               onCancel,
                                           }: CreditRepaymentMandateFormProps) {
    const t = useTranslations("credit");

    return (
        <form
            onSubmit={onSubmit}
            className="mt-5 space-y-5"
        >
            {/* Repayment Account */}
            <div>
                <label
                    htmlFor="repayment-account"
                    className="
                        block
                        text-sm
                        font-medium
                        text-text-primary
                    "
                >
                    {t(
                        "facility.repaymentMandate.account",
                    )}
                </label>

                {mandate ? (
                    <AccountSelect
                        id="repayment-account"
                        accounts={availableAccounts}
                        value={mandate.accountId}
                        onChange={() => undefined}
                        disabled
                    />
                ) : (
                    <Controller
                        name="accountId"
                        control={control}
                        render={({
                                     field,
                                     fieldState,
                                 }) => (
                            <AccountSelect
                                id="repayment-account"
                                accounts={availableAccounts}
                                value={field.value}
                                onChange={
                                    field.onChange
                                }
                                disabled={
                                    isSubmitting
                                }
                                placeholder={t(
                                    "facility.repaymentMandate.accountPlaceholder",
                                )}
                                error={
                                    fieldState.error
                                        ?.message
                                }
                            />
                        )}
                    />
                )}
            </div>

            {/* Repayment Type */}
            <div>
                <label
                    htmlFor="repayment-type"
                    className="
                        block
                        text-sm
                        font-medium
                        text-text-primary
                    "
                >
                    {t(
                        "facility.repaymentMandate.repaymentType",
                    )}
                </label>

                <select
                    id="repayment-type"
                    {...register("repaymentType")}
                    disabled={isSubmitting}
                    className="
                        mt-2
                        w-full
                        rounded-lg
                        border
                        border-border
                        bg-background
                        px-3
                        py-2.5
                        text-sm
                        text-text-primary
                        outline-none
                        transition
                        focus:ring-2
                        focus:ring-primary/20
                        disabled:cursor-not-allowed
                        disabled:opacity-60
                    "
                >
                    <option
                        value={
                            RepaymentType.FULL_BALANCE
                        }
                    >
                        {t(
                            "facility.repaymentMandate.types.FULL_BALANCE",
                        )}
                    </option>

                    <option
                        value={
                            RepaymentType.MINIMUM_PAYMENT
                        }
                    >
                        {t(
                            "facility.repaymentMandate.types.MINIMUM_PAYMENT",
                        )}
                    </option>
                </select>

                {errors.repaymentType && (
                    <p className="mt-1.5 text-sm text-danger">
                        {
                            errors.repaymentType
                                .message
                        }
                    </p>
                )}
            </div>

            {/* Actions */}
            <div className="flex flex-wrap justify-end gap-2">
                <button
                    type="button"
                    onClick={onCancel}
                    disabled={isSubmitting}
                    className="
                        rounded-lg
                        border
                        border-border
                        bg-surface
                        px-4
                        py-2.5
                        text-sm
                        font-medium
                        text-text-primary
                        transition
                        hover:bg-surface-subtle
                        disabled:cursor-not-allowed
                        disabled:opacity-60
                    "
                >
                    {t(
                        "facility.repaymentMandate.actions.cancel",
                    )}
                </button>

                <button
                    type="submit"
                    disabled={isSubmitting}
                    className="
                        rounded-lg
                        bg-primary
                        px-4
                        py-2.5
                        text-sm
                        font-medium
                        text-primary-foreground
                        transition
                        hover:bg-primary/90
                        disabled:cursor-not-allowed
                        disabled:opacity-60
                    "
                >
                    {isSubmitting
                        ? t(
                            "facility.repaymentMandate.actions.saving",
                        )
                        : mandate
                            ? t(
                                "facility.repaymentMandate.actions.save",
                            )
                            : t(
                                "facility.repaymentMandate.actions.register",
                            )}
                </button>
            </div>
        </form>
    );
}