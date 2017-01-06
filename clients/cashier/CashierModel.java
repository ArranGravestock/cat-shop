package clients.cashier;

import java.util.ArrayList;
import catalogue.Basket;
import catalogue.BetterBasket;
import catalogue.Product;
import debug.DEBUG;
import middle.*;

import java.util.Observable;

/**
 * Implements the Model of the cashier client
 * @author  Mike Smith University of Brighton
 * @version 1.0
 */
public class CashierModel extends Observable
{
  private enum State { process, checked }

  private State       theState   = State.process;   // Current state
  private Product     theProduct = null;            // Current product
  private Basket      theBasket  = null;            // Bought items

  private String      pn = "";                      // Product being processed

  private StockReadWriter theStock     = null;
  private OrderProcessing theOrder     = null;

  /**
   * Construct the model of the Cashier
   * @param mf The factory to create the connection objects
   */

  public CashierModel(MiddleFactory mf)
  {
    try                                           // 
    {      
      theStock = mf.makeStockReadWriter();        // Database access
      theOrder = mf.makeOrderProcessing();        // Process order
    } catch ( Exception e )
    {
      DEBUG.error("CashierModel.constructor\n%s", e.getMessage() );
    }
    theState   = State.process;                  // Current state
  }
  
  /**
   * Get the Basket of products
   * @return basket
   */
  public Basket getBasket()
  {
    return theBasket;
  }
  
  /**
   * Check if the product is in Stock
   * @param productNum The product number
   */
  public void doCheck(String productNum, String qty )
  {
    String theAction = "";
    theState  = State.process;                  // State process
    pn  = productNum.trim();                    // Product no.
    int    amount  = Integer.parseInt(qty);                         //  & quantity
    try
    {
      if ( theStock.exists( pn ) )              // Stock Exists?
      {                                         // T
        Product pr = theStock.getDetails(pn);   //  Get details
        if ( pr.getQuantity() >= amount )       //  In stock?
        {                                       //  T
          theAction =                           //   Display 
            String.format( "%s : %7.2f (%2d) ", //
              pr.getDescription(),              //    description
              pr.getPrice(),                    //    price
              pr.getQuantity() );               //    quantity     
          theProduct = pr;                      //   Remember prod.
          theProduct.setQuantity( amount );     //    & quantity
          theState = State.checked;             //   OK await BUY 
        } else {                                //  F
          theAction =                           //   Not in Stock
            pr.getDescription() +" not in stock";
        }
      } else {                                  // F Stock exists
        theAction =                             //  Unknown
          "Unknown product number " + pn;       //  product no.
      }
    } catch( StockException e )
    {
      DEBUG.error( "%s\n%s", 
            "CashierModel.doCheck", e.getMessage() );
      theAction = e.getMessage();
    }
    setChanged(); notifyObservers(theAction);
  }

  /**
   * Buy the product
   */
  public void doBuy(String qty)
  {
    String theAction = "";
    int    amount  = Integer.parseInt(qty);                         //  & quantity
   
    try
    {
      if ( theState != State.checked || amount != theProduct.getQuantity())          // Not checked
      {                                         //  with customer
        theAction = "Check if OK with customer first";
      } else {
        boolean stockBought =                   // Buy
          theStock.buyStock(                    //  however
            theProduct.getProductNum(),         //  may fail              
            theProduct.getQuantity() );         //
        if ( stockBought )                      // Stock bought
        {                                       // T
          makeBasketIfReq();                    //  new Basket ?
          theBasket.add( theProduct );          //  Add to bought
          theAction = "Purchased " +            //    details
                  theProduct.getDescription();  //
        } else {                                // F
          theAction = "!!! Not in stock";       //  Now no stock
        }
      }
    } catch( StockException e )
    {
      DEBUG.error( "%s\n%s", 
            "CashierModel.doBuy", e.getMessage() );
      theAction = e.getMessage();
    }
    theState = State.process;                   // All Done
    setChanged(); notifyObservers(theAction);
  }
  
