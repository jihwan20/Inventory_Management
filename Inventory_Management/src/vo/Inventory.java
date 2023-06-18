package vo;

import lombok.Data;

@Data
public class Inventory {
	private int inventoryId;
	private int productId;
	private int quantity;
	private int minStock;
	private int maxStock;
}
