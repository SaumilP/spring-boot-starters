/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.dataprivacy;

import io.github.saumilp.starters.dataprivacy.masking.MaskStrategy;
import io.github.saumilp.starters.dataprivacy.masking.MaskingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Demonstrates transparent field encryption and masked display.
 *
 * <ul>
 *   <li>{@code POST /customers} stores a customer; the national ID is encrypted at rest.</li>
 *   <li>{@code GET /customers/{id}} returns the national ID masked for safe display.</li>
 *   <li>{@code GET /customers/{id}/raw} returns the decrypted value (encryption is transparent).</li>
 * </ul>
 *
 * @author SaumilP
 */
@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerRepository repository;
    private final MaskingService maskingService;

    /**
     * Creates the controller.
     *
     * @param repository     the customer repository
     * @param maskingService the masking service
     */
    public CustomerController(CustomerRepository repository, MaskingService maskingService) {
        this.repository = repository;
        this.maskingService = maskingService;
    }

    /**
     * Stores a new customer; the national ID number is encrypted at rest.
     *
     * @param body a JSON object with {@code name} and {@code nationalIdNumber}
     * @return the created customer id
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, String> body) {
        Customer saved = repository.save(new Customer(body.get("name"), body.get("nationalIdNumber")));
        return ResponseEntity.ok(Map.of("id", saved.getId(), "status", "stored (national ID encrypted)"));
    }

    /**
     * Returns a customer with the national ID masked for display.
     *
     * @param id the customer id
     * @return the customer with a masked national ID, or {@code 404}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable Long id) {
        return repository.findById(id)
            .map(customer -> ResponseEntity.ok(Map.<String, Object>of(
                "name", customer.getName(),
                "nationalIdNumber", maskingService.mask(customer.getNationalIdNumber(), MaskStrategy.CREDIT_CARD))))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Returns the decrypted national ID, demonstrating that encryption is transparent on read.
     *
     * @param id the customer id
     * @return the decrypted national ID, or {@code 404}
     */
    @GetMapping("/{id}/raw")
    public ResponseEntity<Map<String, Object>> getRaw(@PathVariable Long id) {
        return repository.findById(id)
            .map(customer -> ResponseEntity.ok(Map.<String, Object>of(
                "name", customer.getName(),
                "nationalIdNumber", customer.getNationalIdNumber())))
            .orElse(ResponseEntity.notFound().build());
    }
}
