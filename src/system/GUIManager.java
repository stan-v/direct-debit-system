/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import org.eclipse.wb.swing.FocusTraversalOnArray;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import save_load.Saveable;
import assets.Transaction;
import java.awt.Dimension;

public class GUIManager {

	public static final String TITLE = "The Association";

	public JFrame jFrame;

	protected DefaultListModel<Saveable> listModel = new DefaultListModel<Saveable>();
	
	protected JTabbedPane tabbedPane;
	
	protected JPanel memberTab;
	protected JScrollPane membersScrollPane;
	protected JLabel membersListTitle;
	protected JList<Saveable> membersList;
	protected JButton gotoPersonButton;
	protected JTextField searchField;
	protected JButton searchButton;
	protected JButton saveButton;
	protected JButton importListButton;

	protected JPanel transactionsTab;
	protected JScrollPane transactionScrollPane;
	protected JLabel transactionListTitle;
	protected JList<Transaction> transactionList;
	protected JButton addTransactionButton;
	protected JFormattedTextField priceField;
	protected JTextField postField;
	protected JTextField descriptionField;
	protected JButton batchPrintButton;
	protected JButton overviewPrintButton;
	protected JButton clearTransactionsButton;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUIManager window = new GUIManager();
					window.jFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUIManager() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		// Begin Frame
		jFrame = new JFrame();
		jFrame.setBackground(new Color(153, 204, 255));
		jFrame.setForeground(new Color(153, 204, 255));
		jFrame.setFont(new Font("Calibri", Font.PLAIN, 16));
		jFrame.setTitle(TITLE);
		jFrame.setBounds(100, 100, 760, 476);
		jFrame.setLocationRelativeTo(null);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//End Frame
		
		//Begin Tabs
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setFocusCycleRoot(true);
		tabbedPane.setForeground(new Color(0, 0, 0));
		tabbedPane.setBackground(new Color(153, 255, 0));
		tabbedPane.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
		jFrame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		//End Tabs
		
		//Begin Membertab
		memberTab = new JPanel();
		memberTab.setSize(new Dimension(23, 23));
		memberTab.setFocusCycleRoot(true);
		memberTab.setBackground(new Color(153, 204, 255));
		tabbedPane.addTab("Members", null, memberTab, "Tab for members");
		memberTab.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(141px;default)"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(129px;default)"),
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("85px:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("85px:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("max(120dlu;default)"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,}));
		
		membersScrollPane = new JScrollPane();
		memberTab.add(membersScrollPane, "2, 2, 3, 9, default, fill");
		
		membersList = new JList<Saveable>(listModel);
		membersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		membersScrollPane.setViewportView(membersList);
		
		membersListTitle = new JLabel("Leden");
		membersListTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		membersListTitle.setHorizontalAlignment(SwingConstants.CENTER);
		membersScrollPane.setColumnHeaderView(membersListTitle);
		
		searchField = new JTextField();
		searchField.setFont(new Font("Tahoma", Font.PLAIN, 13));
		memberTab.add(searchField, "7, 2, fill, fill");
		searchField.setColumns(10);
		
		gotoPersonButton = new JButton("Show Transactions");
		gotoPersonButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tabbedPane.setSelectedComponent(transactionsTab);
			}
		});
		
		searchButton = new JButton(new String(Character.toChars(0x1F50E)));
		searchButton.setMinimumSize(new Dimension(23, 23));
		searchButton.setMaximumSize(new Dimension(23, 23));
		searchButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
		memberTab.add(searchButton, "9, 2");
		memberTab.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{membersScrollPane, membersListTitle, searchField, searchButton, gotoPersonButton, membersList}));
		
		importListButton = new JButton("Import payment list");
		memberTab.add(importListButton, "7, 6");
		
		clearTransactionsButton = new JButton("Clear all transactions");
		memberTab.add(clearTransactionsButton, "9, 6");
		
		batchPrintButton = new JButton("Print batch");
		memberTab.add(batchPrintButton, "7, 8");
		
		overviewPrintButton = new JButton("Print overview");
		memberTab.add(overviewPrintButton, "7, 10");
		memberTab.add(gotoPersonButton, "2, 12");
		
		saveButton = new JButton("Save");
		memberTab.add(saveButton, "9, 14");
		
		//End Members Tab
		
		//Begin Personal Info Tab
		
		transactionsTab = new JPanel();
		transactionsTab.setFocusCycleRoot(true);
		transactionsTab.setBackground(new Color(153, 204, 255));
		tabbedPane.addTab("Intern Saldo", null, transactionsTab, null);
		transactionsTab.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("77px:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("204px:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("239px:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("172px"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		transactionScrollPane = new JScrollPane();
		transactionsTab.add(transactionScrollPane, "2, 2, 5, 1, fill, fill");
		
		transactionListTitle = new JLabel("Intern Saldo");
		transactionListTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		transactionListTitle.setHorizontalAlignment(SwingConstants.CENTER);
		transactionScrollPane.setColumnHeaderView(transactionListTitle);
		
		transactionList = new JList<Transaction>(new DefaultListModel<Transaction>());
		transactionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		transactionScrollPane.setViewportView(transactionList);
		
		addTransactionButton = new JButton("Add transaction");
		DefaultFormatterFactory currencyFormatter = new DefaultFormatterFactory(
                new NumberFormatter(DecimalFormat.getCurrencyInstance()),
                new NumberFormatter(DecimalFormat.getCurrencyInstance()),
                new NumberFormatter(DecimalFormat.getInstance()));
		
		priceField = new JFormattedTextField(currencyFormatter);
		transactionsTab.add(priceField, "2, 4, fill, default");
		
		postField = new JTextField();
		transactionsTab.add(postField, "4, 4, fill, default");
		
		descriptionField = new JTextField();
		transactionsTab.add(descriptionField, "6, 4, fill, default");
		descriptionField.setColumns(10);
		transactionsTab.add(addTransactionButton, "6, 6");
		
		transactionsTab.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{priceField, postField, descriptionField, addTransactionButton}));
		
		
		
		tabbedPane.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{memberTab, transactionsTab}));
	}
	
}
