package com.github.martmatix.gambaapi.gamba.constants;

public enum ErrorCodes {

    TOKEN_EXTRACTION_ERROR("Token Extraction Error"),
    PUBLIC_NOT_FOUND("Public Key 'decoding_key' Not Found");

    private final String code;

    ErrorCodes(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
