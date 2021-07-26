/*******************************************************************************
 * Copyright (c) 2021 Stan Verstappen. All Rights Reserved.
 *
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>.
 ******************************************************************************/

package system;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import save_load.Saveable;
import save_load.SaveableSystem;
import assets.Person;
import assets.Transaction;
import system.AssociationSystem.Manipulator;

public class AssociationGUIManager extends GUIManager {

	private SaveableSystem system;

	public AssociationGUIManager(AssociationSystem AS){
		super();
		system = AS;
		startSystem();
	}
	
	private void startSystem(){
		
		AssociationSystem as = (AssociationSystem) system;
		Manipulator manipulator = as.getManipulator();
		membersList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				manipulator.setSelected(membersList.getSelectedValue());
			}
			
		});
		listModel.clear();
		Saveable[] saveables = system.getAllSaveables();
		for(int i = 0; i < saveables.length; i++){
			if(saveables[i] instanceof Person){
				listModel.addElement(((Person)saveables[i]));
			}
			
		}
		membersList.validate();
		membersScrollPane.validate();
		
		tabbedPane.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				if(tabbedPane.getSelectedComponent().equals(transactionsTab)){
					updateTransactionList();
				}
				
			}
		});
		
		
		searchButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Person p = ((AssociationSystem)system).getPerson(searchField.getText(), true);
				for(int i = 0; i < listModel.getSize(); i++){
					if(listModel.get(i)==p){
						membersList.setSelectedIndex(i);
						membersList.ensureIndexIsVisible(i);
						return;
					}
				}
			}
		});
		
		searchField.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode()==KeyEvent.VK_ENTER) {
					searchButton.doClick();
				}
			}
			public void keyReleased(KeyEvent arg0) {}
			public void keyTyped(KeyEvent arg0) {}
			
		});
		
		
		addTransactionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Saveable S = as.getManipulator().getSelected();
				String name = S.toString();
				String post = postField.getText();
				double price = Double.parseDouble(String.valueOf(priceField.getValue()));
				String desc = descriptionField.getText();
				system.execute(String.format("T \"%s\" %.2f \"%s\" \"%s\"", name, price, post, desc));
				updateTransactionList();
				//Clear the fields
				priceField.setValue(null);
				postField.setText("");
				descriptionField.setText("");
			}
		});
		
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				system.execute("save");
			}
		});
		
		importListButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				system.execute("list");
			}
		});
		
		batchPrintButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				system.execute("cit \"Contribution and Internal Payments\"");
			}
		});
		
		
		overviewPrintButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				system.execute("cb");
			}
		});
		
		clearTransactionsButton.addActionListener(new ActionListener() {
			 @Override
			public void actionPerformed(ActionEvent e) {
				int confirmation = JOptionPane.showConfirmDialog(jFrame, "Are you sure you want to delete all transactions?");
				if(confirmation == JOptionPane.YES_OPTION) {
					system.execute("ct");
				}
			}
		});
		
	}
	
	protected void updateTransactionList() {
		DefaultListModel<Transaction> defListModel = ((DefaultListModel<Transaction>)transactionList.getModel());
		defListModel.clear();
		Saveable person = membersList.getSelectedValue();
		if(person == null) return;
		if(person instanceof Person){
			for(Transaction T: ((Person)person).getTransactions()){
				defListModel.addElement(T);
			}
		}
	}
	
	
}
