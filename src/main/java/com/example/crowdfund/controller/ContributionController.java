package com.example.crowdfund.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/api/payment")
public class ContributionController {

    @PostMapping("/")
    public ResponseEntity<?> doContribution(){

        return ResponseEntity.ok("Ok");
    }

}
