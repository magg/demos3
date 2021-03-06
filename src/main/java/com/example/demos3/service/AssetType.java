package com.example.demos3.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Asset type.
 */
@AllArgsConstructor
@Getter
public enum AssetType {
    TEST("test/");

    private final String prefix;
}
