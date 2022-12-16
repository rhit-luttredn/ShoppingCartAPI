package shoppingCartAPI;

import databaseServices.*;

public class Product {
	private int productId;
	private double price;
	private ProductDBService productDB;
	
	public Product(int productId, double price, ProductDBService productDB) {
		this.productId = productId;
		this.price = price;
		this.productDB = productDB;
	}
	
	/**
	 * Gets the current stock of the 
	 * @return the stock of the product
	 */
	public int getStock() {
		return productDB.getStockByProductId(this.productId);
	}
	
	public double getPrice() {
		return this.price;
	}
	
	public int getProductId() {
		return this.productId;
	}
}
