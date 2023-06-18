package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Scanner;

import vo.Event;
import vo.Inventory;
import vo.Order;
import vo.Product;
import vo.Sale;

public class Main {
	private Scanner scanner = new Scanner(System.in);
	private Connection conn;

	public Main() {
		String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		String url = "jdbc:sqlserver://localhost:1433;" + "databaseName=bjh;encrypt=true;trustServerCertificate=true";
		String user = "bjh";
		String password = "1004";
		try {
			// JDBC Driver 등록
			Class.forName(driver);
			// 연결하기
			conn = DriverManager.getConnection(url, user, password);

			System.out.println("연동 성공");

		} catch (Exception e) {
			e.printStackTrace();
			exit();
		}
	}

	// 메인 메뉴
	public void mainMenu() {
		System.out.println();
		System.out.println("메인 메뉴: 1.재고관리 | 2.주문관리 | 3.판매관리 | 4.관리자설정 | 5.Exit");
		System.out.print("메뉴 선택: ");
		String menuNo = scanner.nextLine();
		System.out.println();

		switch (menuNo) {
		case "1":
			inventoryMenu();
		case "2":
			orderMenu();
		case "3":
			saleMenu();
		case "4":
			adminLogin();
		case "5":
			exit();
		default:
			System.out.println("잘못된 메뉴 선택입니다.");
			mainMenu();
		}
	}

	// 재고 전체 내역
	public void inventoryList() {
		System.out.println();
		System.out.println("[재고 현황]");
		System.out.println("-------------------------------------------------------");
		System.out.printf("%-12s%-12s%-12s%-16s\n", "재고ID", "제품ID", "제품명", "수량");
		System.out.println("-------------------------------------------------------");
		try {
			String sql = "SELECT i.inventory_id, i.product_id, i.inventory_quantity, p.product_name"
					+ " FROM tb_inventory AS i INNER JOIN tb_product AS p ON i.product_id = p.product_id;";
			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Inventory inventory = new Inventory();
				Product product = new Product();
				inventory.setInventoryId(rs.getInt("inventory_id"));
				inventory.setProductId(rs.getInt("product_id"));
				product.setProductName(rs.getString("product_name"));
				inventory.setQuantity(rs.getInt("inventory_quantity"));
				System.out.printf("%3d%13d%15s%10d\n", inventory.getInventoryId(), inventory.getProductId(),
						product.getProductName(), inventory.getQuantity());
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			exit();
		}
	}

	// 재고 관리 메뉴
	public void inventoryMenu() {
		Inventory inventory = new Inventory();
		inventoryList();
		System.out.println();
		System.out.println("메인 메뉴: 1.재고등록 | 2.재고상세보기 | 3.부족한 재고현황 | 4.메인메뉴");
		System.out.print("메뉴 선택: ");
		String menuNo = scanner.nextLine();
		System.out.println();

		switch (menuNo) {
		case "1":
			insertInventory();
		case "2":
			inventoryOne(inventory.getInventoryId());
		case "3":
			selectScarceInventory();
		case "4":
			mainMenu();
		default:
			System.out.println("잘못된 메뉴 선택입니다.");
			inventoryMenu();
		}
	}

	// 재고 등록
	public void insertInventory() {
		// 입력
		Inventory inventory = new Inventory();
		System.out.println("[새 재고 등록]");
		System.out.print("재고ID: ");
		inventory.setInventoryId(scanner.nextInt());
		scanner.nextLine();
		System.out.print("제품ID: ");
		int productId = scanner.nextInt();
		scanner.nextLine();

		try {
			// 중복된 inventoryId 확인
			String dupliCheckSql = "SELECT * FROM tb_inventory WHERE inventory_id = ?;";
			PreparedStatement duplicateCheckStmt = conn.prepareStatement(dupliCheckSql);
			duplicateCheckStmt.setInt(1, inventory.getInventoryId());
			ResultSet duplicateCheckRs = duplicateCheckStmt.executeQuery();

			if (duplicateCheckRs.next()) {
				System.out.println("해당 재고ID가 이미 존재합니다. 재고 등록을 취소합니다.");
			} else {

				// 제품 존재 여부 확인
				String productCheckSql = "SELECT * FROM tb_product WHERE product_id = ?;";
				PreparedStatement productCheckStmt = conn.prepareStatement(productCheckSql);
				productCheckStmt.setInt(1, productId);
				ResultSet productCheckRs = productCheckStmt.executeQuery();

				if (productCheckRs.next()) {
					// 보조 메뉴 출력
					System.out.println("-------------------------------------------------------");
					System.out.println("보조 메뉴: 1.입력 | 2.취소");
					System.out.print("메뉴 선택: ");
					String selectNo = scanner.nextLine();

					if (selectNo.equals("1")) {
						try {
							// 제품 등록
							String sql = "INSERT INTO tb_inventory"
									+ " (inventory_id, product_id, inventory_quantity, min_stock, max_stock) "
									+ " VALUES (?, ?, 0, 10, 100);";
							PreparedStatement stmt = conn.prepareStatement(sql);
							stmt.setInt(1, inventory.getInventoryId());
							stmt.setInt(2, productId);
							stmt.executeUpdate();
							stmt.close();
						} catch (Exception e) {
							e.printStackTrace();
							exit();
						}
					}
				} else {
					System.out.println("해당 제품이 존재하지 않습니다. 재고 등록을 취소합니다.");
				}
				productCheckRs.close();
				productCheckStmt.close();
			}
			duplicateCheckRs.close();
			duplicateCheckStmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			exit();
		}
		inventoryMenu();
	}

