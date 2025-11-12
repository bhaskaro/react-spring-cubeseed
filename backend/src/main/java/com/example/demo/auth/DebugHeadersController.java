package com.example.demo.auth;

import com.example.demo.jwt.JwtService;
import jakarta.validation.Valid;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/debug")
class DebugHeadersController {
  
  private final com.example.demo.jwt.JwtService jwt;
  
  public DebugHeadersController(com.example.demo.jwt.JwtService jwt) { this.jwt = jwt; }

  @GetMapping("/headers")
  public java.util.Map<String,String> headers(jakarta.servlet.http.HttpServletRequest req) {
    var map = new java.util.LinkedHashMap<String,String>();
    java.util.Collections.list(req.getHeaderNames()).forEach(h -> map.put(h, req.getHeader(h)));
    return map;
  }

  @GetMapping("/whoami")
  public java.util.Map<String,String> whoami(@org.springframework.web.bind.annotation.RequestHeader(value="Authorization",required=false) String auth) {
    String sub = null;
    if (auth != null && auth.startsWith("Bearer ")) {
      try { sub = jwt.extractUsername(auth.substring(7).trim()); } catch (Exception ignored) {}
    }
    return java.util.Map.of("subject", sub == null ? "" : sub);
  }
}