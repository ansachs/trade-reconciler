package com.gatsby.tradereconcilliation.model;

import com.gatsby.tradereconcilliation.enums.PositionType;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class TradePosition {
    protected PositionType type;
    private String symbol;
}
