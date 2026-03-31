package com.example.urlshortener;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin
public class UrlController {

    private final UrlMappingRepository urlMappingRepository;
    private final SecureRandom random = new SecureRandom();
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public UrlController(UrlMappingRepository urlMappingRepository) {
        this.urlMappingRepository = urlMappingRepository;
    }

    @PostMapping("/shorten")
    public ResponseEntity<?> shorten(
            @RequestBody Map<String, String> body,
            jakarta.servlet.http.HttpServletRequest request) {

        String longUrl = body.get("url");

        if (!isValidUrl(longUrl)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid URL. Must be a valid http/https URL."));
        }

        String code = generateCode();
        while (urlMappingRepository.findByCode(code).isPresent()) {
            code = generateCode();
        }
        urlMappingRepository.save(new UrlMapping(code, longUrl));

        String baseUrl = request.getScheme() + "://" + request.getServerName()
                + ":" + request.getServerPort();

        return ResponseEntity.ok(Map.of("shortUrl", baseUrl + "/" + code));
    }

    @GetMapping("/{code:[a-zA-Z0-9]+}")
    public void redirect(@PathVariable String code, HttpServletResponse response)
            throws IOException {
        Optional<UrlMapping> urlOptional = urlMappingRepository.findByCode(code);
        if (urlOptional.isPresent()) {
            UrlMapping urlMapping = urlOptional.get();
            urlMapping.incrementClickCount();
            urlMappingRepository.save(urlMapping);
            response.sendRedirect(urlMapping.getOriginalUrl());
        } else {
            response.sendError(404, "Short URL not found");
        }
    }

    @GetMapping("/api/stats/{code}")
    public ResponseEntity<?> getStats(@PathVariable String code) {
        Optional<UrlMapping> urlOptional = urlMappingRepository.findByCode(code);
        if (urlOptional.isPresent()) {
            UrlMapping mapping = urlOptional.get();
            return ResponseEntity.ok(Map.of(
                    "code", mapping.getCode(),
                    "originalUrl", mapping.getOriginalUrl(),
                    "clickCount", mapping.getClickCount(),
                    "createdAt", mapping.getCreatedAt().toString()
            ));
        } else {
            return ResponseEntity.status(404).body(Map.of("error", "Short URL not found"));
        }
    }

    private boolean isValidUrl(String url) {
        if (url == null || url.isBlank())
            return false;
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            return ("http".equals(scheme) || "https".equals(scheme))
                    && uri.getHost() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return code.toString();
    }
}