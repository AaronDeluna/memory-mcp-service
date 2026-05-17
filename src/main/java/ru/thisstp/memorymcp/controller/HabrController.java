package ru.thisstp.memorymcp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.thisstp.memorymcp.entity.Article;
import ru.thisstp.memorymcp.service.HabrParserService;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HabrController {

    private final HabrParserService habrParserService;

    @PostMapping("/start-parse")
    public ResponseEntity<Article> startParse(@RequestParam String id) {
        return ResponseEntity.ok(habrParserService.parse(id));
    }

    @PostMapping("/start-parse-all")
    public ResponseEntity<Map<String, String>> startParseAll() {
        habrParserService.startParseAll();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("status", "started"));
    }
}
