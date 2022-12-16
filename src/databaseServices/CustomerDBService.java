package databaseServices;

import shoppingCartAPI.*;

public interface CustomerDBService {
	// login
	public Customer authenticate(String username, String password);
	
	// get cart
	public Cart getCartFromCustomer(Customer customer);
	
	/**
	 * Checks if the customer exceeds the bad discount limit
	 * @param customer
	 * @return true if customer does not exceed bad discount limit, false otherwise
	 */
	public boolean verifyBadDiscountCodes(Customer customer);
	
	/**
	 * 
	 * @param customer
	 * @param price
	 * @return the amount taxed
	 */
	public double calculateTaxForCustomer(Customer customer, double price);
}
