package orderbook;

import orderbook.marketdata.MarketData;
import orderbook.marketdata.PriceLevel;
import orderbook.marketdata.Side;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toUnmodifiableList;
import static orderbook.marketdata.Side.BUY;
import static orderbook.marketdata.Side.SELL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderBookTest {

    private final OrderBook orderBook = new OrderBook("AUDUSD");

    @Test
    void should_not_accept_market_data_with_different_instrument() {
        // given
        var marketData = new MarketData("participant1", "EURUSD", emptyList());

        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> orderBook.handleMarketDataEvent(marketData));

        // then
        assertEquals("Incorrect instrument, AUDUSD required, but EURUSD provided", exception.getMessage());
    }

    @Test
    void should_handle_empty_market_data() {
        // given
        var marketData = new MarketData("participant1", "AUDUSD", emptyList());

        // when
        orderBook.handleMarketDataEvent(marketData);

        // then
        assertTrue(orderBook.getOrdersBySide(BUY).isEmpty());
        assertTrue(orderBook.getOrdersBySide(SELL).isEmpty());
    }

    @Test
    void should_handle_market_data() {
        // given
        var marketData = new MarketData(
                "participant1",
                "AUDUSD",
                List.of(
                        new PriceLevel(TEN, 1, BUY),
                        new PriceLevel(ONE, 2, SELL)
                )
        );

        // when
        orderBook.handleMarketDataEvent(marketData);

        // then
        assertEquals(1, orderBook.getOrdersBySide(BUY).size());
        assertSame(marketData.priceList.get(0), orderBook.getOrdersBySide(BUY).get(0).priceLevel);

        assertEquals(1, orderBook.getOrdersBySide(SELL).size());
        assertSame(marketData.priceList.get(1), orderBook.getOrdersBySide(SELL).get(0).priceLevel);

        assertEquals(marketData.source, orderBook.getOrdersBySide(BUY).get(0).source);
        assertEquals(marketData.source, orderBook.getOrdersBySide(SELL).get(0).source);
    }


    @Test
    void should_update_existing_market_data() {
        // given
        givenInitialMarketDataSet();

        var marketData = new MarketData(
                "participant1",
                "AUDUSD",
                List.of(
                        new PriceLevel(BigDecimal.valueOf(20), 1, BUY),
                        new PriceLevel(BigDecimal.valueOf(30), 2, SELL)
                )
        );

        // when
        orderBook.handleMarketDataEvent(marketData);

        // then
        assertEquals(1, orderBook.getOrdersBySide(BUY).size());
        assertSame(marketData.priceList.get(0), orderBook.getOrdersBySide(BUY).get(0).priceLevel);

        assertEquals(1, orderBook.getOrdersBySide(SELL).size());
        assertSame(marketData.priceList.get(1), orderBook.getOrdersBySide(SELL).get(0).priceLevel);

        assertEquals(marketData.source, orderBook.getOrdersBySide(BUY).get(0).source);
        assertEquals(marketData.source, orderBook.getOrdersBySide(SELL).get(0).source);
    }


    @Test
    void should_remove_data__on_empty_market_data_event() {
        // given
        givenInitialMarketDataSet();
        var marketData = new MarketData("participant1", "AUDUSD", emptyList());

        // when
        orderBook.handleMarketDataEvent(marketData);

        // given
        assertTrue(orderBook.getOrdersBySide(BUY).isEmpty());
        assertTrue(orderBook.getOrdersBySide(SELL).isEmpty());
    }

    @Test
    void should_sort_bids_by_price_quantity_and_source() {
        // given
        var marketData1 = new MarketData(
                "participant1",
                "AUDUSD",
                List.of(
                        new PriceLevel(BigDecimal.valueOf(20), 1, BUY),
                        new PriceLevel(BigDecimal.valueOf(10), 2, BUY),
                        new PriceLevel(BigDecimal.valueOf(5), 10, BUY)
                )
        );

        var marketData2 = new MarketData(
                "participant2",
                "AUDUSD",
                List.of(
                        new PriceLevel(BigDecimal.valueOf(10), 1, BUY),
                        new PriceLevel(BigDecimal.valueOf(30), 2, BUY),
                        new PriceLevel(BigDecimal.valueOf(5), 10, BUY)
                )
        );

        // when
        orderBook.handleMarketDataEvent(marketData2);
        orderBook.handleMarketDataEvent(marketData1);

        // given
        assertEquals(6, orderBook.getOrdersBySide(BUY).size());
        assertEquals(
                List.of(
                        marketData2.priceList.get(1),
                        marketData1.priceList.get(0),
                        marketData1.priceList.get(1),
                        marketData2.priceList.get(0),
                        marketData1.priceList.get(2),
                        marketData2.priceList.get(2)
                ),
                orderBook.getOrdersBySide(BUY).stream()
                        .map(order -> order.priceLevel)
                        .collect(toUnmodifiableList())
        );
    }

    @Test
    void should_sort_offers_by_price_quantity_and_source() {
        // given
        var marketData1 = new MarketData(
                "participant1",
                "AUDUSD",
                List.of(
                        new PriceLevel(BigDecimal.valueOf(20), 1, SELL),
                        new PriceLevel(BigDecimal.valueOf(10), 2, SELL),
                        new PriceLevel(BigDecimal.valueOf(5), 10, SELL)
                )
        );

        var marketData2 = new MarketData(
                "participant2",
                "AUDUSD",
                List.of(
                        new PriceLevel(BigDecimal.valueOf(10), 1, SELL),
                        new PriceLevel(BigDecimal.valueOf(30), 2, SELL),
                        new PriceLevel(BigDecimal.valueOf(5), 10, SELL)
                )
        );

        // when
        orderBook.handleMarketDataEvent(marketData2);
        orderBook.handleMarketDataEvent(marketData1);

        // given
        assertEquals(6, orderBook.getOrdersBySide(SELL).size());
        assertEquals(
                List.of(
                        marketData1.priceList.get(2),
                        marketData2.priceList.get(2),
                        marketData1.priceList.get(1),
                        marketData2.priceList.get(0),
                        marketData1.priceList.get(0),
                        marketData2.priceList.get(1)
                ),
                orderBook.getOrdersBySide(SELL).stream()
                        .map(order -> order.priceLevel)
                        .collect(toUnmodifiableList())
        );
    }

    @Test
    void should_return_0_quantity__when_no_matching_price() {
        // given
        var marketData = new MarketData(
                "participant1",
                "AUDUSD",
                List.of(
                        new PriceLevel(TEN, 1, BUY),
                        new PriceLevel(ONE, 2, SELL)
                )
        );
        orderBook.handleMarketDataEvent(marketData);

        // when / then
        assertEquals(0, orderBook.getTotalQuantityForPriceAndSide(BigDecimal.valueOf(5), BUY));
    }

    @ParameterizedTest
    @EnumSource
    void should_calculate_total_quantity__for_price(Side side) {
        // given
        var marketData1 = new MarketData(
                "participant1",
                "AUDUSD",
                List.of(
                        new PriceLevel(TEN, 1, side),
                        new PriceLevel(ONE, 2, side)
                )
        );

        var marketData2 = new MarketData(
                "participant2",
                "AUDUSD",
                List.of(
                        new PriceLevel(TEN, 1, side),
                        new PriceLevel(ONE, 2, side)
                )
        );

        orderBook.handleMarketDataEvent(marketData1);
        orderBook.handleMarketDataEvent(marketData2);

        // when / then
        assertEquals(4, orderBook.getTotalQuantityForPriceAndSide(ONE, side));
    }

    @Test
    void should_return_0_VWAP__when_no_matching_quantity() {
        // given
        var marketData = new MarketData(
                "participant1",
                "AUDUSD",
                List.of(
                        new PriceLevel(TEN, 1, BUY),
                        new PriceLevel(ONE, 2, SELL)
                )
        );
        orderBook.handleMarketDataEvent(marketData);

        // when / then
        assertEquals(ZERO, orderBook.getVwapForQuantityAndSide(3, BUY));
    }

    @ParameterizedTest
    @EnumSource
    void should_calculate_VWAP__for_quantity(Side side) {
        // given
        var marketData1 = new MarketData(
                "participant1",
                "AUDUSD",
                List.of(
                        new PriceLevel(TEN, 1, side),
                        new PriceLevel(ONE, 2, side)
                )
        );

        var marketData2 = new MarketData(
                "participant2",
                "AUDUSD",
                List.of(
                        new PriceLevel(TEN, 1, side),
                        new PriceLevel(ONE, 2, side)
                )
        );

        orderBook.handleMarketDataEvent(marketData1);
        orderBook.handleMarketDataEvent(marketData2);

        // when / then
        assertEquals(TEN, orderBook.getVwapForQuantityAndSide(1, side));
    }

    @Test
    void should_remove_data__on_reset() {
        // given
        givenInitialMarketDataSet();

        // when
        orderBook.reset();

        // given
        assertTrue(orderBook.getOrdersBySide(BUY).isEmpty());
        assertTrue(orderBook.getOrdersBySide(SELL).isEmpty());
    }

    private void givenInitialMarketDataSet() {
        var marketData1 = new MarketData(
                "participant1",
                "AUDUSD",
                List.of(
                        new PriceLevel(TEN, 1, BUY),
                        new PriceLevel(ONE, 2, SELL)
                )
        );

        orderBook.handleMarketDataEvent(marketData1);
    }

}