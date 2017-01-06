package clients.cashier;

import catalogue.BetterBasket;
import middle.MiddleFactory;

public class BetterCashierModel extends CashierModel {

    public BetterCashierModel(MiddleFactory mf) {
        super(mf);
    }
    
    @Override
    protected BetterBasket makeBasket() {
        // Makes use of the new BetterBasket class
        return new BetterBasket();
    }

    
}
