package shoppingCartAPI;

import databaseServices.*;

public class Customer {
	private int userId;
	private String username;
	private String email;
	private String address;
	private CustomerDBService customerDB;

	public Customer(int userId, String username, String email, String address, CustomerDBService customerDB) {
		this.userId = userId;
		this.username = username;
		this.email = email;
		this.address = address;
		this.customerDB = customerDB;
	}

	/**
	 * Gets the customer's cart from the DB
	 * 
	 * @return the customer's cart
	 */
	public Cart getCart() {
		return customerDB.getCartFromCustomer(this);
	}

	/**
	 * Calculates the tax for a purchase using the Customer's address
	 * 
	 * @param price
	 * @return the price of tax
	 */
	public double calculateTax(double price) {
		return customerDB.calculateTaxForCustomer(this, price);
	}

	/**
	 * 
	 * @return true if the user has not exceeded the bad discount code limit, false
	 *         otherwise
	 */
	public boolean checkBadDiscountCodes() {
		return customerDB.verifyBadDiscountCodes(this);
	}
}
