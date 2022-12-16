package testing;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.LinkedList;
import java.util.HashMap;

import org.easymock.*;
import org.junit.*;
import databaseServices.*;
import shoppingCartAPI.*;
import shoppingCartAPI.CartItem.OutOfStockException;

public class TestAll extends EasyMockSupport {
	private HashMap<Integer, CartItem> items;
	private HashMap<String, Discount> discounts;

	@TestSubject
	private Cart cart;
	@TestSubject
	private Customer customer;

	@Mock
	private DatabaseConnectionService dbService = mock(DatabaseConnectionService.class);
	@Mock
	private CartDBService cartDB = mock(CartDBService.class);
	@Mock
	private CustomerDBService customerDB = mock(CustomerDBService.class);
	@Mock
	private DiscountDBService discountDB = mock(DiscountDBService.class);
	@Mock
	private ProductDBService productDB = mock(ProductDBService.class);

	@Before
	public void setup() {
		// initialize a cart and customer to test on
		this.customer = new Customer(0, "DylanDillpickle", "foo@foo.com", "4 Privet Drive", customerDB);

		items = new HashMap<>();
		discounts = new HashMap<>();

		// Create items
		items.put(0, new CartItem(new Product(0, 45.42, productDB), 12, cartDB));
		items.put(1, new CartItem(new Product(1, 33.69, productDB), 4, cartDB));
		items.put(2, new CartItem(new Product(2, 14.91, productDB), 13, cartDB));
		items.put(3, new CartItem(new Product(3, 37.18, productDB), 7, cartDB));
		items.put(4, new CartItem(new Product(4, 6.38, productDB), 6, cartDB));
		items.put(5, new CartItem(new Product(5, 44.82, productDB), 3, cartDB));
		items.put(6, new CartItem(new Product(6, 7.06, productDB), 12, cartDB));
		items.put(7, new CartItem(new Product(7, 28.94, productDB), 9, cartDB));
		items.put(8, new CartItem(new Product(8, 2.16, productDB), 1, cartDB));
		items.put(9, new CartItem(new Product(9, 1.32, productDB), 11, cartDB));

		// Create Discounts
		discounts.put("123", new Discount("123", 0, discountDB));
		discounts.put("456", new Discount("456", 0, discountDB));
		discounts.put("789", new Discount("789", 1, discountDB));
		discounts.put("abc123", new Discount("abc123", 2, discountDB));
		discounts.put("abc456", new Discount("abc456", 3, discountDB));
		discounts.put("abc789", new Discount("abc789", 4, discountDB));

		this.cart = new Cart(0, this.customer, new LinkedList<CartItem>(items.values()),
				new LinkedList<Discount>(discounts.values()), cartDB);
	}

	/* UC0: View Contents of Cart */
	@Test
	public void testGetCartContents() {
		replayAll();
		LinkedList<CartItem> cartContents = (LinkedList<CartItem>) cart.getContents();

		verifyAll();
		assertEquals(items.size(), cartContents.size());
		assertTrue(cartContents.containsAll(items.values()));
	}

	@Test
	public void testSumDiscounts() {
		expect(discountDB.applyDiscountToItem(discounts.get("123"), items.get(0))).andReturn(2.00).times(1);
		expect(discountDB.applyDiscountToItem(discounts.get("456"), items.get(0))).andReturn(3.00).times(1);
		expect(discountDB.applyDiscountToItem(discounts.get("789"), items.get(1))).andReturn(0.50).times(1);
		expect(discountDB.applyDiscountToItem(discounts.get("abc123"), items.get(2))).andReturn(1.00).times(1);
		expect(discountDB.applyDiscountToItem(discounts.get("abc456"), items.get(3))).andReturn(1.25).times(1);
		expect(discountDB.applyDiscountToItem(discounts.get("abc789"), items.get(4))).andReturn(3.00).times(1);

		replayAll();
		double saved = cart.calculateDiscountSavings();

		verifyAll();
		assertEquals(10.75, saved);
	}

