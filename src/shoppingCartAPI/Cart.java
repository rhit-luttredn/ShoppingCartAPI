package shoppingCartAPI;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import databaseServices.*;

public class Cart {
	private int cartId;
	private Customer customer;
	private HashMap<Integer, CartItem> itemsByProductId;
	private HashMap<Integer, List<Discount>> discountsByProductId;
	private CartDBService cartDB;

	public Cart(int cartId, Customer customer, List<CartItem> items, List<Discount> discounts, CartDBService cartDB) {
		this.cartId = cartId;
		this.cartDB = cartDB;
		this.customer = customer;

		// populate the items
		this.itemsByProductId = new HashMap<>();
		for (CartItem item : items) {
			itemsByProductId.put(item.getProductId(), item);
		}

		// populate the discounts
		this.discountsByProductId = new HashMap<>();
		for (Discount discount : discounts) {
			if (!discountsByProductId.containsKey(discount.getProductId()))
				discountsByProductId.put(discount.getProductId(), new LinkedList<>());
			discountsByProductId.get(discount.getProductId()).add(discount);
		}
	}

	public List<CartItem> getContents() {
		return new LinkedList<CartItem>(itemsByProductId.values());
	}

	/**
	 * Calculates the amount saved from discounts
	 * 
	 * @return the amount saved from discounts
	 */
	public double calculateDiscountSavings() {
		double saved = 0;
		for (Entry<Integer, List<Discount>> entry : discountsByProductId.entrySet()) {
			Integer productId = entry.getKey();
			LinkedList<Discount> discounts = (LinkedList<Discount>) entry.getValue();
			CartItem item = itemsByProductId.get(productId);
			for (Discount discount : discounts) {
				saved += discount.applyToItem(item);
			}
		}
		return saved;
	}

	/**
	 * Calculate the total cost of the cart
	 * 
	 * @return total cost of cart
	 */
	public double calculateTotalCost() {
		double price = 0;
		for (CartItem item : itemsByProductId.values())
			price += item.calculatePrice();
		
		price -= calculateDiscountSavings();
		price += customer.calculateTax(price);
		return price;
	}

	/**
	 * Calculates the price of tax
	 * 
	 * @return the calculated price of tax
	 */
	public double estimateTax() {
		double price = 0;
		for (CartItem item : itemsByProductId.values())
			price += item.calculatePrice();
		
		price -= calculateDiscountSavings();
		return customer.calculateTax(price);
	}
	
	public HashMap<Integer, List<Discount>> getDiscounts() {
		return this.discountsByProductId;
	}

	/**
	 * Adds an item to the cart
	 * 
	 * @param item
	 * @return true on successful addition of item to cart, false on failure
	 * @throws OutOfStockException if the product is out of stock
	 */
	public boolean addItem(CartItem item) throws CartItem.OutOfStockException {
		if (!item.verifyStock())
			throw new CartItem.OutOfStockException();
		if (!cartDB.addItemToCart(item, this))
			return false;
		itemsByProductId.put(item.getProductId(), item);
		return true;
	}

	/**
	 * Removes item from the cart
	 * 
	 * @param item
	 */
	public void removeItem(CartItem item) {
		cartDB.removeItemFromCart(item, this);
		for (Discount discount : discountsByProductId.get(item.getProductId())) {
			cartDB.removeDiscountFromCart(discount, this);
		}

		itemsByProductId.remove(item.getProductId());
		discountsByProductId.remove(item.getProductId());
	}

	/**
	 * 
	 * @param discount
	 * @return true if discount was successfully added to cart, false otherwise
	 * @throws Exception if customer exceeds bad code limit, MissingProductException
	 *                   if the cart does not contain the needed product,
	 *                   BadDiscountCodeException if the discount code is invalid
	 */
	public boolean addDiscount(Discount discount) throws Exception {
		if (!discount.checkValidCode())
			throw new Discount.BadDiscountCodeException();

		if (!itemsByProductId.containsKey(discount.getProductId()))
			throw new MissingProductException(discount.getProductId());
		
		if (!customer.checkBadDiscountCodes())
			throw new Exception();

		if (!cartDB.addDiscountToCart(discount, this))
			return false;

		if (!discountsByProductId.containsKey(discount.getProductId()))
			discountsByProductId.put(discount.getProductId(), new LinkedList<>());
		discountsByProductId.get(discount.getProductId()).add(discount);
		return true;
	}

	public void removeDiscount(Discount discount) {
		cartDB.removeDiscountFromCart(discount, this);
		discountsByProductId.remove(discount.getProductId());
	}

	/**
	 * 
	 * @param item
	 * @param quantity
	 */
	public boolean handleModifyItemQuantity(CartItem item, int quantity) throws CartItem.OutOfStockException{
		if (!item.setQuantity(quantity))
			throw new CartItem.OutOfStockException();

		if (!cartDB.updateItemQuantityInCart(item, quantity, this))
			return false;
		
		if (item.getQuantity() == 0)
			removeItem(item);
		
		return true;
	}

	public static class MissingProductException extends Exception {
		int missingProduct;

		public MissingProductException(int missingProduct) {
			this.missingProduct = missingProduct;
		}
	}
}
