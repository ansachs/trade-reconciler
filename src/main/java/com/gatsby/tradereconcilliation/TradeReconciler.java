package com.gatsby.tradereconcilliation;

import com.gatsby.tradereconcilliation.enums.PositionType;
import com.gatsby.tradereconcilliation.model.Position;
import com.gatsby.tradereconcilliation.model.Result;
import com.gatsby.tradereconcilliation.model.Trade;
import com.gatsby.tradereconcilliation.model.TradePosition;
import lombok.Builder;
import lombok.Data;

import java.util.*;

public class TradeReconciler {
    private final List<Trade> trades;
    private final Map<String, HiLoPositions> positionsMap;

    public TradeReconciler(List<Position> positions, List<Trade> trades) {
        this.trades = trades;
        this.positionsMap = mapPositions(positions);
    }

    public Result reconcile() {
        Result result = Result.builder()
                .build();

        trades.sort(Comparator.comparingInt((Trade t) -> t.getLegs().size()).reversed());

        for (Trade trade : trades) {
            List<TradePosition> unMatchedLegs = new ArrayList<>();
            List<TradePosition> legs = trade.getLegs();

            for (TradePosition tradePosition: legs) {
                List<Position> positionList = getPositionsForSymbolAndType(tradePosition);

                Optional<Position> position = positionList.stream()
                        .filter(Objects::nonNull)
                        .filter(p -> p.getQuantity().equals(trade.getQuantity()))
                        .findAny();

                if (position.isPresent()) {
                    positionList.remove(position.get());
                } else {
                    unMatchedLegs.add(tradePosition);
                }
            }

            if (unMatchedLegs.size() > 0) {
                Trade tradeUnMatchedLegs = trade.toBuilder()
                        .legs(new ArrayList<>(unMatchedLegs))
                        .build();
                result.getUnmatchedTrades().add(tradeUnMatchedLegs);
            }
            unMatchedLegs.clear();
        }

        for (String key : this.positionsMap.keySet()) {
            result.getUnmatchedPositions().addAll(positionsMap.get(key).getLongList());
            result.getUnmatchedPositions().addAll(positionsMap.get(key).getShortList());
        }

        return result;
    }

    private List<Position> getPositionsForSymbolAndType(TradePosition tradePosition) {
        return Optional.ofNullable(positionsMap.get(tradePosition.getSymbol()))
                .map((hiLoPositions -> PositionType.LONG.equals(tradePosition.getType())
                        ? hiLoPositions.getLongList()
                            : hiLoPositions.getShortList()))
                .orElse(Collections.emptyList());
    }

    private Map<String, HiLoPositions> mapPositions(List<Position> positions) {
        Map<String, HiLoPositions> positionsMap = new HashMap<>();
        for (Position position: positions) {
            HiLoPositions hiLoPositions = positionsMap
                    .computeIfAbsent(position.getSymbol(), (k) -> HiLoPositions.builder().build());
            if (PositionType.LONG.equals(position.getType())) {
                hiLoPositions.getLongList().add(position);
            } else {
                hiLoPositions.getShortList().add(position);
            }
        }
        return positionsMap;
    }

    @Data
    @Builder
    static class HiLoPositions {
        @Builder.Default
        private List<Position> shortList = new ArrayList<>();
        @Builder.Default
        private List<Position> longList = new ArrayList<>();

    }
}
