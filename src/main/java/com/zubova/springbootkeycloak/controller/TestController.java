
package com.zubova.springbootkeycloak.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/anonymous")
    public ResponseEntity<String> getAnonymous() {
        return ResponseEntity.ok("Hello Anonymous");
    }

    @GetMapping("/admin")
    public ResponseEntity<String> getAdmin(@AuthenticationPrincipal Jwt jwt) {
        String userName = jwt.getClaimAsString("name");
        String userEmail = jwt.getClaimAsString("email");
        return ResponseEntity.ok("Hello Admin\nUser Name: " + userName + "\nUser Email: " + userEmail);
    }

    @GetMapping("/user")
    public ResponseEntity<String> getUser(@AuthenticationPrincipal Jwt jwt) {
        String userName = jwt.getClaimAsString("name");
        String userEmail = jwt.getClaimAsString("email");
        return ResponseEntity.ok("Hello User\nUser Name: " + userName + "\nUser Email: " + userEmail);
    }

}