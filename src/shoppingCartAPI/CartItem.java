package shoppingCartAPI;

import databaseServices.*;

public class CartItem {
	private Product product;
	private int quantity;
	private CartDBService cartDB;
	
	public CartItem(Product product, int quantity, CartDBService cartDB) {
		this.product = product;
		this.quantity = quantity;
		this.cartDB = cartDB;
	}
	
	public int getProductId() {
		return this.product.getProductId();
	}
	
	public double calculatePrice() {
		return this.product.getPrice() * this.quantity;
	}
	
	public boolean verifyStock() {
		return this.quantity <= this.product.getStock();
	}
	
	public boolean setQuantity(int quantity) throws OutOfStockException {
		if (!verifyStock())
			throw new OutOfStockException();
		
		if (quantity > product.getStock())
			return false;
		
		this.quantity = quantity;
		return true;
	}
	
	public int getQuantity() {
		return quantity;
	}
	
	public static class OutOfStockException extends Exception {
		
	}
}
