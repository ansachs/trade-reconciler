package com.gatsby.tradereconcilliation.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class Result {
    @Builder.Default List<Position> unmatchedPositions = new ArrayList<>();
    @Builder.Default List<Trade> unmatchedTrades = new ArrayList<>();
}
