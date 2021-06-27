package com.gatsby.tradereconcilliation.model;

import com.gatsby.tradereconcilliation.enums.PositionType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class Position extends TradePosition {
    private String id;
    private Long quantity;
}
