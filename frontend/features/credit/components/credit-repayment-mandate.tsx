"use client";

import {
    useEffect,
    useMemo,
    useState,
} from "react";
import {useTranslations} from "next-intl";
import {
    useForm,
} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";

import {useMyAccounts} from "@/features/account/hooks/use-my-accounts";
import {useAccountStore} from "@/features/account/stores/account-store";
import {AccountStatus} from "@/features/account/types/account";

import {useCreateCreditRepaymentMandate} from "../hooks/use-create-credit-repayment-mandate";
import {useRevokeCreditRepaymentMandate} from "../hooks/use-revoke-credit-repayment-mandate";
import {useUpdateCreditRepaymentMandate} from "../hooks/use-update-credit-repayment-mandate";
import {
    createCreditRepaymentMandateSchema,
    type CreditRepaymentMandateFormValues,
} from "../schemas/credit-repayment-mandate-schema";
import type {CreditRepaymentMandate, GetUserCreditFacilityResult} from "../types/credit-facility";
import {
    CreditRepaymentMandateStatus,
    RepaymentType,
} from "../types/credit-repayment-mandate";
import {CreditRepaymentMandateHeader} from "@/features/credit/components/credit-repayment-mandate-header";
import {CreditRepaymentMandateEmpty} from "@/features/credit/components/credit-repayment-mandate-empty";
import {CreditRepaymentMandateConfigured} from "@/features/credit/components/credit-repayment-mandate-configured";
import {CreditRepaymentMandateForm} from "@/features/credit/components/credit-repayment-mandate-form";

interface CreditRepaymentMandateProps {
    facility: GetUserCreditFacilityResult;
}

export function CreditRepaymentMandate({
                                           facility,
                                       }: CreditRepaymentMandateProps) {
    const t = useTranslations("credit.facility");

    const accounts = useAccountStore(
        (state) => state.accounts,
    );

    const accountsInitialized = useAccountStore(
        (state) => state.initialized,
    );

    const {getMyAccounts} = useMyAccounts();

    const {createMandate} =
        useCreateCreditRepaymentMandate();

    const {updateMandate} =
        useUpdateCreditRepaymentMandate();

    const {revokeMandate} =
        useRevokeCreditRepaymentMandate();

    const [mandate, setMandate] =
        useState<CreditRepaymentMandate | null>(
            facility.repaymentMandate,
        );

    const [isEditing, setIsEditing] =
        useState(false);

    const [isRevoking, setIsRevoking] =
        useState(false);

    const schema = useMemo(
        () =>
            createCreditRepaymentMandateSchema(t),
        [t],
    );

    const {
        register,
        control,
        handleSubmit,
        reset,
        formState: {
            errors,
            isSubmitting,
        },
    } = useForm<CreditRepaymentMandateFormValues>({
        resolver: zodResolver(schema),
        defaultValues: {
            accountId: "",
            repaymentType:
            RepaymentType.FULL_BALANCE,
        },
    });

    useEffect(() => {
        // eslint-disable-next-line react-hooks/set-state-in-effect
        setMandate(
            facility.repaymentMandate,
        );
    }, [facility.repaymentMandate]);

    useEffect(() => {
        if (accountsInitialized) {
            return;
        }

        void getMyAccounts();
    }, [
        accountsInitialized,
        getMyAccounts,
    ]);

    const activeMandate =
        mandate?.status ===
        CreditRepaymentMandateStatus.ACTIVE
            ? mandate
            : null;

    const availableAccounts =
        accounts.filter(
            (account) =>
                account.currency ===
                facility.currency &&
                account.status ===
                AccountStatus.ACTIVE,
        );

    const handleStartCreate = () => {
        reset({
            accountId: "",
            repaymentType:
            RepaymentType.FULL_BALANCE,
        });

        setIsEditing(true);
    };

    const handleStartEdit = () => {
        if (!activeMandate) {
            return;
        }

        reset({
            accountId:
            activeMandate.accountId,
            repaymentType:
            activeMandate.repaymentType,
        });

        setIsEditing(true);
    };

    const handleCancel = () => {
        setIsEditing(false);

        reset({
            accountId:
                activeMandate?.accountId ?? "",
            repaymentType:
                activeMandate?.repaymentType ??
                RepaymentType.FULL_BALANCE,
        });
    };

    const handleSubmitMandate = async (
        values: CreditRepaymentMandateFormValues,
    ) => {
        if (activeMandate) {
            const response =
                await updateMandate(
                    activeMandate.id,
                    {
                        repaymentType:
                        values.repaymentType,
                    },
                );

            if (!response.success) {
                return;
            }

            setMandate((current) =>
                current
                    ? {
                        ...current,
                        repaymentType:
                        response.data
                            .repaymentType,
                        status:
                        response.data.status,
                        updatedAt:
                        response.data
                            .updatedAt,
                    }
                    : current,
            );

            setIsEditing(false);

            return;
        }

        const response =
            await createMandate({
                creditFacilityId:
                facility.id,
                accountId:
                values.accountId,
                repaymentType:
                values.repaymentType,
            });

        if (!response.success) {
            return;
        }

        setMandate({
            id: response.data.mandateId,
            accountId:
            response.data.accountId,
            repaymentType:
            response.data.repaymentType,
            status:
            response.data.status,
            createdAt:
            response.data.createdAt,
            updatedAt:
            response.data.createdAt,
        });

        setIsEditing(false);
    };

    const handleRevoke = async () => {
        if (!activeMandate) {
            return;
        }

        setIsRevoking(true);

        try {
            const response =
                await revokeMandate(
                    activeMandate.id,
                );

            if (!response.success) {
                return;
            }

            setMandate((current) =>
                current
                    ? {
                        ...current,
                        status:
                        response.data.status,
                    }
                    : current,
            );

            setIsEditing(false);
        } finally {
            setIsRevoking(false);
        }
    };

    return (
        <div
            className="
                mt-5
                rounded-xl
                border
                border-border
                bg-background-subtle
                p-4
                sm:p-5
            "
        >
            <CreditRepaymentMandateHeader
                mandate={mandate}
            />

            {!activeMandate &&
                !isEditing && (
                    <CreditRepaymentMandateEmpty
                        onRegister={
                            handleStartCreate
                        }
                    />
                )}

            {activeMandate &&
                !isEditing && (
                    <CreditRepaymentMandateConfigured
                        mandate={
                            activeMandate
                        }
                        accounts={accounts}
                        onEdit={
                            handleStartEdit
                        }
                        onRevoke={
                            handleRevoke
                        }
                        isRevoking={
                            isRevoking
                        }
                    />
                )}

            {isEditing && (
                <CreditRepaymentMandateForm
                    mandate={activeMandate}
                    availableAccounts={
                        availableAccounts
                    }
                    control={control}
                    register={register}
                    errors={errors}
                    isSubmitting={
                        isSubmitting
                    }
                    onSubmit={handleSubmit(
                        handleSubmitMandate,
                    )}
                    onCancel={
                        handleCancel
                    }
                />
            )}
        </div>
    );
}