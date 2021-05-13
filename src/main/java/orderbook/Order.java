package orderbook;

import orderbook.marketdata.PriceLevel;

import static java.util.Objects.requireNonNull;

public class Order {

    public final String source;
    public final PriceLevel priceLevel;

    public Order(String source, PriceLevel priceLevel) {
        this.source = requireNonNull(source, "source");
        this.priceLevel = requireNonNull(priceLevel, "priceLevel");
    }


    @Override
    public String toString() {
        return "Order{" +
                "source='" + source + '\'' +
                ", priceLevel=" + priceLevel +
                '}';
    }
}
