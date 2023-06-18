package vo;

import lombok.Data;

@Data
public class Order {
	private int orderId;
	private int productId;
	private String orderDate;
	private int orderQuantity;
	private int checkOrder;
}
