package orderbook.marketdata;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public class PriceLevel {

    public final Side side;
    public final BigDecimal price;
    public final long quantity;

    public PriceLevel(BigDecimal price, long quantity, Side side) {
        this.price = requireNonNull(price, "price");
        this.quantity = quantity;
        this.side = requireNonNull(side, "side");

        if (!(quantity > 0 )) throw new IllegalArgumentException("quantity must be positive");
        if (price.compareTo(BigDecimal.ZERO) < 1) throw new IllegalArgumentException("price must be positive");
    }

    @Override
    public String toString() {
        return "PriceLevel{" +
                "side=" + side +
                ", price=" + price +
                ", quantity=" + quantity +
                '}';
    }
}
