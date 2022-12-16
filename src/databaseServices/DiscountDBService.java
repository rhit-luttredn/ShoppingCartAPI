package databaseServices;

import shoppingCartAPI.*;

public interface DiscountDBService {
	// get discount by code
	public Discount getDiscountByCode(String discountCode);
	
	// check if discount is expired
	public boolean checkDiscountValid(Discount discount);
	
	/**
	 * Checks if the discount is valid for the given item
	 * @param discount
	 * @param item
	 * @return
	 */
	public boolean validateDiscountForItem(Discount discount, CartItem item);
	
	/**
	 * 
	 * @param discount
	 * @param item
	 * @return the amount saved by the discount
	 */
	public double applyDiscountToItem(Discount discount, CartItem item);
	
	// verify discount is applied to correct item
	public Product getProductForDiscount(Discount discount);
	
	// verify discount is applied to correct quantity
	public int getQuantityOfProductForDiscount(Discount discount, Product product);
}
