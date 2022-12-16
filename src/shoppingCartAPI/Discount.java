package shoppingCartAPI;

import databaseServices.*;

public class Discount {
	private String discountCode;
	private int requiredProductId;
	private DiscountDBService discountDB;
	
	public Discount(String code, int productId, DiscountDBService discountDB) {
		this.discountCode = code;
		this.requiredProductId = productId;
		this.discountDB = discountDB;
	}
	
	/**
	 * Checks if the discount code is expired
	 * @return false if discount is not expired, true otherwise
	 */
	public boolean checkValidCode() {
		return discountDB.checkDiscountValid(this);
	}
	
	public boolean validateForItem(CartItem item) {
		return discountDB.validateDiscountForItem(this, item);
	}
	
	/**
	 * Applies this discount to an item
	 * @param item: Item to apply discount to
	 * @return price saved from discount
	 */
	public double applyToItem(CartItem item) {
		// TODO: validate discount
		return discountDB.applyDiscountToItem(this, item);
	}
	
	public int getProductId() {
		return requiredProductId;
	}

	public static class BadDiscountCodeException extends Exception {
		
	}
}
