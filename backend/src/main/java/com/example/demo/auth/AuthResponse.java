package com.example.demo.auth;

/**
 *
 * Author : bhask
 * Created : 11-10-2025
 */
public class AuthResponse {
    private String token;
    public AuthResponse(String token){ this.token = token; }
    public String getToken(){ return token; }
}
