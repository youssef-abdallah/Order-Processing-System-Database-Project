package view;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.EventObject;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import controller.Controller;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1005606185398506511L;
	private Controller controller;
	private LoginPanel loginPanel;
	private SignUpPanel signUpPanel;
	private ManagerPanel managerPanel;
	private PublisherPanel publisherPanel;
	private NewBookPanel newBookPanel;
	private UserPanel userPanel;
	private EditInfoPanel infoPanel;
	private SearchPanel searchPanel;
	private static MainFrame uniqueInstance;
	private static JPanel cards;
	// constants
	private String editInfoName = "EDITUSERINFO";

	private MainFrame() {
		super("Bookstore");
		controller = new Controller();
		loginPanel = new LoginPanel();
		signUpPanel = new SignUpPanel();
		managerPanel = new ManagerPanel();
		publisherPanel = new PublisherPanel();
		newBookPanel = new NewBookPanel();
		userPanel = new UserPanel();
		searchPanel = new SearchPanel();
		cards = new JPanel(new CardLayout());
		loginPanel.setListener(new Listener() {
			@Override
			public void eventOccurred(SwitchEvent e) {
				CardLayout cl = (CardLayout) cards.getLayout();
				cl.show(cards, "SIGNUP");
			}

			@Override
			public void eventOccurred(LoginEvent e) {
				String errorMsg = controller.logIn(e);
				if (errorMsg.equals("NoError")) {
					CardLayout cl = (CardLayout) cards.getLayout();
					if (e.isManager()) {
						cl.show(cards, "MANAGER");
					} else {
						cl.show(cards, "USER");
					}
				} else {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), errorMsg, "Invalid input",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		signUpPanel.setListener(new Listener() {
			@Override
			public void eventOccurred(SignUpEvent e) {
				controller.registerUser(e);
				CardLayout cl = (CardLayout) cards.getLayout();
				cl.show(cards, "LOGIN");
			}
		});
		publisherPanel.setListener(new Listener() {
			@Override
			public void eventOccurred(PublisherEvent e) {
				controller.addPublisher(e);
				CardLayout cl = (CardLayout) cards.getLayout();
				cl.show(cards, "NEWBOOK");
			}
		});

		managerPanel.setListener(new Listener() {
			@Override
			public void eventOccured(EventObject e) {
				if (e.getSource() == managerPanel.getAddBookBtn()) {
					CardLayout cl = (CardLayout) cards.getLayout();
					cl.show(cards, "NEWBOOK");
				} else if (e.getSource() == managerPanel.getSearchBtn()) {
					CardLayout cl = (CardLayout) cards.getLayout();
					cl.show(cards, "SEARCH");
				}
			}
		});
		newBookPanel.setListener(new Listener() {
			@Override
			public void eventOccured(EventObject e) {
				if (e.getSource() == newBookPanel.getAddPublisherBtn()) {
					CardLayout cl = (CardLayout) cards.getLayout();
					cl.show(cards, "PUBLISHER");
				}
			}
			
			@Override
			public void eventOccured(BookEvent e) {
				controller.addBook(e);
			}
		});
		
		searchPanel.setListener(new Listener() {
			@Override
			public void eventOccured(SearchEvent e) {
				controller.search(e);
			}
		});
		userPanel.setListener(new Listener() {
			@Override
			public void eventOccured(EventObject e) {
				if (e.getSource() == userPanel.getEditInfoBtn()) {
					initializeUserInfo();
					CardLayout cl = (CardLayout) cards.getLayout();
					cl.show(cards, editInfoName);
				} else if (e.getSource() == userPanel.getSearchForBookBtn()) {

				} else if (e.getSource() == userPanel.getAddToCartBtn()) {

				} else if (e.getSource() == userPanel.getManageCartBtn()) {

				} else if (e.getSource() == userPanel.getCheckOutCartBtn()) {

				} else if (e.getSource() == userPanel.getLogOutBtn()) {
					CardLayout cl = (CardLayout) cards.getLayout();
					cl.show(cards, "LOGIN");
				}
			}
		});
		controller.connectToDB();
		setMinimumSize(new Dimension(1000, 700));
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		cards.add(loginPanel, "LOGIN");
		cards.add(signUpPanel, "SIGNUP");
		cards.add(managerPanel, "MANAGER");
		cards.add(publisherPanel, "PUBLISHER");
		cards.add(newBookPanel, "NEWBOOK");
		cards.add(userPanel, "USER");
		cards.add(searchPanel, "SEARCH");
		this.add(cards);
	}

	private void initializeUserInfo() {
		infoPanel = new EditInfoPanel();
		infoPanel.setListener(new Listener() {
			@Override
			public void eventOccurred(UpdateDataEvent e) {
				if (!controller.updateUserData(e)) {
					infoPanel.getErrorLabel().setText("Email or username already used.");
				} else {
					CardLayout cl = (CardLayout) cards.getLayout();
					cl.show(cards, "USER");
				}
			}
		});
		cards.add(infoPanel, editInfoName);
	}

	public static synchronized MainFrame getInstance() {
		if (uniqueInstance == null) {
			uniqueInstance = new MainFrame();
		}
		return uniqueInstance;
	}
}
