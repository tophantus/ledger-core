package com.example.ledgercore.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {


    private boolean success;


    private String code;


    private String message;


    private T data;



    public static <T> ApiResponse<T> success(T data, String message){

        return new ApiResponse<>(
                true,
                "SUCCESS",
                message,
                data
        );
    }



    public static <T> ApiResponse<T> error(
            String code,
            String message
    ){

        return new ApiResponse<>(
                false,
                code,
                message,
                null
        );
    }

}