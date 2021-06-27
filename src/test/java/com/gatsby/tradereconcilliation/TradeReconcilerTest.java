package com.gatsby.tradereconcilliation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gatsby.tradereconcilliation.enums.PositionType;
import com.gatsby.tradereconcilliation.model.Position;
import com.gatsby.tradereconcilliation.model.Result;
import com.gatsby.tradereconcilliation.model.Trade;
import com.gatsby.tradereconcilliation.model.TradePosition;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class TradeReconcilerTest {

    private ObjectMapper mapper;
    private Trade[] trades;
    private Position[] positions;

    @BeforeEach
    void setup() throws IOException {
        this.mapper = new ObjectMapper();

        URL positionJson = getResource("test_reconciled/positions.json");
        URL tradeJson = getResource("test_reconciled/trades.json");
        this.trades = mapper.readValue(tradeJson, Trade[].class);
        this.positions = mapper.readValue(positionJson, Position[].class);
    }


    @Test
    public void reconcileTrades_emptyPositionList_returnsAllTrades() throws IOException {
        TradeReconciler tradeReconcile = new TradeReconciler(new ArrayList<>(), Arrays.asList(trades));

        //when
        Result result = tradeReconcile.reconcile();

        //then
        assertThat(result.getUnmatchedPositions()).isEmpty();
        assertThat(result.getUnmatchedTrades().size()).isEqualTo(trades.length);
        assertThat(result.getUnmatchedTrades()).containsExactlyInAnyOrder(trades);
    }

    @Test
    public void reconcileTrades_emptyTrades_returnsAllPositions() throws IOException {
        TradeReconciler tradeReconcile = new TradeReconciler(Arrays.asList(positions), new ArrayList<>());

        //when
        Result result = tradeReconcile.reconcile();

        //then
        assertThat(result.getUnmatchedPositions()).containsExactlyInAnyOrder(positions);
    }

    @Test
    public void reconcileTrades_tradesAndPositionsPerfectlyReconciled_responseIsEmptyList() throws IOException {
        TradeReconciler tradeReconcile = new TradeReconciler(Arrays.asList(positions), Arrays.asList(trades));

        //when
        Result result = tradeReconcile.reconcile();

        //then
        assertThat(result.getUnmatchedPositions()).isEmpty();
        assertThat(result.getUnmatchedTrades()).isEmpty();
    }

    @Test
    public void reconcileTrades_positionsDontHaveTrades_returnPositions() throws IOException {
        Position unMatchedPosition_1 = Position.builder()
                .id("id_1")
                .quantity(65L)
                .type(PositionType.LONG)
                .symbol("aaa")
                .build();

        Position unMatchedPosition_2 = Position.builder()
                .id("id_2")
                .quantity(23L)
                .type(PositionType.SHORT)
                .symbol("abc")
                .build();


        List<Position> positionList = Lists.newArrayList(positions);
        positionList.add(unMatchedPosition_1);
        positionList.add(unMatchedPosition_2);
        TradeReconciler tradeReconcile = new TradeReconciler(positionList, Arrays.asList(trades));

        //when
        Result result = tradeReconcile.reconcile();

        //then
        assertThat(result.getUnmatchedPositions()).containsExactlyInAnyOrder(unMatchedPosition_1, unMatchedPosition_2);
        assertThat(result.getUnmatchedTrades()).isEmpty();
    }

    @Test
    public void reconcileTrades_tradesWithoutPositions_returnsTradesAndUnmatchedLegs() throws IOException {
        String stock_1 = "abc_1";
        PositionType stock_1_position = PositionType.LONG;

        TradePosition matchedLeg = TradePosition.builder()
                .symbol(stock_1)
                .type(stock_1_position)
                .build();

        TradePosition unmatchedLeg = TradePosition.builder()
                .symbol("abc_2")
                .type(PositionType.SHORT)
                .build();

        Trade partiallyMatchedTrade = Trade.builder()
                .id("id_1")
                .quantity(42L)
                .legs(Arrays.asList(matchedLeg, unmatchedLeg))
                .build();

        Position matchedPosition = Position.builder()
                .id("id_2")
                .quantity(42L)
                .type(stock_1_position)
                .symbol(stock_1)
                .build();


        List<Position> positionList = Lists.newArrayList(positions);
        positionList.add(matchedPosition);
        List<Trade> tradeList = Lists.newArrayList(trades);
        tradeList.add(partiallyMatchedTrade);

        TradeReconciler tradeReconcile = new TradeReconciler(positionList, tradeList);

        //when
        Result result = tradeReconcile.reconcile();

        //then
        assertThat(result.getUnmatchedPositions()).isEmpty();
        assertThat(result.getUnmatchedTrades().size()).isEqualTo(1);
        assertThat(result.getUnmatchedTrades().get(0).getLegs()).containsExactlyInAnyOrder(unmatchedLeg);
    }

    @Test
    public void partiallyMatchedTradeLegs_whenMatchedWithPositions_positionsAreConsideredCleared() throws IOException {
        String stock_1 = "stock_1";
        PositionType stock_1_position = PositionType.LONG;
        String trade_1 = "trade_1";
        String trade_2 = "trade_2";

        TradePosition matchedLeg = TradePosition.builder()
                .symbol(stock_1)
                .type(stock_1_position)
                .build();

        TradePosition unmatchedLeg = TradePosition.builder()
                .symbol("abc_2")
                .type(PositionType.SHORT)
                .build();

        Trade partiallyMatchedTrade = Trade.builder()
                .id(trade_1)
                .quantity(42L)
                .legs(Arrays.asList(matchedLeg, unmatchedLeg))
                .build();

        Trade fullyMatchedTrade = Trade.builder()
                .id(trade_2)
                .quantity(42L)
                .legs(Collections.singletonList(matchedLeg))
                .build();

        Position matchedPosition = Position.builder()
                .id("position_1")
                .quantity(42L)
                .type(stock_1_position)
                .symbol(stock_1)
                .build();



        List<Position> positionList = Lists.newArrayList(positions);
        positionList.add(matchedPosition);
        List<Trade> tradeList = Lists.newArrayList(trades);
        tradeList.add(fullyMatchedTrade);
        tradeList.add(partiallyMatchedTrade);

        TradeReconciler tradeReconcile = new TradeReconciler(positionList, tradeList);

        //when
        Result result = tradeReconcile.reconcile();

        //then
        assertThat(result.getUnmatchedPositions()).isEmpty();
        assertThat(result.getUnmatchedTrades().size()).isEqualTo(2);
        Optional<Trade> trade1 = result.getUnmatchedTrades().stream()
                .filter(trade -> trade_1.equals(trade.getId()))
                .findFirst();
        Optional<Trade> trade2 = result.getUnmatchedTrades().stream()
                .filter(trade -> trade_2.equals(trade.getId()))
                .findFirst();

        assertThat(trade1.get().getLegs()).containsExactlyInAnyOrder(unmatchedLeg);
        assertThat(trade2.get().getLegs()).containsExactlyInAnyOrder(matchedLeg);
    }



    private URL getResource(String s) {
        return getClass().getClassLoader().getResource(s);
    }

}