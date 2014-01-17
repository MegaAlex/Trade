package me.josvth.trade.transaction.action.trader.offer;

import me.josvth.trade.transaction.Trader;
import me.josvth.trade.transaction.offer.Offer;

import java.util.HashMap;
import java.util.Map;

public class SetOfferAction extends OfferAction {

    final Map<Integer, Offer> changes = new HashMap<Integer, Offer>();

    public SetOfferAction(Trader trader, int offerIndex, Offer offer) {
        super(trader);
        changes.put(offerIndex, offer);
    }

    @Override
    public Map<Integer, ? extends Offer> getChanges() {
        return changes;
    }

    @Override
    public void execute() {

        for (Map.Entry<Integer, Offer> entry : changes.entrySet()) {
            getTrader().getOffers().set(entry.getKey(), entry.getValue());
        }

        super.execute();

    }

}
