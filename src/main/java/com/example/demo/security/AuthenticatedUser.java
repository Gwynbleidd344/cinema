package com.example.demo.security;

import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthenticatedUser {

  private AuthenticatedUser() {}

  public static UUID id() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof UUID userId)) {
      throw new IllegalStateException("No authenticated user in this context");
    }
    return userId;
  }

  public static boolean hasRole(String role) {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null) {
      return false;
    }
    return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
  }

  public static boolean hasAnyRole(String... roles) {
    for (String role : roles) {
      if (hasRole(role)) {
        return true;
      }
    }
    return false;
  }
}
