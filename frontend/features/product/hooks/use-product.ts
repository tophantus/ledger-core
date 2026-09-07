"use client";

import {useCallback} from "react";

import {productApi} from "../api/product-api";
import {useProductStore} from "../store/product-store";

export function useProduct() {
    const products = useProductStore(
        (state) => state.products,
    );

    const initialized = useProductStore(
        (state) => state.initialized,
    );

    const setProducts = useProductStore(
        (state) => state.setProducts,
    );

    const getActiveProducts = useCallback(
        async () => {
            if (initialized) {
                return null;
            }

            const response =
                await productApi.getActiveProducts();

            if (response.success) {
                setProducts(response.data);
            }

            return response;
        },
        [initialized, setProducts],
    );

    return {
        products,
        initialized,
        getActiveProducts,
    };
}