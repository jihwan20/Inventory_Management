package vo;

import lombok.Data;

@Data
public class Event {
	private int eventId;
	private String eventName;
	private String eventType;
	private String startDate;
	private String endDate;
	private double discountRate;
	private int productId;
}
