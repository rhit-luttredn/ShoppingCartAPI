package databaseServices;

import shoppingCartAPI.*;

public interface CartDBService {	
	// get cart by id
	public Cart getCartById(Cart cart);
	
	// add item
	public boolean addItemToCart(CartItem item, Cart cart);
	
	// remove item
	public void removeItemFromCart(CartItem item, Cart cart);
	
	// add discount
	public boolean addDiscountToCart(Discount discount, Cart cart);
	
	// remove discount
	public void removeDiscountFromCart(Discount discount, Cart cart);
	
	// update item quantity
	public boolean updateItemQuantityInCart(CartItem item, int quantity, Cart cart);
}
