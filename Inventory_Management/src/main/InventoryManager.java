package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import vo.Inventory;
import vo.Product;

public class InventoryManager {
	private Connection conn;
	private Scanner scanner;
	private Main main;
	
	public InventoryManager(Connection conn, Scanner scanner) {
		this.conn = conn;
		this.scanner = scanner;
	}
	// 재고 전체 내역
	public void inventoryList() {
		System.out.println();
		System.out.println("[재고 현황]");
		System.out.println("-------------------------------------------------------");
		System.out.printf("\t%-12s%-12s%-12s%-16s\n", "재고ID", "제품ID", "제품명", "수량");
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
				System.out.printf("\t%3d%13d%15s%10d\n", inventory.getInventoryId(), inventory.getProductId(),
						product.getProductName(), inventory.getQuantity());
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			main.exit();
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
			main.mainMenu();
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
							main.exit();
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
			main.exit();
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
				}
			} else {
				System.out.println("해당 재고가 존재하지 않습니다.");
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			main.exit();
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
				main.exit();
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
				main.exit();
			}
		}
		inventoryMenu();
	}

	// 부족한 재고 출력
	public void selectScarceInventory() {
		System.out.println();
		System.out.println("[부족한 재고 현황]");
		System.out.println("-------------------------------------------------------");
		System.out.printf("\t%-12s%-12s%-12s%-16s\n", "재고ID", "제품ID", "제품명", "수량");
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
				System.out.printf("\t%3d%13d%15s%10d\n", inventory.getInventoryId(), inventory.getProductId(),
						product.getProductName(), inventory.getQuantity());
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			main.exit();
		}
		System.out.println("-------------------------------------------------------");
		System.out.println("보조 메뉴: 1.주문 | 2.취소");
		System.out.print("메뉴 선택: ");
		String selectNo = scanner.nextLine();
		System.out.println();

		switch (selectNo) {
		case "1":
			main.insertOrder();
		case "2":
			inventoryMenu();
		}
	}
}
