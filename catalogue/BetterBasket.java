package catalogue;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;

/**
 * Write a description of class BetterBasket here.
 * 
 * @author  Arran Gravestock
 * @version 1.0
 */
public class BetterBasket extends Basket implements Serializable
{
  private static final long serialVersionUID = 1L;  
  
  /**
   * Check the basket for a matching product
   * Increase the quantity of the matching product
   */  
  @Override
  public boolean add( Product pr ) {
        for (Product temp : this) {
            if (temp.getProductNum().equals(pr.getProductNum())) { //look for the matching product numbers
                temp.setQuantity(temp.getQuantity() + pr.getQuantity()); //set the new quantity of the product
                return true;
            }
        }
        boolean changed = super.add(pr);
        
        Collections.sort(this, new Comparator<Product>() {
            public int compare(Product p1, Product p2) {
                return p1.getProductNum().compareTo(p2.getProductNum());
            }
        });
        
		return changed;
  }
}
