import {v7 as uuidv7} from "uuid";

export function generateTransactionReference(): string {
    return `TRX_${uuidv7()}`;
}