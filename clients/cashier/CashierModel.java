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
  
  private Product lastItemPurchased = null;
  
  private ArrayList<Product> actionLog = new ArrayList();
  
  /*
  public class Action {
      private String theProductNum;
      private int theQty;
      private String theAction;
      
      public Action( String aProductNum, int aQty, String aAction ) {
          theProductNum = aProductNum;
          theQty = aQty;
          theAction = aAction;
        }
      
      public String getProductNumber() { return theProductNum; }
      public int getQty() { return theQty; }
      public String getAction() { return theAction; }
      
      public void setProductNumber( String aProductNum ) { theProductNum = aProductNum; }
      public void setQuantity( int aQty ) { theQty = aQty; }
      public void setAction( String aAction ) { theAction = aAction; }
  }*/
  
  //private ArrayList<Action> Log = new ArrayList();
  private String lastAction = null;

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
    Product p = null;
   
    try
    {
      if ( theState != State.checked )          // Not checked
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
          if ( theBasket != null ) {
              for (Product item : theBasket) {
                  if (item.getProductNum().equals(theProduct.getProductNum())){
                      p = item;
                      System.out.println(p.getQuantity());
                    }
                } 
          }
          if (p != null) {actionLog.add( p ); System.out.println(actionLog.size() + " " + actionLog.get(1).getQuantity());}
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
          if (theBasket != null) {
              for (Product pr: theBasket) {
                  if (pr.getProductNum().equals(index)){
                        found = true;
                        p = pr;
                    } else {
                        theAction = "Item was not found in the basket!";
                  }
              }
        
              if (found) {                    
                    theAction = "Item " + p.getDescription() + " x" + Integer.parseInt(qty) + " removed!";
                    if (p.getQuantity() > Integer.parseInt(qty)) {
                        actionLog.add(p);
                        p.setQuantity(p.getQuantity() - Integer.parseInt(qty));
                        theStock.addStock(p.getProductNum(), Integer.parseInt(qty));
                        //set the unorderedbasket quantity of the product number found to current - Integer.parseInt(qty)
                    } else if (p.getQuantity() < Integer.parseInt(qty)) {
                        theAction = "You are trying to remove too many!";
                    } else {
                        theBasket.remove(p);
                        actionLog.remove(p);
                        theStock.addStock(p.getProductNum(), Integer.parseInt(qty));
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
   * Remove the last item added
   */
  public void doUndo(String qty) {
      System.out.println( actionLog.get(0).getQuantity());
      Product p = null;
      Boolean found = false;
      Product lastItem = actionLog.get(actionLog.size()-1); //get the item before last
      String theAction = "No item to remove!";

          if( actionLog != null ) {
              for(Product pr : theBasket) {
                  if (pr.getProductNum().equals(lastItem.getProductNum())) {
                      found = true;
                      p = pr;
                    }
              }
          
              if (found) {
                  
                  if (actionLog.size() == 1) {
                      actionLog.remove(lastItem);
                      theBasket.remove(p);
                  } else if ( actionLog.size() > 1 ) {
                      actionLog.remove(lastItem);
                      p.setQuantity(lastItem.getQuantity());
                    }
                }

            } else {
              theAction = "The basket is empty!";
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
  
