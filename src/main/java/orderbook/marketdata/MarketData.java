package orderbook.marketdata;

import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static orderbook.marketdata.Side.BUY;

public class MarketData {
    static final int MAX_ELEMENTS_PER_SIDE = 64;

    public final String source;
    public final String instrument;
    public final List<PriceLevel> priceList;

    public MarketData(String source, String instrument, List<PriceLevel> priceList) {
        this.source = requireNonNull(source, "source");
        this.instrument = requireNonNull(instrument);
        this.priceList = unmodifiableList(requireNonNull(priceList, "priceList"));

        validate(priceList);
    }

    private void validate(List<PriceLevel> priceList) {
        final var buyCount = priceList.stream().filter(price -> price.side == BUY).count();
        if (buyCount > MAX_ELEMENTS_PER_SIDE) throw new IllegalArgumentException(String.format(
                "No more than %s elements for BUY side can be specified but the actual amount was %s",
                MAX_ELEMENTS_PER_SIDE, buyCount
        ));

        var sellCount = priceList.size() - buyCount;
        if (sellCount > MAX_ELEMENTS_PER_SIDE) throw new IllegalArgumentException(String.format(
                "No more than %s elements for SELL side can be specified but the actual amount was %s",
                MAX_ELEMENTS_PER_SIDE, sellCount
        ));
    }

    @Override
    public String toString() {
        return "MarketData{" +
                "source='" + source + '\'' +
                ", instrument='" + instrument + '\'' +
                ", priceList=" + priceList +
                '}';
    }
}
