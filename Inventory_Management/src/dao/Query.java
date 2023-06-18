package dao;

public class Query {
	public static final String INSERT_PRODUCT; // 재품 등록
	public static final String INSERT_INVENTORY; // 재고 등록
	public static final String SELECT_INVENTORY; // 재고 확인
	static {
		// 재품 등록
		INSERT_PRODUCT = "INSERT INTO tb_Product (productId, productName, price) VALUES (?, ?, ?);";
		// 재고 등록
		INSERT_INVENTORY = "INSERT INTO tb_Inventory (inventoryId, productId, quantity, min_stock, max_stock) VALUES (?, ?, 0, 10, 100);";
		// 재고 확인
		SELECT_INVENTORY = "SELECT i.inventoryId, i.productId, i.quantity, p.productName FROM tb_Inventory as i INNER JOIN tb_product as p ON i.productId = p.productId;";
		
	}
}
