/******************************************************************************
*
* Filename:     CartonAuditView.java
* Description:
*
* (c) COPYRIGHT QC Software, LLC 2018 All Rights Reserved
* No part of this copyrighted work may be reproduced, modified, or distributed
* in any form or by any means without the prior written permission of QC
* Software, LLC.
*
* $Id: CartonAuditView.java 1144 2018-03-29 13:29:37Z jas $
* Notes:
*
*****************************************************************************/
package com.qcsoftware.jacent.ui.cartonaudit;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.qcsoftware.jacent.ui.manualorder.OrderModel;
import com.qcsoftware.jacent.ui.manualorder.PickListTableView;
import com.qcsoftware.library.bean.AbstractBeanView;
import com.qcsoftware.library.bean.BeanModelException;
import com.qcsoftware.library.bean.FeedbackMessage;
import com.qcsoftware.library.database.DbTransaction;
import com.qcsoftware.ui.mvc.swing.BeanLabelView;
import com.qcsoftware.ui.mvc.swing.BeanListView;
import com.qcsoftware.ui.mvc.swing.BeanTextView;
import com.qcsoftware.ui.mvc.swing.FeedbackList;
import com.qcsoftware.ui.mvc.swing.MnemonicLabelView;
import com.qcsoftware.ui.util.swing.ExitOnEscape;
import com.qcsoftware.ui.util.swing.SelectAllOnFocus;

import Library.Database.DbConnect;
import Library.Utils.Tracer;
import Packages.OrderMgmt.PickWorkOrders;

/**
 * @author jas
 *
 */
