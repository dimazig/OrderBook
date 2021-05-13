package orderbook;

import orderbook.marketdata.MarketData;
import orderbook.marketdata.Side;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toUnmodifiableList;
import static orderbook.marketdata.Side.BUY;
import static orderbook.marketdata.Side.SELL;

public class OrderBook {

    public static final Comparator<Order> BID_COMPARATOR = ((Comparator<Order>) (order1, order2) -> order2.priceLevel.price.compareTo(order1.priceLevel.price))
            .thenComparing((order1, order2) -> Long.compare(order2.priceLevel.quantity, order1.priceLevel.quantity))
            .thenComparing(order -> order.source);

    public static final Comparator<Order> OFFER_COMPARATOR = ((Comparator<Order>) (order1, order2) -> order1.priceLevel.price.compareTo(order2.priceLevel.price))
            .thenComparing((order1, order2) -> Long.compare(order2.priceLevel.quantity, order1.priceLevel.quantity))
            .thenComparing(order -> order.source);

    private final static Map<Side, Comparator<Order>> COMPARATOR_MAP = Map.of(
            BUY, BID_COMPARATOR,
            SELL, OFFER_COMPARATOR
    );

    private final String instrument;
    private final AtomicReference<Map<Side, List<Order>>> orders = new AtomicReference<>(emptyMap());

    public OrderBook(String instrument) {
        this.instrument = instrument;
    }

    public void handleMarketDataEvent(MarketData marketData) {
        validate(marketData);

        Map<Side, List<Order>> orderMap;
        Map<Side, List<Order>> updatedOrderMap;
        do {
            orderMap = orders.get();
            updatedOrderMap = new HashMap<>();

            for (int i = 0; i < Side.values().length; i++) {
                Side side = Side.values()[i];

                final var orders = orderMap.getOrDefault(side, emptyList());

                var updatedOrders = Stream.concat(
                        orders.stream()
                                .filter(order -> !order.source.equals(marketData.source)),
                        marketData.priceList.stream()
                                .filter(price -> price.side == side)
                                .map(price -> new Order(marketData.source, price))
                ).sorted(COMPARATOR_MAP.get(side)
                ).collect(toUnmodifiableList());

                updatedOrderMap.put(side, updatedOrders);
            }
        } while (!orders.compareAndSet(orderMap, unmodifiableMap(updatedOrderMap)));
    }

    public void reset() {
        orders.set(emptyMap());
    }

    public List<Order> getOrdersBySide(Side side) {
        return orders.get().getOrDefault(side, emptyList());
    }

    public long getTotalQuantityForPriceAndSide(BigDecimal price, Side side) {
        return orders.get().getOrDefault(side, emptyList())
                .stream()
                .filter(order -> order.priceLevel.price.compareTo(price) == 0)
                .mapToLong(order -> order.priceLevel.quantity)
                .sum();
    }

    public BigDecimal getVwapForQuantityAndSide(long quantity, Side side) {
        var sideOrders = orders.get().getOrDefault(side, emptyList());

        var count =  sideOrders
                .stream()
                .filter(order -> order.priceLevel.quantity == quantity)
                .count();

        if (count == 0) return ZERO;

        var dividend = sideOrders
                .stream()
                .filter(order -> order.priceLevel.quantity == quantity)
                .map(order -> order.priceLevel.price.multiply(BigDecimal.valueOf(order.priceLevel.quantity)))
                .reduce(ZERO, BigDecimal::add);


        var divisor = BigDecimal.valueOf(quantity *count);

        return dividend.divide(divisor, RoundingMode.HALF_EVEN);
    }

    private void validate(MarketData marketData) {
        if (!instrument.equals(marketData.instrument)) throw new IllegalArgumentException(
                String.format("Incorrect instrument, %s required, but %s provided", instrument, marketData.instrument)
        );
    }
}