	// 재고 상세보기
	public void inventoryOne(int inventoryId) {
		System.out.print("재고 선택: ");
		inventoryId = Integer.parseInt(scanner.nextLine());

		try {
			String sql = "SELECT i.inventory_id, i.product_id, i.inventory_quantity, p.product_name"
					+ " FROM tb_Inventory AS i INNER JOIN tb_product AS p ON i.product_id = p.product_id WHERE i.inventory_id = ?;";

			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, inventoryId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				Inventory inventory = new Inventory();
				Product product = new Product();
				inventory.setInventoryId(inventoryId);
				inventory.setProductId(rs.getInt("product_id"));
				inventory.setQuantity(rs.getInt("inventory_quantity"));
				product.setProductName(rs.getString("product_name"));

				System.out.println("################");
				System.out.println("재고ID: " + inventory.getInventoryId());
				System.out.println("제품ID: " + inventory.getProductId());
				System.out.println("재품명: " + product.getProductName());
				System.out.println("수량: " + inventory.getQuantity());
				System.out.println("################");

				System.out.println();
				System.out.println("메인 메뉴: 1.수정 | 2.삭제 | 3.재고메뉴");
				System.out.print("메뉴 선택: ");

				String menuNo = scanner.nextLine();
				System.out.println();

				switch (menuNo) {
				case "1":
					updateInventory(inventoryId);
				case "2":
					deleteInventory(inventoryId);
				case "3":
					inventoryMenu();
				default:
					System.out.println("잘못된 메뉴 선택입니다.");
					inventoryMenu();
				}
			} else {
				System.out.println("해당 재고가 존재하지 않습니다.");
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			exit();
		}
		inventoryMenu();
	}

	// 재고 수정(수량)
	public void updateInventory(int inventoryId) {
		Inventory inventory = new Inventory();
		System.out.println("[수정 내용 입력]");
		System.out.print("수량: ");
		inventory.setQuantity(scanner.nextInt());
		scanner.nextLine();

		// 보조 메뉴 출력
		System.out.println("------------------------------------------------------");
		System.out.println("보조 메뉴: 1.수정 | 2.취소");
		System.out.print("메뉴 선택: ");
		String selectNo = scanner.nextLine();
		if (selectNo.equals("1")) {
			try {
				String sql = "UPDATE tb_inventory SET inventory_quantity=? WHERE inventory_id=?";
				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setInt(1, inventory.getQuantity());
				stmt.setInt(2, inventoryId);
				stmt.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
				exit();
			}
		}
		inventoryMenu();
	}

	// 재고 삭제
	public void deleteInventory(int inventoryId) {
		System.out.println("[재고 삭제]");
		System.out.println("정말 삭제하시겠습니까?");
		System.out.println("보조 메뉴: 1.삭제 | 2.취소");
		System.out.print("메뉴 선택: ");
		String selectNo = scanner.nextLine();
		if (selectNo.equals("1")) {
			try {
				String sql = "DELETE FROM tb_inventory WHERE inventory_id=?;";
				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setInt(1, inventoryId);
				stmt.executeUpdate();
				stmt.close();
			} catch (Exception e) {
				e.printStackTrace();
				exit();
			}
		}
		inventoryMenu();
	}

	// 부족한 재고 출력
	public void selectScarceInventory() {
		System.out.println();
		System.out.println("[부족한 재고 현황]");
		System.out.println("-------------------------------------------------------");
		System.out.printf("%-12s%-12s%-12s%-16s\n", "재고ID", "제품ID", "제품명", "수량");
		System.out.println("-------------------------------------------------------");
		try {
			String sql = "SELECT i.inventory_id, i.product_id, i.inventory_quantity, p.product_name"
					+ " FROM tb_Inventory AS i INNER JOIN tb_product AS p ON i.product_id = p.product_id"
					+ " WHERE inventory_quantity < min_stock;";
			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Inventory inventory = new Inventory();
				Product product = new Product();
				inventory.setInventoryId(rs.getInt("inventory_id"));
				inventory.setProductId(rs.getInt("product_id"));
				product.setProductName(rs.getString("product_name"));
				inventory.setQuantity(rs.getInt("inventory_quantity"));
				System.out.printf("%3d%13d%15s%10d\n", inventory.getInventoryId(), inventory.getProductId(),
						product.getProductName(), inventory.getQuantity());
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			exit();
		}
		System.out.println("-------------------------------------------------------");
		System.out.println("보조 메뉴: 1.주문 | 2.취소");
		System.out.print("메뉴 선택: ");
		String selectNo = scanner.nextLine();
		System.out.println();

		switch (selectNo) {
		case "1":
			insertOrder();
		case "2":
			inventoryMenu();
		default:
			System.out.println("잘못된 메뉴 선택입니다.");
			inventoryMenu();
		}
	}