	@Test
	public void testEstimateTax() {
		// get the pre-discounted and pre-taxed cost of the items
		double costOfItems = 0;
		for (CartItem item : items.values()) {
			costOfItems += item.calculatePrice();
		}
		
		double discountedAmount = 1.00;
		expect(discountDB.applyDiscountToItem(anyObject(Discount.class), anyObject(CartItem.class)))
				.andReturn(discountedAmount).times(discounts.size());

		double expectedTax = 1.45;
		expect(customerDB.calculateTaxForCustomer(customer, costOfItems-discountedAmount*discounts.size())).andReturn(expectedTax).times(1);

		replayAll();
		double tax = cart.estimateTax();

		verifyAll();
		assertEquals(expectedTax, tax);
	}

	@Test
	public void testCalculateTotalCost() {
		// get the pre-discounted and pre-taxed cost of the items
		double costOfItems = 0;
		for (CartItem item : items.values()) {
			costOfItems += item.calculatePrice();
		}
		
		double discountedAmount = 1.00;
		expect(discountDB.applyDiscountToItem(anyObject(Discount.class), anyObject(CartItem.class)))
				.andReturn(discountedAmount).times(discounts.size());

		double taxAmount = 3.00;
		expect(customerDB.calculateTaxForCustomer(customer, costOfItems - discountedAmount*discounts.size())).andReturn(taxAmount).times(1);

		replayAll();
		double total = cart.calculateTotalCost();

		verifyAll();
		double expected = costOfItems - discountedAmount*discounts.size() + taxAmount;
		assertEquals(expected, total);
	}

