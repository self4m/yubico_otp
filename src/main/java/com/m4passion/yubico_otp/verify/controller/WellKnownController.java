package com.m4passion.yubico_otp.verify.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WellKnownController {

    @GetMapping("/.well-known/appspecific/com.chrome.devtools.json")
    public ResponseEntity<String> devtoolsJson() {
        // 如果不需要，直接返回 404
        return ResponseEntity.notFound().build();

        // 或返回空 JSON（不建议）
        // return ResponseEntity.ok("{}");
    }
}