  /**
   * Remove the product number
   */
  public void doRemove(String index, String qty)
  {
      Boolean found = false;
      Product p = null;
      String theAction = "No item found in basket!";
      try {
          if (theBasket != null) {              //T if the basket has items
              for (Product item: theBasket) {     //search every item in the basket
                  if (item.getProductNum().equals(index)){    // the product numbers match
                        found = true;
                        p = item;
                    } else {
                        theAction = "Item was not found in the basket!";
                  }
              }
        
              if (found) {      //process the request as it exists in the basket             
                    theAction = "Item " + p.getDescription() + " x" + Integer.parseInt(qty) + " removed!";
                    if (p.getQuantity() > Integer.parseInt(qty)) {
                        p.setQuantity(p.getQuantity() - Integer.parseInt(qty));
                        theStock.addStock(p.getProductNum(), Integer.parseInt(qty));    //readd the stock
                    } else if (p.getQuantity() < Integer.parseInt(qty)) {
                        theAction = "You are trying to remove too many!";
                    } else {
                        theBasket.remove(p);
                        theStock.addStock(p.getProductNum(), Integer.parseInt(qty));    //readd the stock
                    }
              }
          } else {
              theAction = "The basket is empty!";
          }
      } catch(StockException e ) 
      {
        DEBUG.error( "%s\n%s", 
             "CashierModel.doRemove", e.getMessage() );
        theAction = e.getMessage();
      }
      setChanged(); notifyObservers(theAction);
  }

    /**
   * Remove all items in the basket
   */
  public void doClear()
  {
      String theAction = "Basket is empty!";
      try
      {
          if (theBasket != null && theBasket.size() >= 1)       //check the basket contains any products
          {
              for (Product item : theBasket) {      //remove all the items
                  theStock.addStock(item.getProductNum(), item.getQuantity());
              }
              theBasket = null;
              theAction = "Basket has been cleared!";
          }
      } catch( Exception e )
      {
          DEBUG.error( "%s\n%s", 
            "CashierModel.doClear", e.getMessage() );
      theAction = e.getMessage();
    }
    setChanged(); notifyObservers(theAction);
  }
  
  /**
   * Customer pays for the contents of the basket
   */
  public void doBought(String qty)
  {
    String theAction = "";
    int    amount  = Integer.parseInt(qty);                       //  & quantity
    try
    {
      if ( theBasket != null &&
           theBasket.size() >= 1 )            // items > 1
      {                                       // T
        theOrder.newOrder( theBasket );       //  Process order
        theBasket = null;                     //  reset
      }                                       //
      theAction = "Next customer";            // New Customer
      theState = State.process;               // All Done
      theBasket = null;
    } catch( OrderException e )
    {
      DEBUG.error( "%s\n%s", 
            "CashierModel.doCancel", e.getMessage() );
      theAction = e.getMessage();
    }
    theBasket = null;
    setChanged(); notifyObservers(theAction); // Notify
  }

  /**
   * ask for update of view callled at start of day
   * or after system reset
   */
  public void askForUpdate()
  {
    setChanged(); notifyObservers("Welcome");
  }
  
  /**
   * make a Basket when required
   */
  private void makeBasketIfReq()
  {
    if ( theBasket == null )
    {
      try
      {
        int uon   = theOrder.uniqueNumber();     // Unique order num.
        theBasket = makeBasket();                //  basket list
        theBasket.setOrderNum( uon );            // Add an order number
      } catch ( OrderException e )
      {
        DEBUG.error( "Comms failure\n" +
                     "CashierModel.makeBasket()\n%s", e.getMessage() );
      }
    }
  }

  /**
   * return an instance of a new Basket
   * @return an instance of a new Basket
   */
  protected Basket makeBasket()
  {
    return new Basket();
  }
}
  
