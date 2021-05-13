package orderbook.marketdata;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toUnmodifiableList;
import static orderbook.marketdata.MarketData.MAX_ELEMENTS_PER_SIDE;
import static orderbook.marketdata.Side.BUY;
import static orderbook.marketdata.Side.SELL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MarketDataTest {

    @Test
    void should_throw_when_amount_of_bids_exceeded_limit() {
        // given
        var priceList = IntStream.range(1, MAX_ELEMENTS_PER_SIDE + 2)
                .mapToObj(i -> new PriceLevel(BigDecimal.TEN, i, BUY))
                .collect(toUnmodifiableList());

        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> new MarketData("participant1", "AUDUSD", priceList));

        // then
        assertEquals("No more than 64 elements for BUY side can be specified but the actual amount was 65", exception.getMessage());
    }

    @Test
    void should_throw_when_amount_of_offers_exceeded_limit() {
        // given
        var priceList = IntStream.range(1, MAX_ELEMENTS_PER_SIDE + 2)
                .mapToObj(i -> new PriceLevel(BigDecimal.TEN, i, SELL))
                .collect(toUnmodifiableList());

        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> new MarketData("participant1", "AUDUSD", priceList));

        // then
        assertEquals("No more than 64 elements for SELL side can be specified but the actual amount was 65", exception.getMessage());
    }



}