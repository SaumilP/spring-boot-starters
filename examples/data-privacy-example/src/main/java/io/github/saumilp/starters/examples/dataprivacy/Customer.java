/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.examples.dataprivacy;

import io.github.saumilp.starters.dataprivacy.crypto.EncryptedStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Customer entity whose {@code nationalIdNumber} is transparently encrypted at rest via
 * {@link EncryptedStringConverter}.
 *
 * @author SaumilP
 */
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "national_id_number")
    private String nationalIdNumber;

    /** Creates an empty customer (required by JPA). */
    public Customer() {
    }

    /**
     * Creates a customer.
     *
     * @param name           the customer name
     * @param nationalIdNumber the national ID number (stored encrypted)
     */
    public Customer(String name, String nationalIdNumber) {
        this.name = name;
        this.nationalIdNumber = nationalIdNumber;
    }

    /**
     * Returns the identifier.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the national ID number (decrypted on read).
     *
     * @return the national ID number
     */
    public String getNationalIdNumber() {
        return nationalIdNumber;
    }

    /**
     * Sets the national ID number (encrypted on write).
     *
     * @param nationalIdNumber the national ID number
     */
    public void setNationalIdNumber(String nationalIdNumber) {
        this.nationalIdNumber = nationalIdNumber;
    }
}
