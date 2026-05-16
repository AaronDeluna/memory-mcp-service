package ru.thisstp.memorymcp.controller;

import lombok.RequiredArgsConstructor;
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
    public Article startParse(@RequestParam String id) {
        return habrParserService.parse(id);
    }

    @PostMapping("/start-parse-all")
    public Map<String, String> startParseAll() {
        habrParserService.parseAll();
        return Map.of("status", "started");
    }
}
