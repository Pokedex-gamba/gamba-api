package com.github.martmatix.gambaapi.DTOs;

public class UserWalletDTO {

    private String userId;
    private int balance;

    public UserWalletDTO() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}
