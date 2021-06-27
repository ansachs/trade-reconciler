package com.gatsby.tradereconcilliation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Trade {
    private String id;
    private Long quantity;
    private List<TradePosition> legs;
}
