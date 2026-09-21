export enum BusinessDayStatus {
    OPEN = "OPEN",
    CLOSED = "CLOSED",
}
export interface CurrentBusinessDay {
    businessDate: string;
    status: BusinessDayStatus;
    canClose: boolean;
}