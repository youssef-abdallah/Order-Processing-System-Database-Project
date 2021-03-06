package model;

public class User {
	private String userName;
	private String firstName;
	private String lastName;
	private String email;
	private String password;
	private String shippingAddress;
	private String phone;
	private boolean manager;
	private Cart cart;
	
	public User() {
		
	}

	public User(String userName, String firstName, String lastName, String email, String password,
			String shippingAddress, String phone) {
		this.setUserName(userName);
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.password = password;
		this.shippingAddress = shippingAddress;
		this.phone = phone;
		setCart(new Cart());
	}

	public User(String userName, String firstName, String lastName, String email, String password,
			String shippingAddress, String phone, boolean isManager, Cart cart) {
		this.setUserName(userName);
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.password = password;
		this.shippingAddress = shippingAddress;
		this.phone = phone;
		this.manager = isManager;
		this.cart = cart;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public void setManager() {
		this.manager = true;
	}
	
	public boolean isManager() {
		return manager;
	}
	
	public Cart getCart() {
		return cart;
	}

	public void setCart(Cart cart) {
		this.cart = cart;
	}
}
