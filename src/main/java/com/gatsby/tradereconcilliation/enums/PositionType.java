package com.gatsby.tradereconcilliation.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PositionType {
    SHORT("short"), LONG("long");

    private final String label;

    PositionType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getPositionType() {
        return label;
    }
}
