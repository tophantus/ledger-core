import {create} from "zustand";

import type {Product, ProductType} from "../types/product";

interface ProductState {
    products: Product[];
    productMap: Map<string, Product>;
    initialized: boolean;

    setProducts: (products: Product[]) => void;

    getProductsByType: (
        type: ProductType
    ) => Product[];

    clearProducts: () => void;
}

export const useProductStore =
    create<ProductState>((set, get) => ({
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

        getProductsByType: (type) =>
            get().products.filter(
                (product) =>
                    product.type === type
            ),

        clearProducts: () =>
            set({
                products: [],
                productMap: new Map(),
                initialized: false,
            }),
    }));