"use client";

import {useCallback} from "react";

import {paymentProviderApi} from "../api/payment-provider-api";
import type {
    RegisterPaymentProviderRequest,
} from "../types/payment-provider";

export function useRegisterPaymentProvider() {
    const registerPaymentProvider =
        useCallback(
            async (
                request: RegisterPaymentProviderRequest,
            ) => {
                return paymentProviderApi.register(
                    request,
                );
            },
            [],
        );

    return {
        registerPaymentProvider,
    };
}