package com.github.martmatix.gambaapi.gamba.constants;

public enum ErrorCodes {

    TOKEN_EXTRACTION_ERROR("Token Extraction Error");

    private final String code;

    ErrorCodes(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
