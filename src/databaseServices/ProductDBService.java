package databaseServices;

import shoppingCartAPI.*;

public interface ProductDBService {
	// get stock
	public int getStockByProductId(int productId);
}
