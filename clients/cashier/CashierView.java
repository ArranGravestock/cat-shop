package clients.cashier;

import catalogue.Basket;
import middle.MiddleFactory;
import middle.OrderProcessing;
import middle.StockReadWriter;
import javax.swing.*;
import java.awt.*;

import java.util.Observable;
import java.util.Observer;

/**
 * View of the model
 * @author  M A Smith (c) June 2014  
 */
public class CashierView implements Observer
{
  private static final int H = 300;       // Height of window pixels
  private static final int W = 400;       // Width  of window pixels
  
  private static final String CHECK  = "Check";
  private static final String BUY    = "Buy";
  private static final String BOUGHT = "Bought";
  private static final String REMOVE = "Remove";
  private static final String CLEAR  = "Clear";
  private static final String PLUS   = "+";
  private static final String MINUS  = "-";

  private final JLabel      theAction  = new JLabel();
  private final JTextField  theInput   = new JTextField();
  private final JTextField  theQty     = new JTextField();
  private final JTextArea   theOutput  = new JTextArea();
  private final JScrollPane theSP      = new JScrollPane();
  private final JButton     theBtCheck = new JButton( CHECK );
  private final JButton     theBtBuy   = new JButton( BUY );
  private final JButton     theBtBought= new JButton( BOUGHT );
  private final JButton     theBtRemove= new JButton( REMOVE );
  private final JButton     theBtPlus  = new JButton( PLUS );
  private final JButton     theBtMinus = new JButton( MINUS );
  private final JButton     theBtClear = new JButton( CLEAR );

  private StockReadWriter theStock     = null;
  private OrderProcessing theOrder     = null;
  private CashierController cont       = null;
  
  private enum State { process, checked }
  
  private State       theState   = State.process;   // Current state
  
  /**
   * Construct the view
   * @param rpc   Window in which to construct
   * @param mf    Factor to deliver order and stock objects
   * @param x     x-coordinate of position of window on screen 
   * @param y     y-coordinate of position of window on screen  
   */        
  public CashierView(  RootPaneContainer rpc,  MiddleFactory mf, int x, int y  )
  {
    try                                           // 
    {      
      theStock = mf.makeStockReadWriter();        // Database access
      theOrder = mf.makeOrderProcessing();        // Process order
    } catch ( Exception e )
    {
      System.out.println("Exception: " + e.getMessage() );
    }
    Container cp         = rpc.getContentPane();    // Content Pane
    Container rootWindow = (Container) rpc;         // Root Window
    cp.setLayout(null);                             // No layout manager
    rootWindow.setSize( W, H );                     // Size of Window
    rootWindow.setLocation( x, y );

    Font f = new Font("Monospaced",Font.PLAIN,12);  // Font f is

    theBtCheck.setBounds( 16, 25+50*0, 80, 30 );    // Check Button
    theBtCheck.addActionListener(                   // Call back code
      e -> cont.doCheck( theInput.getText(), theQty.getText() ) );
    cp.add( theBtCheck );                           //  Add to canvas

    theBtBuy.setBounds( 16, 25+50*1, 80, 30 );      // Buy button 
    theBtBuy.addActionListener(                     // Call back code
      e -> cont.doBuy(theQty.getText()) );
    cp.add( theBtBuy );                             //  Add to canvas

    theBtRemove.setBounds( 16, 25+50*2, 80, 30);    // Remove Button
    theBtRemove.addActionListener(                  // Call back code
      e -> cont.doRemove(theInput.getText(),  theQty.getText()) );
    cp.add( theBtRemove );
    
    theBtClear.setBounds(16, 25+50*3, 80, 30);      // Clear button
    theBtClear.addActionListener(                   // Call back code
      e -> cont.doClear() );                        
    cp.add( theBtClear );                           // Add to canvas
    
    theBtBought.setBounds( 16, 25+50*4, 80, 30 );   // Bought Button
    theBtBought.addActionListener(                  // Call back code
      e -> cont.doBought(theQty.getText()) );
    cp.add( theBtBought );                          // Add to canvas
    
    theBtMinus.setBounds( 240+10, 50, 45, 39);      // Minus button
    theBtMinus.addActionListener(                   // Call back code
      e -> {
            if (Integer.parseInt(theQty.getText()) > 1) {                               // If qty is greater than 1, then minus 1
                theQty.setText(String.valueOf(Integer.parseInt(theQty.getText())-1));
            }
          }
    );
    cp.add( theBtMinus );                           // Add to canvas
    
    theQty.setBounds( 240+45+10, 50, 40, 39 );      // Input Area
    theQty.setText("1");                            // Set default 1
    theQty.setHorizontalAlignment(JTextField.CENTER);   // Set the text to the center
    theQty.setEditable(false);                      // Do not allow it to be edited
    cp.add( theQty );                               // Add to canvas
    
    theBtPlus.setBounds( 240+45+40+10, 50, 45, 39); // Plus button
    theBtPlus.addActionListener(                    // Call back code, add 1 to the quantity
      e -> theQty.setText(String.valueOf(Integer.parseInt(theQty.getText())+1))
    );
    cp.add( theBtPlus );                            // Add to canvas

    theAction.setBounds( 110, 25 , 270, 20 );       // Message area
    theAction.setText( "" );                        // Blank
    cp.add( theAction );                            //  Add to canvas

    theInput.setBounds( 110, 50, 140, 40 );         // Input Area
    theInput.setText("");                           // Blank
    cp.add( theInput );                             //  Add to canvas

    theSP.setBounds( 110, 100, 270, 160 );          // Scrolling pane
    theOutput.setText( "" );                        //  Blank
    theOutput.setFont( f );                         //  Uses font  
    cp.add( theSP );                                //  Add to canvas
    theSP.getViewport().add( theOutput );           //  In TextArea
    rootWindow.setVisible( true );                  // Make visible
    theInput.requestFocus();                        // Focus is here
  }

  /**
   * The controller object, used so that an interaction can be passed to the controller
   * @param c   The controller
   */
  public void setController( CashierController c )
  {
    cont = c;
  }

  /**
   * Update the view
   * @param modelC   The observed model
   * @param arg      Specific args 
   */
  @Override
  public void update( Observable modelC, Object arg )
  {
    CashierModel model  = (CashierModel) modelC;
    String      message = (String) arg;
    theAction.setText( message );
    Basket basket = model.getBasket();
    if ( basket == null )
      theOutput.setText( "Customers order" );
    else
      theOutput.setText( basket.getDetails() );
    
    theInput.requestFocus();               // Focus is here
  }
}