	// 주문 전체 내역
	public void orderList() {
		System.out.println();
		System.out.println("[주문 현황]");
		System.out.println("---------------------------------------------------------------------------");
		System.out.printf("%-12s%-12s%-12s%-16s%-12s\n", "주문ID", "제품ID", "제품명", "주문수량", "주문일");
		System.out.println("---------------------------------------------------------------------------");
		try {
			String sql = "SELECT o.order_id, o.product_id, o.order_quantity,o.order_date, o.order_check, p.product_name"
					+ " FROM tb_order AS o INNER JOIN tb_product AS p ON o.product_id = p.product_id;";
			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Order order = new Order();
				Product product = new Product();
				order.setOrderId(rs.getInt("order_id"));
				order.setProductId(rs.getInt("product_id"));
				product.setProductName(rs.getString("product_name"));
				order.setOrderQuantity(rs.getInt("order_quantity"));
				order.setOrderDate(rs.getString("order_date"));

				if (rs.getInt("order_check") == 1) {
					// 파란색으로 출력
					System.out.print("\u001B[34m"); // ANSI escape code for blue color
				}

				System.out.printf("%3d%13d%15s%12d%21s\n", order.getOrderId(), order.getProductId(),
						product.getProductName(), order.getOrderQuantity(), order.getOrderDate());

				if (rs.getInt("order_check") == 1) {
					// 리셋하여 기본색으로 변경
					System.out.print("\u001B[0m"); // ANSI escape code for resetting color
				}
			}

			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			exit();
		}
	}

	// 주문 관리 메뉴
	public void orderMenu() {
		Order order = new Order();
		orderList();
		System.out.println();
		System.out.println("메인 메뉴: 1.주문등록 | 2.주문상세보기 | 3.메인메뉴");
		System.out.print("메뉴 선택: ");
		String menuNo = scanner.nextLine();
		System.out.println();

		switch (menuNo) {
		case "1":
			insertOrder();
		case "2":
			orderOne(order.getOrderId());
		case "3":
			mainMenu();
		default:
			System.out.println("잘못된 메뉴 선택입니다.");
			orderMenu();
		}
	}

	// 주문 등록
	public void insertOrder() {
		// 입력
		Order order = new Order();
		Product product = new Product();
		System.out.println("[새 주문 등록]");
		System.out.print("제품ID: ");
		int productId = scanner.nextInt();
		scanner.nextLine();
		System.out.print("주문 수량: ");
		order.setOrderQuantity(scanner.nextInt());
		scanner.nextLine();

		try {
			// 제품 존재 여부 확인
			String productCheckSql = "SELECT * FROM tb_inventory WHERE product_id = ?;";
			PreparedStatement productCheckStmt = conn.prepareStatement(productCheckSql);
			productCheckStmt.setInt(1, productId);
			ResultSet productCheckRs = productCheckStmt.executeQuery();

			if (productCheckRs.next()) {

				// 보조 메뉴 출력
				System.out.println("-------------------------------------------------------");
				System.out.println("보조 메뉴: 1.입력 | 2.취소");
				System.out.print("메뉴 선택: ");
				String selectNo = scanner.nextLine();

				if (selectNo.equals("1")) {
					try {
						// 제품 등록
						String sql = "INSERT INTO tb_order" + " (product_id, order_date, order_quantity, order_check)"
								+ " VALUES (?, GETDATE(), ?, 0);";

						PreparedStatement stmt = conn.prepareStatement(sql);
						stmt.setInt(1, productId);
						stmt.setInt(2, order.getOrderQuantity());
						stmt.executeUpdate();
						stmt.close();
						System.out.println("주문이 등록되었습니다.");
					} catch (Exception e) {
						e.printStackTrace();
						exit();
					}
				}
			} else {
				System.out.println("해당 제품은 재고에 없으므로 주문을 등록할 수 없습니다.");
			}
			productCheckRs.close();
			productCheckStmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			exit();
		}
		orderMenu();
	}

	// 주문 상세보기
	public void orderOne(int orderId) {
		System.out.print("주문 선택: ");
		orderId = Integer.parseInt(scanner.nextLine());

		try {
			String sql = "SELECT o.order_id, o.product_id, o.order_quantity, o.order_date, p.product_name"
					+ " FROM tb_order AS o INNER JOIN tb_product AS p ON o.product_id = p.product_id WHERE o.order_id = ?;";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, orderId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				Order order = new Order();
				Product product = new Product();
				order.setOrderId(rs.getInt("order_id"));
				order.setProductId(rs.getInt("product_id"));
				order.setOrderQuantity(rs.getInt("order_quantity"));
				order.setOrderDate(rs.getString("order_date"));
				product.setProductName(rs.getString("product_name"));

				System.out.println("################");
				System.out.println("주문ID: " + orderId);
				System.out.println("제품ID: " + order.getProductId());
				System.out.println("재품명: " + product.getProductName());
				System.out.println("수량: " + order.getOrderQuantity());
				System.out.println("주문일: " + order.getOrderDate());
				System.out.println("################");

				System.out.println();
				System.out.println("메인 메뉴: 1.확인 | 2.수정 | 3.삭제 | 4.주문메뉴");
				System.out.print("메뉴 선택: ");

				String menuNo = scanner.nextLine();
				System.out.println();

				switch (menuNo) {
				case "1":
					orderCheck(orderId);
				case "2":
					updateOrder(orderId);
				case "3":
					deleteOrder(orderId);
				case "4":
					orderMenu();
				default:
					System.out.println("잘못된 메뉴 선택입니다.");
					orderMenu();
				}
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			exit();
		}
		orderMenu();
	}

	// 주문 확인
	public void orderCheck(int orderId) {
		try {
			// 주문 정보 가져오기
			String getOrderSql = "SELECT product_id, order_quantity, order_check FROM tb_order WHERE order_id = ?;";
			PreparedStatement getOrderStmt = conn.prepareStatement(getOrderSql);
			getOrderStmt.setInt(1, orderId);
			ResultSet getOrderRs = getOrderStmt.executeQuery();

			if (getOrderRs.next()) {
				int productId = getOrderRs.getInt("product_id");
				int orderQuantity = getOrderRs.getInt("order_quantity");
				int orderCheck = getOrderRs.getInt("order_check");

				if (orderCheck == 0) {
					// 재고 수정
					String updateInventorySql = "UPDATE tb_inventory SET inventory_quantity = inventory_quantity + ? WHERE product_id = ?;";
					PreparedStatement updateInventoryStmt = conn.prepareStatement(updateInventorySql);
					updateInventoryStmt.setInt(1, orderQuantity);
					updateInventoryStmt.setInt(2, productId);
					updateInventoryStmt.executeUpdate();

					// 주문 확인 상태 변경
					String updateOrderCheckSql = "UPDATE tb_order SET order_check = 1 WHERE order_id = ?;";
					PreparedStatement updateOrderCheckStmt = conn.prepareStatement(updateOrderCheckSql);
					updateOrderCheckStmt.setInt(1, orderId);
					updateOrderCheckStmt.executeUpdate();

					System.out.println("주문 확인이 완료되었습니다.");

				} else {
					System.out.println("해당 주문은 이미 확인되었습니다.");
				}
			} else {
				System.out.println("해당 주문이 존재하지 않습니다.");
			}

			getOrderRs.close();
			getOrderStmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			exit();
		}
		orderMenu();
	}

	// 주문 수정(주문 확인되지 않은 것만)
	public void updateOrder(int orderId) {
		Order order = new Order();
		System.out.println("[수정 내용 입력]");
		System.out.print("수량: ");
		order.setOrderQuantity(scanner.nextInt());
		scanner.nextLine();

		try {
			String orderCheckSql = "SELECT * FROM tb_order WHERE order_id = ? AND order_check = 0;";
			PreparedStatement orderCheckStmt = conn.prepareStatement(orderCheckSql);
			orderCheckStmt.setInt(1, orderId);
			ResultSet orderCheckRs = orderCheckStmt.executeQuery();
			if (orderCheckRs.next()) {
				// 주문 확인되지 않은 경우에만 수정 가능
				System.out.println("------------------------------------------------------");
				System.out.println("보조 메뉴: 1.수정 | 2.취소");
				System.out.print("메뉴 선택: ");
				String selectNo = scanner.nextLine();
				if (selectNo.equals("1")) {
					try {

						String sql = "UPDATE tb_order SET order_quantity=?, order_date=GETDATE() WHERE order_id=?";
						PreparedStatement stmt = conn.prepareStatement(sql);
						stmt.setInt(1, order.getOrderQuantity());
						stmt.setInt(2, orderId);
						stmt.executeUpdate();
					} catch (Exception e) {
						e.printStackTrace();
						exit();
					}
				}
			} else {
				System.out.println("해당 주문은 확인되었으므로 수정할 수 없습니다.");
			}
			orderCheckRs.close();
			orderCheckStmt.close();

		} catch (Exception e) {
			e.printStackTrace();
			exit();
		}
		orderMenu();
	}

	// 주문 삭제(주문 확인되지 않은 것만)
	public void deleteOrder(int orderId) {
		try {
			String orderCheckSql = "SELECT * FROM tb_order WHERE order_id = ? AND order_check = 0;";
			PreparedStatement orderCheckStmt = conn.prepareStatement(orderCheckSql);
			orderCheckStmt.setInt(1, orderId);
			ResultSet orderCheckRs = orderCheckStmt.executeQuery();
			if (orderCheckRs.next()) {
				// 주문 확인되지 않은 경우에만 수정 가능
				System.out.println("[주문 삭제]");
				System.out.println("정말 삭제하시겠습니까?");
				System.out.println("보조 메뉴: 1.삭제 | 2.취소");
				System.out.print("메뉴 선택: ");
				String selectNo = scanner.nextLine();
				if (selectNo.equals("1")) {
					try {
						String sql = "DELETE FROM tb_order WHERE order_id=?;";
						PreparedStatement stmt = conn.prepareStatement(sql);
						stmt.setInt(1, orderId);
						stmt.executeUpdate();
						stmt.close();
					} catch (Exception e) {
						e.printStackTrace();
						exit();
					}
				}
			} else {
				System.out.println("해당 주문은 확인되었으므로 삭제할 수 없습니다.");
			}
			orderCheckRs.close();
			orderCheckStmt.close();

		} catch (Exception e) {
			e.printStackTrace();
			exit();
		}
		orderMenu();
	}

	// 판매 전체 내역
	public void saleList() {
		System.out.println();
		System.out.println("[판매 현황]");
		System.out.println("---------------------------------------------------------------------------");
		System.out.printf("%-12s%-12s%-12s%-16s%-12s\n", "판매ID", "제품ID", "제품명", "판매수량", "판매일");
		System.out.println("---------------------------------------------------------------------------");
		try {
			String sql = "SELECT s.sale_id, s.product_id, s.sale_quantity,s.sale_date, p.product_name, s.sale_check"
					+ " FROM tb_sale AS s INNER JOIN tb_product AS p ON s.product_id = p.product_id;";
			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Sale sale = new Sale();
				Product product = new Product();
				sale.setSaleId(rs.getInt("sale_id"));
				sale.setProductId(rs.getInt("product_id"));
				product.setProductName(rs.getString("product_name"));
				sale.setSaleQuantity(rs.getInt("sale_quantity"));
				sale.setSaleDate(rs.getString("sale_date"));
				int saleCheck = rs.getInt("sale_check");

				if (saleCheck == 1) {
					// 빨간색 출력
					System.out.print("\033[31m");
				}

				System.out.printf("%3d%13d%15s%12d%21s\n", sale.getSaleId(), sale.getProductId(),
						product.getProductName(), sale.getSaleQuantity(), sale.getSaleDate());

				if (saleCheck == 1) {
					// 기본 색상으로 변경
					System.out.print("\033[0m");
				}
			}

			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			exit();
		}
	}

	// 판매 관리 메뉴
	public void saleMenu() {
		Sale sale = new Sale();
		saleList();
		System.out.println();
		System.out.println("메인 메뉴: 1.판매등록 | 2.판매상세보기 | 3.메인메뉴");
		System.out.print("메뉴 선택: ");
		String menuNo = scanner.nextLine();
		System.out.println();

		switch (menuNo) {
		case "1":
			insertSale();
		case "2":
			saleOne(sale.getSaleId());
		case "3":
			// ();
		case "4":
			mainMenu();
		default:
			System.out.println("잘못된 메뉴 선택입니다.");
			saleMenu();
		}
	}

	// 판매등록
	public void insertSale() {
		Sale sale = new Sale();
		System.out.println("[새 판매 등록]");
		System.out.print("제품ID: ");
		int productId = scanner.nextInt();
		scanner.nextLine();
		System.out.print("판매 수량: ");
		int saleQuantity = scanner.nextInt();
		scanner.nextLine();

		try {
			// 재고 확인
			String inventoryCheckSql = "SELECT inventory_quantity FROM tb_inventory WHERE product_id = ?;";
			PreparedStatement inventoryCheckStmt = conn.prepareStatement(inventoryCheckSql);
			inventoryCheckStmt.setInt(1, productId);
			ResultSet inventoryCheckRs = inventoryCheckStmt.executeQuery();

			if (inventoryCheckRs.next()) {
				int inventoryQuantity = inventoryCheckRs.getInt("inventory_quantity");

				if (saleQuantity > inventoryQuantity) {
					System.out.println("재고 수량보다 판매 수량이 더 큽니다.");
					inventoryCheckRs.close();
					inventoryCheckStmt.close();
					saleMenu(); // 판매 선택 메뉴로 돌아감
					return;
				}

				// 보조 메뉴 출력
				System.out.println("-------------------------------------------------------");
				System.out.println("보조 메뉴: 1.등록 | 2.취소");
				System.out.print("메뉴 선택: ");
				String selectNo = scanner.nextLine();

				if (selectNo.equals("1")) {
					// 판매 등록
					String insertSaleSql = "INSERT INTO tb_sale (product_id, sale_date, sale_quantity, sale_check) VALUES (?, GETDATE(), ?, 0);";
					PreparedStatement insertSaleStmt = conn.prepareStatement(insertSaleSql);
					insertSaleStmt.setInt(1, productId);
					insertSaleStmt.setInt(2, saleQuantity);
					insertSaleStmt.executeUpdate();
					insertSaleStmt.close();

					System.out.println("판매가 등록되었습니다.");

					// 재고 업데이트
					String updateInventorySql = "UPDATE tb_inventory SET inventory_quantity = inventory_quantity - ? WHERE product_id = ?;";
					PreparedStatement updateInventoryStmt = conn.prepareStatement(updateInventorySql);
					updateInventoryStmt.setInt(1, saleQuantity);
					updateInventoryStmt.setInt(2, productId);
					updateInventoryStmt.executeUpdate();
					updateInventoryStmt.close();
				}
			} else {
				System.out.println("해당 제품의 재고가 존재하지 않습니다.");
			}

			inventoryCheckRs.close();
			inventoryCheckStmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			exit();
		}

		saleMenu();
	}

	// 판매 상세보기
	public void saleOne(int saleId) {
		System.out.print("판매 선택: ");
		saleId = Integer.parseInt(scanner.nextLine());

		try {
			String sql = "SELECT s.sale_id, s.product_id, s.sale_quantity, s.sale_date, s.sale_check, p.product_name"
					+ " FROM tb_sale AS s INNER JOIN tb_product AS p ON s.product_id = p.product_id WHERE s.sale_id = ?;";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, saleId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				Sale sale = new Sale();
				Product product = new Product();
				sale.setSaleId(rs.getInt("sale_id"));
				sale.setProductId(rs.getInt("product_id"));
				sale.setSaleQuantity(rs.getInt("sale_quantity"));
				sale.setSaleDate(rs.getString("sale_date"));
				product.setProductName(rs.getString("product_name"));

				System.out.println("#############");
				System.out.println("판매ID: " + sale.getSaleId());
				System.out.println("제품ID: " + sale.getProductId());
				System.out.println("제품명: " + product.getProductName());
				System.out.println("수량: " + sale.getSaleQuantity());
				System.out.println("판매일: " + sale.getSaleDate());
				System.out.println("#############");

				System.out.println();
				System.out.println("메인 메뉴: 1.확인 | 2.수정 | 3.삭제 | 4.판매메뉴");
				System.out.print("메뉴 선택: ");

				String menuNo = scanner.nextLine();
				System.out.println();

				switch (menuNo) {
				case "1":
					saleCheck(saleId);
				case "2":
					updateSale(saleId);
				case "3":
					deleteSale(saleId);
				case "4":
					saleMenu();
				default:
					System.out.println("잘못된 메뉴 선택입니다.");
					saleMenu();
				}
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			exit();
		}
	}

	// 판매 확인
	public void saleCheck(int saleId) {
		try {
			String sql = "SELECT * FROM tb_sale WHERE sale_id = ?;";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, saleId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int saleCheck = rs.getInt("sale_check");
				if (saleCheck == 1) {
					System.out.println("이미 확인된 판매입니다.");
				} else {
					// 판매 확인 처리
					String updateSql = "UPDATE tb_sale SET sale_check = 1 WHERE sale_id = ?;";
					PreparedStatement updateStmt = conn.prepareStatement(updateSql);
					updateStmt.setInt(1, saleId);
					updateStmt.executeUpdate();
					System.out.println("판매가 확인되었습니다.");
					updateStmt.close();
				}
			} else {
				System.out.println("해당 판매ID가 존재하지 않습니다.");
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			exit();
		}
		saleMenu();
	}

	// 판매 수정(확인되지 않은 것만)
	public void updateSale(int saleId) {
		try {
			// 확인되지 않은 판매인지 확인
			String checkSql = "SELECT * FROM tb_sale WHERE sale_id = ? AND sale_check = 0;";
			PreparedStatement checkStmt = conn.prepareStatement(checkSql);
			checkStmt.setInt(1, saleId);
			ResultSet checkRs = checkStmt.executeQuery();

			if (checkRs.next()) {
				int productId = checkRs.getInt("product_id");
				int saleQuantity = checkRs.getInt("sale_quantity");

				// 판매 수량 수정 입력
				System.out.println("[판매 수정]");
				System.out.print("판매 수량: ");
				int newSaleQuantity = scanner.nextInt();
				scanner.nextLine();

				// 재고 수량 확인
				String inventorySql = "SELECT * FROM tb_inventory WHERE product_id = ?;";
				PreparedStatement inventoryStmt = conn.prepareStatement(inventorySql);
				inventoryStmt.setInt(1, productId);
				ResultSet inventoryRs = inventoryStmt.executeQuery();

				if (inventoryRs.next()) {
					int inventoryQuantity = inventoryRs.getInt("inventory_quantity");

					if (newSaleQuantity <= inventoryQuantity) {
						int updatedInventoryQuantity = inventoryQuantity + saleQuantity - newSaleQuantity;

						// 판매 수정 처리
						String updateSql = "UPDATE tb_sale SET sale_quantity = ? WHERE sale_id = ?;";
						PreparedStatement updateStmt = conn.prepareStatement(updateSql);
						updateStmt.setInt(1, newSaleQuantity);
						updateStmt.setInt(2, saleId);
						updateStmt.executeUpdate();
						updateStmt.close();

						// 재고 업데이트
						String inventoryUpdateSql = "UPDATE tb_inventory SET inventory_quantity = ? WHERE product_id = ?;";
						PreparedStatement inventoryUpdateStmt = conn.prepareStatement(inventoryUpdateSql);
						inventoryUpdateStmt.setInt(1, updatedInventoryQuantity);
						inventoryUpdateStmt.setInt(2, productId);
						inventoryUpdateStmt.executeUpdate();
						inventoryUpdateStmt.close();

						System.out.println("판매가 수정되었습니다.");
					} else {
						System.out.println("재고 수량보다 판매 수량이 더 큽니다. 판매를 수정할 수 없습니다.");
					}
				} else {
					System.out.println("해당 제품의 재고가 존재하지 않습니다.");
				}

				inventoryRs.close();
				inventoryStmt.close();
			} else {
				System.out.println("해당 판매ID가 존재하지 않거나 이미 확인된 판매입니다.");
			}

			checkRs.close();
			checkStmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			exit();
		}
		saleMenu();
	}

	// 판매삭제(확인되지 않은 것만)
	public void deleteSale(int saleId) {
		try {
			// 확인되지 않은 판매인지 확인
			String checkSql = "SELECT * FROM tb_sale WHERE sale_id = ? AND sale_check = 0;";
			PreparedStatement checkStmt = conn.prepareStatement(checkSql);
			checkStmt.setInt(1, saleId);
			ResultSet checkRs = checkStmt.executeQuery();

			if (checkRs.next()) {
				int productId = checkRs.getInt("product_id");
				int saleQuantity = checkRs.getInt("sale_quantity");

				// 판매 삭제 처리
				String deleteSql = "DELETE FROM tb_sale WHERE sale_id = ?;";
				PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
				deleteStmt.setInt(1, saleId);
				deleteStmt.executeUpdate();
				deleteStmt.close();

				// 재고 업데이트
				String inventoryUpdateSql = "UPDATE tb_inventory SET inventory_quantity = inventory_quantity + ? WHERE product_id = ?;";
				PreparedStatement inventoryUpdateStmt = conn.prepareStatement(inventoryUpdateSql);
				inventoryUpdateStmt.setInt(1, saleQuantity);
				inventoryUpdateStmt.setInt(2, productId);
				inventoryUpdateStmt.executeUpdate();
				inventoryUpdateStmt.close();

				System.out.println("판매가 삭제되었습니다.");
			} else {
				System.out.println("해당 판매ID가 존재하지 않거나 이미 확인된 판매입니다.");
			}

			checkRs.close();
			checkStmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			exit();
		}
		saleMenu();
	}

	// 관리자 로그인
	public void adminLogin() {
		System.out.println("[관리자 로그인]");
		System.out.print("비밀번호: ");
		String password = scanner.nextLine();
		try {
			String sql = "SELECT * FROM tb_admin WHERE password = ?;";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, password);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				// 비밀번호 일치, 관리자 메뉴 진입
				adminMenu();
			} else {
				System.out.println("비밀번호가 일치하지 않습니다.");
			}

			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			exit();
		}
		mainMenu();
	}

	// 관리자 메뉴
	public void adminMenu() {
		System.out.println("[관리자 메뉴]");
		System.out.println("메인 메뉴: 1.이벤트 | 2.제품조회 | 3. 매출 | 4.비밀번호관리(관리자) | 5. 메인메뉴");
		System.out.print("메뉴 선택: ");
		String menuNo = scanner.nextLine();
		System.out.println();

		switch (menuNo) {
		case "1":
			eventMenu();
		case "2":
			selectMenu();
		case "3":
			salesByMonth();
		case "4":
			updateAdminPassword();
		case "5":
			mainMenu();
		default:
			System.out.println("잘못된 메뉴 선택입니다.");
			adminMenu();
		}
	}

	public void eventList() {
		System.out.println();
		System.out.println("[이벤트상품 목록]");
		System.out.println(
				"--------------------------------------------------------------------------------------------------------------------");
		System.out.printf("%10s%9s%17s%17s%17s%15s%15s\n", "이벤트ID", "제품명", "이벤트명", "이벤트유형", "할인율", "시작일", "종료일");
		System.out.println(
				"--------------------------------------------------------------------------------------------------------------------");

		try {
			String sql = "SELECT e.event_id, p.product_name, e.event_name, e.event_type, e.discount_rate, e.start_date, e.end_date"
					+ " FROM tb_event AS e" + " INNER JOIN tb_product AS p ON e.product_id = p.product_id";
			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				int eventId = rs.getInt("event_id");
				String productName = rs.getString("product_name");
				String eventName = rs.getString("event_name");
				String eventType = rs.getString("event_type");
				double discountRate = rs.getDouble("discount_rate");
				String startDate = rs.getString("start_date");
				String endDate = rs.getString("end_date");

				System.out.printf("%9s%13s%20s%18s%17s%20s%16s\n", eventId, productName, eventName, eventType,
						discountRate, startDate, endDate);
			}

			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			exit();
		}
	}

	// 이벤트 메뉴
	public void eventMenu() {
		eventList();
		System.out.println();
		System.out.println("메인 메뉴: 1.이벤트등록 | 2.이벤트수정 | 3.이벤트삭제 | 4.관리자메뉴");
		System.out.print("메뉴 선택: ");
		String menuNo = scanner.nextLine();
		System.out.println();

		switch (menuNo) {
		case "1":
			insertEvent();
		case "2":
			updateEvent();
		case "3":
			deleteEvent();
		case "4":
			adminMenu();
		default:
			System.out.println("잘못된 메뉴 선택입니다.");
			eventMenu();
		}
	}

	// 이벤트 등록
	public void insertEvent() {
		System.out.println("[이벤트 등록]");

		Event event = new Event();

		// 이벤트명 입력
		System.out.print("이벤트명: ");
		event.setEventName(scanner.nextLine());

		// 이벤트유형 입력
		System.out.print("이벤트유형: ");
		event.setEventType(scanner.nextLine());

		// 할인율 입력
		System.out.print("할인율: ");
		event.setDiscountRate(scanner.nextDouble());
		scanner.nextLine(); // 버퍼 비우기

		// 시작일 입력
		System.out.print("시작일 (YYYY-MM-DD): ");
		event.setStartDate(scanner.nextLine());

		// 종료일 입력
		System.out.print("종료일 (YYYY-MM-DD): ");
		event.setEndDate(scanner.nextLine());

		// 제품 ID 입력
		System.out.print("제품 ID: ");
		int productId = scanner.nextInt();
		scanner.nextLine(); // 버퍼 비우기

		try {
			// 제품 ID 유효성 검사
			String selectProductSql = "SELECT * FROM tb_product WHERE product_id = ?";
			PreparedStatement selectProductStmt = conn.prepareStatement(selectProductSql);
			selectProductStmt.setInt(1, productId);
			ResultSet productRs = selectProductStmt.executeQuery();

			if (productRs.next()) {
				// 제품 ID가 유효한 경우, 이벤트 테이블에 데이터 삽입
				String insertEventSql = "INSERT INTO tb_event (event_name, event_type, discount_rate, start_date, end_date, product_id) VALUES (?, ?, ?, ?, ?, ?)";
				PreparedStatement insertEventStmt = conn.prepareStatement(insertEventSql);
				insertEventStmt.setString(1, event.getEventName());
				insertEventStmt.setString(2, event.getEventType());
				insertEventStmt.setDouble(3, event.getDiscountRate());
				insertEventStmt.setString(4, event.getStartDate());
				insertEventStmt.setString(5, event.getEndDate());
				insertEventStmt.setInt(6, productId);
				insertEventStmt.executeUpdate();

				System.out.println("이벤트가 등록되었습니다.");
			} else {
				// 제품 ID가 유효하지 않은 경우
				System.out.println("입력한 제품 ID에 해당하는 제품이 없습니다.");
			}

			productRs.close();
			selectProductStmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			exit();
		}

		eventMenu();
	}

	// 이벤트 수정
	public void updateEvent() {
		System.out.println("[이벤트 수정]");

		// 이벤트 ID 입력
		System.out.print("수정할 이벤트의 ID를 입력하세요: ");
		int eventId = scanner.nextInt();
		scanner.nextLine(); // 버퍼 비우기

		try {
			// 입력한 이벤트 ID로 해당 이벤트 조회
			String selectSql = "SELECT * FROM tb_event WHERE event_id = ?";
			PreparedStatement selectStmt = conn.prepareStatement(selectSql);
			selectStmt.setInt(1, eventId);
			ResultSet rs = selectStmt.executeQuery();

			if (rs.next()) {
				// 이벤트가 존재하는 경우, 수정할 정보 입력
				System.out.print("이벤트명: ");
				String eventName = scanner.nextLine();

				System.out.print("이벤트유형: ");
				String eventType = scanner.nextLine();

				System.out.print("할인율: ");
				double discountRate = scanner.nextDouble();
				scanner.nextLine(); // 버퍼 비우기

				System.out.print("시작일 (YYYY-MM-DD): ");
				String startDate = scanner.nextLine();

				System.out.print("종료일 (YYYY-MM-DD): ");
				String endDate = scanner.nextLine();

				// 이벤트 수정
				String updateSql = "UPDATE tb_event SET event_name = ?, event_type = ?, discount_rate = ?, start_date = ?, end_date = ? WHERE event_id = ?";
				PreparedStatement updateStmt = conn.prepareStatement(updateSql);
				updateStmt.setString(1, eventName);
				updateStmt.setString(2, eventType);
				updateStmt.setDouble(3, discountRate);
				updateStmt.setString(4, startDate);
				updateStmt.setString(5, endDate);
				updateStmt.setInt(6, eventId);
				updateStmt.executeUpdate();

				System.out.println("이벤트가 수정되었습니다.");
			} else {
				// 이벤트가 존재하지 않는 경우
				System.out.println("입력한 이벤트 ID에 해당하는 이벤트가 없습니다.");
			}

			rs.close();
			selectStmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			exit();
		}

		eventMenu();
	}

	// 이벤트 삭제
	public void deleteEvent() {
		System.out.println("[이벤트 삭제]");

		// 이벤트 ID 입력
		System.out.print("삭제할 이벤트의 ID를 입력하세요: ");
		int eventId = scanner.nextInt();
		scanner.nextLine(); // 버퍼 비우기

		try {
			// 입력한 이벤트 ID로 해당 이벤트 조회
			String selectSql = "SELECT * FROM tb_event WHERE event_id = ?";
			PreparedStatement selectStmt = conn.prepareStatement(selectSql);
			selectStmt.setInt(1, eventId);
			ResultSet rs = selectStmt.executeQuery();

			if (rs.next()) {
				// 이벤트가 존재하는 경우, 이벤트 삭제
				String deleteSql = "DELETE FROM tb_event WHERE event_id = ?";
				PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
				deleteStmt.setInt(1, eventId);
				deleteStmt.executeUpdate();

				System.out.println("이벤트가 삭제되었습니다.");
			} else {
				// 이벤트가 존재하지 않는 경우
				System.out.println("입력한 이벤트 ID에 해당하는 이벤트가 없습니다.");
			}

			rs.close();
			selectStmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			exit();
		}

		eventMenu();
	}

	// 조회 메뉴
	public void selectMenu() {
		System.out.println();
		System.out.println("메인 메뉴: 1.best 5 | 2.worst 5 | 3.관리자메뉴");
		System.out.print("메뉴 선택: ");
		String menuNo = scanner.nextLine();
		System.out.println();

		switch (menuNo) {
		case "1":
			best5();
		case "2":
			worst5();
		case "3":
			adminMenu();
		default:
			System.out.println("잘못된 메뉴 선택입니다.");
			selectMenu();
		}
	}

	// best 5
	public void best5() {
		System.out.println("[Best 5]");
		try {
			String query = "SELECT TOP 5 p.product_id, p.product_name, SUM(s.sale_quantity) AS total_sales "
					+ "FROM tb_product p " + "JOIN tb_sale s ON p.product_id = s.product_id "
					+ "WHERE s.sale_check = 1 " + "GROUP BY p.product_id, p.product_name "
					+ "ORDER BY total_sales DESC";

			PreparedStatement statement = conn.prepareStatement(query);
			ResultSet resultSet = statement.executeQuery();

			System.out.println("-------------------------------------------------------");
			System.out.printf("\t%-12s%-12s%-16s\n", "제품ID", "제품명", "총 판매량");
			System.out.println("-------------------------------------------------------");

			while (resultSet.next()) {
				int productId = resultSet.getInt("product_id");
				String productName = resultSet.getString("product_name");
				int totalSales = resultSet.getInt("total_sales");
				System.out.printf("\t%3d%14s%14d\n", productId, productName, totalSales);
			}

			resultSet.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			exit();
		}
		selectMenu();
	}

	// worst 5
	public void worst5() {
		System.out.println("[Worst 5]");
		try {
			String query = "SELECT TOP 5 p.product_id, p.product_name, SUM(s.sale_quantity) AS total_sales"
					+ " FROM tb_product p JOIN tb_sale s ON p.product_id = s.product_id"
					+ " WHERE s.sale_check = 1 GROUP BY p.product_id, p.product_name" + " ORDER BY total_sales ASC;";
			PreparedStatement statement = conn.prepareStatement(query);
			ResultSet resultSet = statement.executeQuery();

			System.out.println("-------------------------------------------------------");
			System.out.printf("\t%-12s%-12s%-16s\n", "제품ID", "제품명", "총 판매량");
			System.out.println("-------------------------------------------------------");

			while (resultSet.next()) {
				int productId = resultSet.getInt("product_id");
				String productName = resultSet.getString("product_name");
				int totalSales = resultSet.getInt("total_sales");
				System.out.printf("\t%3d%14s%14d\n", productId, productName, totalSales);
			}

			resultSet.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			exit();
		}

		selectMenu();
	}

	// 월매출 및 판매 내역 출력
	public void salesByMonth() {
		// 현재 년도와 월을 가져옴
		Date currentDate = new Date();
		java.sql.Date currentSqlDate = new java.sql.Date(currentDate.getTime());
		int year = currentSqlDate.toLocalDate().getYear();
		int month = currentSqlDate.toLocalDate().getMonthValue();

		try {
			// 판매 내역을 가져오기 위한 쿼리
			String sql = "SELECT s.sale_id, p.product_name, s.sale_date, s.sale_quantity, p.price " + "FROM tb_sale s "
					+ "JOIN tb_product p ON s.product_id = p.product_id "
					+ "WHERE YEAR(s.sale_date) = ? AND MONTH(s.sale_date) = ?";
			// 쿼리를 실행하여 결과를 가져옴
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setInt(1, year);
			statement.setInt(2, month);
			ResultSet resultSet = statement.executeQuery();

			// 결과 출력
			if (resultSet.next()) {
				System.out.println("[" + month + "월 판매 내역]");
				System.out.println("--------------------------------------------------------------------");
				System.out.printf("%-10s%-15s%-12s%-15s%-10s\n", "판매ID", "제품명", "판매일자", "판매수량", "판매가격");
				System.out.println("--------------------------------------------------------------------");

				do {
					int saleId = resultSet.getInt("sale_id");
					String productName = resultSet.getString("product_name");
					Date saleDate = resultSet.getDate("sale_date");
					int saleQuantity = resultSet.getInt("sale_quantity");
					int price = resultSet.getInt("price");

					System.out.printf("%3d%13s%19s%10d%18d\n", saleId, productName, saleDate, saleQuantity, price);
				} while (resultSet.next());
			} else {
				System.out.println("이번 달 판매 내역이 없습니다.");
			}

			// 매출을 계산하기 위한 쿼리
			String salesQuery = "SELECT SUM(s.sale_quantity * p.price) AS monthly_sales " + "FROM tb_sale s "
					+ "JOIN tb_product p ON s.product_id = p.product_id "
					+ "WHERE YEAR(s.sale_date) = ? AND MONTH(s.sale_date) = ?";
			// 쿼리를 실행하여 매출 결과를 가져옴
			PreparedStatement salesStatement = conn.prepareStatement(salesQuery);
			salesStatement.setInt(1, year);
			salesStatement.setInt(2, month);
			ResultSet salesResultSet = salesStatement.executeQuery();

			// 매출 결과 출력
			if (salesResultSet.next()) {
				double monthlySales = salesResultSet.getDouble("monthly_sales");
				DecimalFormat formatter = new DecimalFormat("#,###");
				System.out.println("\n이번 달 매출: " + formatter.format(monthlySales) + "원");
				System.out.println();
			} else {
				System.out.println("이번 달 매출 데이터가 없습니다.");
			}

			salesResultSet.close();
			salesStatement.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		adminMenu();
	}

	// 관리자 비밀번호 수정
	public void updateAdminPassword() {
		try {
			// 관리자 비밀번호 변경을 위한 현재 비밀번호 확인
			System.out.println("현재 비밀번호를 입력하세요: ");
			String currentPassword = scanner.nextLine();

			// 현재 비밀번호가 맞는지 확인
			String checkPasswordQuery = "SELECT COUNT(*) AS count FROM tb_admin WHERE password = ?";
			PreparedStatement checkPasswordStatement = conn.prepareStatement(checkPasswordQuery);
			checkPasswordStatement.setString(1, currentPassword);
			ResultSet checkPasswordResultSet = checkPasswordStatement.executeQuery();
			checkPasswordResultSet.next();
			int count = checkPasswordResultSet.getInt("count");
			if (count == 0) {
				System.out.println("현재 비밀번호가 일치하지 않습니다. 비밀번호 변경을 종료합니다.");
				checkPasswordResultSet.close();
				checkPasswordStatement.close();
				adminMenu();
				return;
			}

			// 새로운 비밀번호 입력
			System.out.println("새로운 비밀번호를 입력하세요: ");
			String newPassword = scanner.nextLine();

			// 관리자 비밀번호 업데이트
			String updatePasswordQuery = "UPDATE tb_admin SET password = ? WHERE id = 1";
			PreparedStatement updatePasswordStatement = conn.prepareStatement(updatePasswordQuery);
			updatePasswordStatement.setString(1, newPassword);
			updatePasswordStatement.executeUpdate();
			System.out.println("비밀번호가 성공적으로 변경되었습니다.");

			checkPasswordResultSet.close();
			checkPasswordStatement.close();
			updatePasswordStatement.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		mainMenu();
	}

	// 실행 종료
	public void exit() {
		System.exit(0);
	}

	public static void main(String[] args) {
		Main m = new Main();
		m.mainMenu();
	}

	// 콘솔창 지우기(대체)
	public static void clearScreen() {
		for (int i = 0; i < 80; i++)
			System.out.println("");
	}
}
