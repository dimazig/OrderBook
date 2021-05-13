package orderbook.marketdata;

import org.junit.jupiter.api.Test;

import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static orderbook.marketdata.Side.BUY;
import static org.junit.jupiter.api.Assertions.*;

class PriceLevelTest {

    @Test
    public void should_not_accept_non_positive_quantity() {
        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> new PriceLevel(TEN, 0, BUY));

        // then
        assertEquals("quantity must be positive", exception.getMessage());
    }

    @Test
    public void should_not_accept_non_positive_price() {
        // when
        var exception = assertThrows(IllegalArgumentException.class, () -> new PriceLevel(ZERO, 1, BUY));

        // then
        assertEquals("price must be positive", exception.getMessage());
    }

}