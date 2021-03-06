package model;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import utils.Category;
import utils.MetaData;

public class Database {
	private static User loggedInUser;
	private static Connection connection;
	private String startTransactionQuery = "START TRANSACTION;";
	private String disableAutoCommit = "SET AUTOCOMMIT = 0;";
	private String enableAutoCommit = "SET AUTOCOMMIT = 1;";
	private String commit = "COMMIT;";
	private String rollBack = "ROLLBACK;";

	public void createConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://localhost:3306/BOOKSTORE";
			connection = DriverManager.getConnection(url, "root", "password");
			System.out.println("Connected with DB!");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public void disconnect() {
		if (connection != null) {
			try {
				connection.close();
				System.out.println("Disconnected");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// we can change the return type to boolean to return false in case of existing
	// email
	public void signUpNewUser(User user) {
		if (isDuplicateUser(user.getUserName(), user.getEmail())) {
			System.out.println("This username or email already exists!");
			return;
		}
		try {
			Statement statement = connection.createStatement();
			String hashedPassword = "";
			try {
				hashedPassword = PasswordHashing.getSHA(user.getPassword());
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			String manager = user.isManager() ? "'1'" : "'0'";
			String operation = "INSERT INTO USER VALUES('" + user.getUserName() + "', '" + hashedPassword + "', '"
					+ user.getFirstName() + "', '" + user.getLastName() + "', '" + user.getEmail() + "', '"
					+ user.getPhone() + "', '" + user.getShippingAddress() + "'," + manager + ")";
			statement.execute(operation);
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void signUpSuperUser() {
		User sudo = new User("root", "root", "root", "root@alexu.edu.eg", "password", "FOE - Shatby", "07775000");
		sudo.setManager();
		if (!isDuplicateUser(sudo.getUserName(), sudo.getEmail())) {
			signUpNewUser(sudo);
		}
	}

	public String signIn(String username, String password, boolean isManager) {
		String hashedPassword = "";
		String errorMsg = "NoError";
		try {
			hashedPassword = PasswordHashing.getSHA(password);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		ResultSet rs;
		try {
			Statement statement = connection.createStatement();
			// Made it select all to fill in the logged in user data
			rs = statement.executeQuery("SELECT * FROM USER where username = '" + username + "'");
			if (rs.next()) {
				String tempPass = rs.getString("passwordHash");
				if (tempPass.compareTo(hashedPassword) != 0) {
					return "wrong password! Try again.";
				}
				boolean storedAsManager = rs.getBoolean("IsManager");
				if (isManager && !storedAsManager) {
					return "You are not a manager!";
				}
			} else {
				return "This Username is not registered!";
			}
		} catch (SQLException e) {
			return e.getLocalizedMessage();
		}
		if (errorMsg.equals("NoError")) {
			try {
				fillInUser(rs.getString("userName"), rs.getString("Fname"), rs.getString("Lname"),
						rs.getString("email"), password, rs.getString("shippingAddress"), rs.getString("phone"),
						isManager);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return errorMsg;
	}

	private boolean isDuplicateUser(String userName, String email) {
		boolean isDuplicate = false;
		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("SELECT username, email FROM USER WHERE username = " + "'" + userName
					+ "'" + " OR email = " + "'" + email + "'");
			while (rs.next()) {
				String tempUserName = rs.getString("username");
				String tempEmail = rs.getString("email");
				if (userName.compareTo(tempUserName) == 0 || email.compareTo(tempEmail) == 0) {
					isDuplicate = true;
					break;
				}
			}
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return isDuplicate;
	}

	// methods to add a publisher to the database
	public boolean addNewPublisher(Publisher publisher) {
		if (isDuplicatePublisher(publisher)) {
			return false;
		}
		try {
			Statement statement = connection.createStatement();
			String operation = "INSERT INTO PUBLISHER VALUES('" + publisher.getPublisherName() + "', '"
					+ publisher.getTelephone() + "', '" + publisher.getAddress() + "')";
			statement.execute(operation);
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;

	}

	// This function is used to check if a publisher with the same name already
	// exists
	private boolean isDuplicatePublisher(Publisher publisher) {
		boolean isDuplicate = false;
		String name = publisher.getPublisherName();
		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement
					.executeQuery("SELECT PublisherName FROM PUBLISHER WHERE PublisherName = " + "'" + name + "'");
			while (rs.next()) {
				String tempPublisherName = rs.getString("PublisherName");
				if (name.compareTo(tempPublisherName) == 0) {
					isDuplicate = true;
					break;
				}
			}
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return isDuplicate;
	}

	// This function is to update the user info
	public boolean updateUser(User e) {
		String userName = e.getUserName();
		String firstaName = e.getFirstName();
		String lastName = e.getLastName();
		String password = e.getPassword();
		String email = e.getEmail();
		String phone = e.getPhone();
		String shippingAddress = e.getShippingAddress();
		if ((!loggedInUser.getUserName().equals(userName) && userNameExists(userName))
				|| (!loggedInUser.getEmail().equals(email) && emailExists(email)))
			return false;
		else {
			// updating the user
			try {
				Statement statement = connection.createStatement();
				String hashedPassword = "";
				try {
					hashedPassword = PasswordHashing.getSHA(password);
				} catch (NoSuchAlgorithmException exception) {
					exception.printStackTrace();
				}
				String operation = "UPDATE USER SET " + "userName = '" + userName + "', " + "passwordHash = '"
						+ hashedPassword + "', " + "Fname = '" + firstaName + "', " + "Lname = '" + lastName + "', "
						+ "Email = '" + email + "', " + "Phone = '" + phone + "', " + "shippingAddress = '"
						+ shippingAddress + "' WHERE USERNAME = '" + loggedInUser.getUserName() + "'";
				statement.execute(operation);
				statement.close();
				fillInUser(userName, firstaName, lastName, email, password, shippingAddress, phone,
						loggedInUser.isManager());
			} catch (SQLException exception) {
				exception.printStackTrace();
			}

			return true;
		}
	}

	// Function checks if email only is repeated
	private boolean emailExists(String email) {
		boolean isDuplicate = false;
		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("SELECT email FROM USER WHERE email = " + "'" + email + "'");
			while (rs.next()) {
				String tempEmail = rs.getString("email");
				if (email.compareTo(tempEmail) == 0) {
					isDuplicate = true;
					break;
				}
			}
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isDuplicate;
	}

	public void addNewBook(Book book) {
		try {
			Statement statement = connection.createStatement();
			String operation = "INSERT INTO BOOK VALUES('" + book.getISBN() + "', '" + book.getTitle() + "', '"
					+ book.getPublisherName() + "', '" + book.getPublicationYear() + "', '" + book.getPrice() + "', '"
					+ book.getCategory() + "', '" + book.getThreshold() + "', '" + book.getCopies() + "')";
			statement.execute(operation);
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static boolean userNameExists(String userName) {
		boolean isDuplicate = false;
		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement
					.executeQuery("SELECT username, email FROM USER WHERE username = " + "'" + userName + "'");
			while (rs.next()) {
				String tempUserName = rs.getString("username");
				if (userName.compareTo(tempUserName) == 0) {
					isDuplicate = true;
					break;
				}
			}
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return isDuplicate;
	}

	// Function to fill in the logged in user data
	private void fillInUser(String userName, String firstName, String lastName, String email, String password,
			String shippingAddress, String phone, boolean isManager) {
		Cart cart = new Cart();
		setLoggedInUser(
				new User(userName, firstName, lastName, email, password, shippingAddress, phone, isManager, cart));
	}

	public static User getLoggedInUser() {
		return loggedInUser;
	}

	public static void setLoggedInUser(User loggedInUser) {
		Database.loggedInUser = loggedInUser;
	}

	public boolean isManager() {
		return loggedInUser.isManager();
	}

	public ArrayList<Book> searchBooks(SearchQuery searchQuery) {
		ArrayList<Book> booksList = new ArrayList<Book>();
		try {
			Statement statement = connection.createStatement();
			StringBuilder sb = new StringBuilder();
			String mainOperation, publisherFilter, lowerPriceFilter, upperPriceFilter, fromYearFilter, toYearFilter;
			mainOperation = "SELECT * FROM BOOK WHERE TITLE LIKE '%" + searchQuery.getBookTitle() + "%'"
					+ " AND CATEGORY = '" + searchQuery.getCategory() + "'";
			publisherFilter = " AND PUBLISHERNAME LIKE '%" + searchQuery.getPublisherName() + "%'";
			lowerPriceFilter = " AND PRICE >= '" + searchQuery.getLowerPrice() + "'";
			upperPriceFilter = "AND PRICE <= '" + searchQuery.getUpperPrice() + "'";
			fromYearFilter = " AND PUBLICATIONYEAR >= '" + searchQuery.getFromYear() + "'";
			toYearFilter = " AND PUBLICATIONYEAR <= '" + searchQuery.getToYear() + "'";
			sb.append(mainOperation);
			if (searchQuery.getPublisherName().compareTo("none") != 0) {
				sb.append(publisherFilter);
			}
			if (searchQuery.getLowerPrice() != 0) {
				sb.append(lowerPriceFilter);
			}
			if (searchQuery.getUpperPrice() != 999999) {
				sb.append(upperPriceFilter);
			}
			if (searchQuery.getFromYear() != 1921) {
				sb.append(fromYearFilter);
			}
			if (searchQuery.getToYear() != 2020) {
				sb.append(toYearFilter);
			}

			ResultSet rs = statement.executeQuery(sb.toString());
			while (rs.next()) {
				Book book = new Book();
				String categoryString = rs.getString("category");
				book.setCategory(Enum.valueOf(Category.class, categoryString.toUpperCase()));
				book.setCopies(Integer.parseInt(rs.getString("copies")));
				book.setISBN(Integer.parseInt(rs.getString("ISBN")));
				book.setPrice(Integer.parseInt(rs.getString("price")));
				// book.setPublicationYear(Integer.parseInt(String.valueOf(rs.getDate("PublicationYear"))));
				book.setPublisherName(rs.getString("publisherName"));
				book.setThreshold(Integer.parseInt(rs.getString("Threshold")));
				book.setTitle(rs.getString("title"));
				booksList.add(book);
				// next steps:
				// show results in a table
				// add an option to the user for book selection
			}
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return booksList;
	}

	public void addBookToCart(Book book, int quantity) {
		Cart cart = loggedInUser.getCart();
		cart.addBookToCart(book, quantity);
		cart.showCart();
	}

	public String checkout() {
		String error = "";
		ArrayList<Book> books = loggedInUser.getCart().getSelectedBooks();
		ArrayList<Integer> quantities = loggedInUser.getCart().getQuantities();

		try {
			Statement statement = connection.createStatement();
			// starting a transaction for checkout
			statement.execute(startTransactionQuery);
			// Removing auto increment to be able to perform commit and roll back.
			statement.execute(disableAutoCommit);
			// performing the update queries
			for (int i = 0; i < books.size(); i++) {
				Book currentBook = books.get(i);
				int quantity = quantities.get(i);
				long ISBN = currentBook.getISBN();
				try {
					statement.execute("UPDATE BOOK SET copies = copies - " + quantity + " WHERE ISBN = " + ISBN);
					statement.execute("INSERT INTO SALES(ISBN,UserName,Quantity) VALUES (" + ISBN + ",'"
							+ loggedInUser.getUserName() + "'," + quantity + ")");
				} catch (SQLException ex) {
					ex.printStackTrace();
					error = "Unfortunately transaction failed, " + currentBook.getTitle()
							+ " doesn't have enough copies in stock.";
					// performing roll back when an error occurs
					statement.execute(rollBack);
					statement.execute(enableAutoCommit);
					statement.close();
					return error;
				}
			}
			// performing commit if all updates completed successfully
			statement.execute(commit);
			statement.execute(enableAutoCommit);
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	// Promoting the user
	public boolean promoteUser(String userName) {
		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(
					"SELECT " + MetaData.USER_ISMANAGER + " FROM USER WHERE username = " + "'" + userName + "'");
			while (rs.next()) {
				Boolean isManager = rs.getBoolean(MetaData.USER_ISMANAGER);
				if (isManager) {
					return false;
				}
			}
			statement.close();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		// promoting the user
		try {
			Statement statement = connection.createStatement();
			String operation = "UPDATE USER SET " + MetaData.USER_ISMANAGER + " = true WHERE " + MetaData.USER_NAME
					+ " = '" + userName + "'";
			statement.execute(operation);
			statement.close();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}

		return true;
	}

	public ArrayList<Order> searchOrders() {
		ArrayList<Order> ordersList = new ArrayList<>();
		try {
			Statement statement = connection.createStatement();
			String operation = "SELECT * FROM BOOK_ORDERS";
			ResultSet rs = statement.executeQuery(operation);
			while (rs.next()) {
				Order order = new Order(rs.getInt(MetaData.ORDERS_ISBN), rs.getInt(MetaData.ORDERS_TOTAL));
				ordersList.add(order);
			}
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ordersList;
	}

	public String placeOrder(Order order) {
		String errorMsg = "";
		try {
			Statement statement = connection.createStatement();
			String operation = "INSERT INTO BOOK_ORDERS VALUES('" + order.getISBN() + "', '" + order.getQuantity()
					+ "')";
			statement.execute(operation);
			statement.close();
		} catch (SQLException e) {
			errorMsg = e.getLocalizedMessage();
		}
		return errorMsg;
	}

	public HashMap<Long, String> findOrdersBookTitles() {
		HashMap<Long, String> results = new HashMap<>();
		try {
			Statement statement = connection.createStatement();
			String operation = "SELECT ISBN, Title FROM BOOK WHERE BOOK.ISBN IN (SElECT ISBN FROM BOOK_ORDERS)";
			ResultSet rs = statement.executeQuery(operation);
			while (rs.next()) {
				results.put(rs.getLong(MetaData.BOOK_ISBN), rs.getString(MetaData.BOOK_TITLE));
			}
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return results;
	}

	public void confirmOrders(ArrayList<Long> orders) {
		try {
			Statement statement = connection.createStatement();
			StringBuilder operation = new StringBuilder();
			operation.append("DELETE FROM BOOK_ORDERS WHERE ISBN IN (");
			for (int i = 0; i < orders.size(); i++) {
				if (i != 0) {
					operation.append(", ");
				}
				operation.append(orders.get(i));
			}
			operation.append(")");
			statement.execute(operation.toString());
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static Connection getConnection() {
		return connection;
	}
}
