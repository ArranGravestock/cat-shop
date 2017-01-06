package catalogue;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import catalogue.*;
import middle.*;

/**
 * The test class BetterBasketTest.
 *
 * @author  (your name)
 * @version (a version number or a date)
 */
public class BetterBasketTest
{
    private Basket theBought1 = null;
    private Basket theBought2 = null;
    private StockReadWriter theStock = null;

    @Before
    public void setUp()
    {
        MiddleFactory mf = new LocalMiddleFactory();
        try
        {
            theStock = mf.makeStockReadWriter();
        } catch (Exception e) {
            fail ("Exception " + e.getMessage());
        }
        
        theBought1 = new Basket();
        theBought2 = new BetterBasket();
        
        try
        {
            theBought1.add(theStock.getDetails("0005"));
            theBought1.add(theStock.getDetails("0003"));
            theBought1.add(theStock.getDetails("0001"));
            theBought1.add(theStock.getDetails("0007"));
            theBought1.add(theStock.getDetails("0007"));
            
            theBought2.add(theStock.getDetails("0005"));
            theBought2.add(theStock.getDetails("0003"));
            theBought2.add(theStock.getDetails("0001"));
            theBought2.add(theStock.getDetails("0007"));
            theBought2.add(theStock.getDetails("0007"));
        } catch (StockException e) {
            fail ("StockException " + e.getMessage());
        }
    }

    @Test
    public void BasketTest1()
    {
        try
        {
            assertEquals(theBought1.size(), 5); //does not allow duplicate quantity
            assertEquals(theBought1.get(4).getProductNum(), "0007"); //last value is 0007 in list because last added
            assertEquals(theBought1.get(0).getProductNum(), "0005"); //first value is 0005 because first added           
        } catch (Exception e) {
            fail("Exception " + e.getMessage() );
        }    
    }
    
    @Test
    public void BetterBasketTest1()
    {
        try
        {
            assertEquals(theBought2.size(), 4); //allows for duplicate quantity
            assertEquals(theBought2.get(3).getProductNum(), "0007"); //last value is 0007 in list because sorted order
            assertEquals(theBought2.get(0).getProductNum(), "0001"); //first value is 0001 because sorted order
        } catch (Exception e) {
            fail("Exception " + e.getMessage() );
        }   
    }
}