public class CartonAuditView extends AbstractBeanView<CartonAuditController, CartonAuditModel>
    implements ActionListener, KeyListener
{
  @SuppressWarnings("unused")
  private static final String svnid = "$Id: CartonAuditView.java 1144 2018-03-29 13:29:37Z jas $";
  DbConnect                   conn;
  private final TableView     tableView;
  private final FeedbackList  feedbackList;
  private final Container     parent;
  private final JTextField    cartonTextField;
  private final JTextField    stockNumTF;
  private final JLabel        workOrderStatusLabel;
  private final JLabel        invoiceLabel;
  private final JLabel        workOrderLabel;
  private final JButton       reprintDocumentButton;
  private final JButton       selectBtn;
  private final JButton       cancelBtn;
  private final JTable        documentTable;
  private final JDialog       oneUpDialog;

  @SuppressWarnings("serial")
  public CartonAuditView(DbConnect conn, CartonAuditController controller, Container parent) throws SQLException
  {
    super(CartonAuditController.CARTON_AUDIT_VIEW, controller, controller.getCartonAuditModel());
    this.conn = conn;
    this.parent = parent;

    this.tableView = new TableView(controller);
    this.documentTable = new JTable(tableView);
    this.feedbackList = new FeedbackList(new BeanListView<>(controller,
                                                            controller.getFeedback(),
                                                            FeedbackMessage.class));
    ((BeanListView<?, ?>) this.feedbackList.getModel()).setAutoScroll(true);
    controller.registerView(this,
                            tableView,
                            (BeanListView<?, ?>) feedbackList.getModel(),
                            new BeanTextView<>(controller,
                                               controller.getCartonAuditModel(),
                                               CartonAuditModel.MOVEABLE_UNIT_ID,
                                               cartonTextField = new JTextField()),
                            new BeanTextView<>(controller,
                                               controller.getCartonAuditModel(),
                                               CartonAuditModel.STOCK_NUMBER,
                                               stockNumTF = new JTextField()),
                            new BeanLabelView<>(controller,
                                                controller.getCartonAuditModel(),
                                                CartonAuditModel.INVOICE,
                                                invoiceLabel = new JLabel()),
                            new BeanLabelView<>(controller,
                                                controller.getCartonAuditModel(),
                                                CartonAuditModel.WORK_ORDER_ID,
                                                workOrderLabel = new JLabel()),
                            new MnemonicLabelView<>(controller,
                                                    controller.getCartonAuditModel(),
                                                    CartonAuditModel.STATUS,
                                                    workOrderStatusLabel = new JLabel(),
                                                    conn,
                                                    PickWorkOrders.STATUS_TYPE));

    this.cartonTextField.addActionListener(this);
    this.cartonTextField.addFocusListener(new SelectAllOnFocus());

    this.invoiceLabel.addFocusListener(new FocusAdapter()
    {
      public void focusLost(FocusEvent e)
      {
        getController().setInvoiceLabel(invoiceLabel.getText());
      };
    });

    this.workOrderLabel.addFocusListener(new FocusAdapter()
    {
      public void focusLost(FocusEvent e)
      {
        getController().setWorkOrderLabel(workOrderLabel.getText());
      };
    });

    this.workOrderStatusLabel.addFocusListener(new FocusAdapter()
    {
      public void focusLost(FocusEvent e)
      {
        getController().setWorkOrderStatusLabel(workOrderStatusLabel.getText());
      };
    });

    this.reprintDocumentButton = createButton("Reprint");

    documentTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent event)
      {
        if (documentTable.getSelectedRow() >= 0 && event.getValueIsAdjusting())
        {
          String doc = documentTable.getModel().getValueAt(documentTable.getSelectedRow(), 0).toString();
          reprintDocumentButton.setText("REPRINT " + doc.toUpperCase());
          reprintDocumentButton.setEnabled(true);
        }
      }
    });

    this.oneUpDialog = new JDialog();
    oneUpDialog.setTitle("Stock Number");
    ExitOnEscape.from(oneUpDialog);

    this.selectBtn = createButton("Select");
    this.cancelBtn = createButton("Cancel");

    disableAllButtons();
  }

  public JDialog getOneUpDialog()
  {
    return oneUpDialog;
  }

  public JTextField getCartonTextField()
  {
    return cartonTextField;
  }

  public JTextField getStockNumTF()
  {
    return stockNumTF;
  }

  public JLabel getWorkOrderStatusLabel()
  {
    return workOrderStatusLabel;
  }

  public JLabel getInvoiceLabel()
  {
    return invoiceLabel;
  }

  public JLabel getWorkOrderLabel()
  {
    return workOrderLabel;
  }

  public JTable getDocumentTable()
  {
    return documentTable;
  }

  public void populateInvoice(Integer invoice)
  {
    invoiceLabel.setText(invoice != null ? invoice.toString() : "");
  }

  public void populateWorkOrderId(String workOrderId)
  {
    workOrderLabel.setText(workOrderId != null ? workOrderId.toString() : "");
  }

  public JButton getReprintDocumentButton()
  {
    return reprintDocumentButton;
  }

  public JButton getSelectBtn()
  {
    return selectBtn;
  }

  public JButton getCancelBtn()
  {
    return cancelBtn;
  }

  public FeedbackList getFeedbackList()
  {
    return feedbackList;
  }

  private JButton createButton(String text)
  {
    JButton button = new JButton(text);
    button.addKeyListener(this);
    button.addActionListener(this);
    return button;
  }

  public void disableAllButtons()
  {
    reprintDocumentButton.setEnabled(false);
  }

  public void promptForCarton()
  {
    cartonTextField.setEnabled(true);
    cartonTextField.requestFocus();
    cartonTextField.selectAll();
  }

  @Override
  public void modelPropertyChange(PropertyChangeEvent evt)
  {

  }

  @Override
  public void keyPressed(KeyEvent arg0)
  {

  }

  @Override
  public void keyReleased(KeyEvent arg0)
  {

  }

  @Override
  public void keyTyped(KeyEvent arg0)
  {

  }

  @Override
  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == this.cartonTextField)
    {
      getController().lookupMoveableUnit(conn, evt.getActionCommand());
      cartonTextField.requestFocus();
      cartonTextField.selectAll();
    }
    else if (evt.getSource() == this.reprintDocumentButton)
    {
      if (documentTable.getSelectedRow() > 0)
      {
        String doc = documentTable.getValueAt(documentTable.getSelectedRow(), 0).toString();
        if ("1UP".equals(doc))
          this.oneUpDialog.setVisible(true);
        else
        {
          try
          {
            getController().getCartonAuditModel().setDocument(doc);
            getController().printDocument(conn);
          }
          catch (SQLException | BeanModelException e)
          {
            Tracer.err(e);
          }
        }
      }
    }
    else if (evt.getSource() == this.selectBtn)
    {
      String doc = documentTable.getValueAt(documentTable.getSelectedRow(), 0).toString();
      String stockNum = stockNumTF.getText().trim();

      Tracer.out("document " + doc);
      Tracer.out("stockNum " + stockNum);

      try
      {
        this.oneUpDialog.setVisible(false);
        getController().getCartonAuditModel().setDocument(doc);
        getController().getCartonAuditModel().setStockNumber(stockNum);
        getController().printDocument(conn);
      }
      catch (SQLException | BeanModelException e)
      {
        Tracer.err(e);
      }
    }
    else if (evt.getSource() == this.cancelBtn)
    {
      oneUpDialog.setVisible(false);

      stockNumTF.setText("");

      oneUpDialog.dispose();
    }
  }
}
