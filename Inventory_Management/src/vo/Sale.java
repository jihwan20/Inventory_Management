package vo;

import lombok.Data;

@Data
public class Sale {
	private int saleId;
	private int productId;
	private String saleDate;
	private int saleQuantity;
	private int saleCheck;
}
