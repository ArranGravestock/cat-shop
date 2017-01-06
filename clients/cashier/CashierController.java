package clients.cashier;


/**
 * The Cashier Controller
 * @author M A Smith (c) June 2014
 */

public class CashierController
{
  private BetterCashierModel model = null;
  private CashierView  view  = null;

  /**
   * Constructor
   * @param model The model 
   * @param view  The view from which the interaction came
   */
  public CashierController( BetterCashierModel model, CashierView view )
  {
    this.view  = view;
    this.model = model;
  }

  /**
   * Check interaction from view
   * @param pn The product number to be checked
   */
  public void doCheck( String pn, String qty )
  {
    model.doCheck(pn, qty);
  }

   /**
   * Buy interaction from view
   */
  public void doBuy(String qty)
  {
    model.doBuy(qty);
  }
  
  /**
   * Remove interaction from view
   */

  public void doRemove(String pn, String qty)
  {
      model.doRemove(pn, qty);
  }
	
  public void doUndo(String qty)
  {
	  model.doUndo(qty);
  }

   /**
   * Bought interaction from view
   */
  public void doBought(String qty)
  {
    model.doBought(qty);
  }
}
