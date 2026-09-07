import {create} from "zustand";

import type {Product} from "../types/product";

interface ProductState {
    products: Product[];
    productMap: Map<string, Product>;
    initialized: boolean;

    setProducts: (products: Product[]) => void;
    clearProducts: () => void;
}

export const useProductStore =
    create<ProductState>((set) => ({
        products: [],
        productMap: new Map(),
        initialized: false,

        setProducts: (products) =>
            set({
                products,
                productMap: new Map(
                    products.map((product) => [
                        product.id,
                        product,
                    ]),
                ),
                initialized: true,
            }),

        clearProducts: () =>
            set({
                products: [],
                productMap: new Map(),
                initialized: false,
            }),
    }));