	/* UC1: Add Item to Cart */
	@Test
	public void testAddItemToCart() {
		int productId = 10;
		double productPrice = 31.13;
		int itemQuantity = 5;
		CartItem newItem = new CartItem(new Product(productId, productPrice, productDB), itemQuantity, cartDB);
		
		expect(productDB.getStockByProductId(productId)).andReturn(200).times(1);
		expect(cartDB.addItemToCart(newItem, cart)).andReturn(true).times(1);
		
		replayAll();
		try {
			boolean added = cart.addItem(newItem);
			
			verifyAll();
			assertTrue(added);
			assertTrue(cart.getContents().contains(newItem));
		} catch (OutOfStockException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testProductOutOfStock() {
		int productId = 10;
		double productPrice = 31.13;
		int itemQuantity = 5;
		CartItem newItem = new CartItem(new Product(productId, productPrice, productDB), itemQuantity, cartDB);
		
		expect(productDB.getStockByProductId(productId)).andReturn(1).times(1);
		
		replayAll();
		try {
			cart.addItem(newItem);
			fail("Did not throw CartItem.OutOfStockException");
		} catch (OutOfStockException e) {
			verifyAll();
			assertFalse(cart.getContents().contains(newItem));
		}
	}

	/* UC2: Apply Discount Code */
	@Test
	public void testApplyDiscount() {
		String discountCode = "gottlieb";
		int productId = 4;
		Discount newDiscount = new Discount(discountCode, productId, discountDB);
		
		expect(discountDB.checkDiscountValid(newDiscount)).andReturn(true).times(1);
		expect(customerDB.verifyBadDiscountCodes(customer)).andReturn(true).times(1);
		expect(cartDB.addDiscountToCart(newDiscount, cart)).andReturn(true).times(1);
		
		replayAll();
		try {
			boolean added = cart.addDiscount(newDiscount);
			
			verifyAll();
			assertTrue(added);
			assertTrue(cart.getDiscounts().containsKey(productId));
			assertTrue(cart.getDiscounts().get(productId).contains(newDiscount));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Expected discount to be added; instead exception was thrown");
		}
	}

	@Test
	public void testInvalidDiscountCode() {
		String discountCode = "gottlieb";
		int productId = 4;
		Discount newDiscount = new Discount(discountCode, productId, discountDB);
		
		expect(discountDB.checkDiscountValid(newDiscount)).andReturn(false).times(1);
		
		replayAll();
		try {
			boolean added = cart.addDiscount(newDiscount);
			fail("Expected: throw Discount.BadDiscountCodeException; instead discount was added");
		} catch (Discount.BadDiscountCodeException e) {
			verifyAll();
			assertFalse(cart.getDiscounts().get(productId).contains(newDiscount));
		} catch (Cart.MissingProductException e) {
			e.printStackTrace();
			fail("Expected: throw Discount.BadDiscountCodeException; Instead: Cart.MissingProductException was thrown");
		} catch (Exception e) {
			e.printStackTrace();
			fail("Expected: throw Discount.BadDiscountCodeException; Instead: Exception was thrown");
		}
	}

	@Test
	public void testMissingItem() {
		String discountCode = "gottlieb";
		// a product id that does not exist
		int productId = 40020;
		Discount newDiscount = new Discount(discountCode, productId, discountDB);
		
		expect(discountDB.checkDiscountValid(newDiscount)).andReturn(true).times(1);
		
		replayAll();
		try {
			boolean added = cart.addDiscount(newDiscount);
			fail("Expected: throw Cart.MissingProductException; instead discount was added");
		} catch (Discount.BadDiscountCodeException e) {
			e.printStackTrace();
			fail("Expected: throw Cart.MissingProductException; Instead: Discount.BadDiscountCodeException was thrown");
		} catch (Cart.MissingProductException e) {
			verifyAll();
			assertFalse(cart.getDiscounts().containsKey(productId));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Expected: throw Cart.MissingProductException; Instead: Expection was thrown");
		}
	}

	@Test
	public void testBadDiscountCodeLimit() {
		String discountCode = "gottlieb";
		int productId = 4;
		Discount newDiscount = new Discount(discountCode, productId, discountDB);
		
		expect(discountDB.checkDiscountValid(newDiscount)).andReturn(true).times(1);
		expect(customerDB.verifyBadDiscountCodes(customer)).andReturn(false).times(1);
		
		replayAll();
		try {
			boolean added = cart.addDiscount(newDiscount);
			fail("Expected: throw Exception; instead discount was added");
		} catch (Discount.BadDiscountCodeException e) {
			e.printStackTrace();
			fail("Expected: throw Exception; Instead: Discount.BadDiscountCodeException was thrown");
		} catch (Cart.MissingProductException e) {
			e.printStackTrace();
			fail("Expected: throw Exception; Instead: Cart.MissingProductException was thrown");
		} catch (Exception e) {
			verifyAll();
			assertFalse(cart.getDiscounts().get(productId).contains(newDiscount));
		}
	}

	/* UC3: Modify Cart Quantity */
	@Test
	public void testModifyItemQuatity() {
		CartItem item = items.get(0);
		// old quantity is 12
		int newQuantity = 14;
		int inStock = 52;
		expect(productDB.getStockByProductId(item.getProductId())).andReturn(inStock).times(2);
		expect(cartDB.updateItemQuantityInCart(item, newQuantity, cart)).andReturn(true).times(1);
		
		replayAll();
		try {
			boolean success = cart.handleModifyItemQuantity(item, newQuantity);
			verifyAll();
			assertTrue(success);
			assertEquals(newQuantity, item.getQuantity());
		} catch (OutOfStockException e) {
			e.printStackTrace();
			fail("Expected: successful modification of item quantity");
		}
	}

	@Test
	public void testRemoveItem() {
		CartItem item = items.get(0);
		// old quantity is 12
		int newQuantity = 0;
		int inStock = 52;
		expect(productDB.getStockByProductId(item.getProductId())).andReturn(inStock).times(2);
		expect(cartDB.updateItemQuantityInCart(item, newQuantity, cart)).andReturn(true).times(1);
		
		cartDB.removeItemFromCart(item, cart);
		expectLastCall().times(1);
		
		// check removal of discounts relating to the item
		cartDB.removeDiscountFromCart(discounts.get("123"), cart);
		expectLastCall().times(1);
		cartDB.removeDiscountFromCart(discounts.get("456"), cart);
		expectLastCall().times(1);
		
		replayAll();
		try {
			boolean success = cart.handleModifyItemQuantity(item, newQuantity);
			verifyAll();
			assertTrue(success);
			assertEquals(newQuantity, item.getQuantity());
			assertFalse(cart.getContents().contains(item));
			assertFalse(cart.getDiscounts().containsKey(item.getProductId()));
		} catch (OutOfStockException e) {
			e.printStackTrace();
			fail("Expected: successful modification of item quantity");
		}
	}

	@Test
	public void testQuantityExceedsStock() {
		CartItem item = items.get(0);
		// old quantity is 12
		int newQuantity = 14;
		int inStock = 13;
		expect(productDB.getStockByProductId(item.getProductId())).andReturn(inStock).times(2);
		
		replayAll();
		try {
			boolean success = cart.handleModifyItemQuantity(item, newQuantity);
			fail("Expected: successful modification of item quantity");
		} catch (OutOfStockException e) {
			verifyAll();
		}
	}
}
