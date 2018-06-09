/******************************************************************************
*
* Filename:     CartonAuditPanel.java
* Description:
*
* (c) COPYRIGHT QC Software, LLC 2018 All Rights Reserved
* No part of this copyrighted work may be reproduced, modified, or distributed
* in any form or by any means without the prior written permission of QC
* Software, LLC.
*
* $Id: CartonAuditPanel.java 1144 2018-03-29 13:29:37Z jas $
* Notes:
*
*****************************************************************************/
package com.qcsoftware.jacent.ui.cartonaudit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.metal.MetalButtonUI;
import javax.swing.table.DefaultTableCellRenderer;

import com.qcsoftware.ui.style.GenericTheme;
import com.qcsoftware.ui.style.IColor;
import com.qcsoftware.ui.style.IPallet;
import com.qcsoftware.ui.style.IStyle;
import com.qcsoftware.ui.style.ITheme;
import com.qcsoftware.ui.style.MFont;
import com.qcsoftware.ui.style.MPallet;
import com.qcsoftware.ui.util.swing.SwingStyle;

import Gui.Basic.QMainFrame;
import Library.Database.DbConnect;
import Library.Language.DictionaryManager;
import Library.Language.SimpleDictionary;
import Library.Utils.Tracer;

/**
 * @author jas
 *
 */
public class CartonAuditPanel extends JPanel
{
  @SuppressWarnings("unused")
  private static final String   svnid         = "$Id: CartonAuditPanel.java 1144 2018-03-29 13:29:37Z jas $";
  private CartonAuditController controller;
  private CartonAuditView       view;
  private Map<String, String>   columnNameMap = new HashMap<>();
  private Map<Integer, String>  pickStatusMap = new HashMap<>();

  public CartonAuditPanel(DbConnect conn)
  {
    setSize(new Dimension(1000, 500));
    setPreferredSize(new Dimension(1000, 500));
    setLayout(new GridBagLayout());
    setBackground(getScreenBackground());

    try
    {
      controller = new CartonAuditController(conn);
      view = new CartonAuditView(conn, controller, this);
    }
    catch (SQLException e)
    {
      Tracer.err(e);
    }

    int padding = 16;
    int labelGap = 2;

    SimpleDictionary sd = DictionaryManager.getInstance().getDictionary(DictionaryManager.PROJECT_DICTIONARY);
    if (sd == null)
      sd = new SimpleDictionary();

    columnNameMap.put(ActivityModel.DOCUMENT, sd.get("document"));
    columnNameMap.put(ActivityModel.PRINTCOUNT, sd.get("PrintCount"));
    columnNameMap.put(ActivityModel.OPERATOR, sd.get("operatorId"));
    columnNameMap.put(ActivityModel.PRINTTIME, sd.get("ts"));

    JLabel moveableUnitIdLbl = new JLabel(sd.get("Carton"));
    moveableUnitIdLbl.setForeground(getPropertyLabelColor());
    moveableUnitIdLbl.setFont(getPropertyLabelFont());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.gridy = 0;
    c.gridx = 0;
    c.insets.top = padding;
    c.insets.bottom = labelGap;
    c.insets.right = padding;
    c.insets.left = padding;
    add(moveableUnitIdLbl, c.clone());

    JTextField moveableUnitIdTF = view.getCartonTextField();
    moveableUnitIdTF.setName("cartonTF");
    moveableUnitIdTF.setColumns(10);
    moveableUnitIdTF.setForeground(getPropertyColor());
    moveableUnitIdTF.setFont(getPropertyFont());
    moveableUnitIdTF.setBackground(getScreenBackground());
    moveableUnitIdTF.setBorder(new MatteBorder(0, 0, 1, 0, getPropertyLabelColor()));
    c.gridy = 1;
    c.insets.top = labelGap;
    c.insets.bottom = padding;
    c.ipadx = 150;
    add(moveableUnitIdTF, c.clone());

    JLabel invoiceLabel = new JLabel(sd.get("Invoice"));
    invoiceLabel.setForeground(getPropertyLabelColor());
    invoiceLabel.setFont(getPropertyLabelFont());
    c.gridy = 0;
    c.gridx = 1;
    c.insets.top = padding;
    c.insets.bottom = labelGap;
    c.insets.right = padding;
    c.insets.left = padding;
    c.ipadx = 0;
    add(invoiceLabel, c.clone());
    c.gridy = 1;
    c.insets.top = labelGap;
    c.insets.bottom = padding;
    c.ipadx = 50;
    JLabel invoiceLbl = view.getInvoiceLabel();
    invoiceLbl.setForeground(getPropertyColor());
    invoiceLbl.setFont(getPropertyFont());
    invoiceLbl.setBackground(getScreenBackground());
    add(invoiceLbl, c.clone());
    
    JLabel workOrderLabel = new JLabel(sd.get("WorkOrderId"));
    workOrderLabel.setForeground(getPropertyLabelColor());
    workOrderLabel.setFont(getPropertyLabelFont());
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.gridy = 0;
    c.gridx = 2;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.weightx = 0;
    c.weighty = 0;
    c.insets.top = padding;
    c.insets.bottom = labelGap;
    c.insets.right = padding;
    c.insets.left = padding;
    c.ipadx = 0;
    add(workOrderLabel, c.clone());
    c.gridy = 1;
    c.insets.top = labelGap;
    c.insets.bottom = padding;
    c.ipadx = 50;
    JLabel workOrderLbl = view.getWorkOrderLabel();
    workOrderLbl.setForeground(getPropertyColor());
    workOrderLbl.setFont(getPropertyFont());
    workOrderLbl.setBackground(getScreenBackground());
    add(workOrderLbl, c.clone());

    JLabel orderStatusLbl = view.getWorkOrderStatusLabel();
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.NORTHEAST;
    c.gridy = 0;
    c.gridx = 3;
    c.gridheight = 2;
    c.gridwidth = 1;
    c.weightx = 1;
    c.weighty = 0;
    c.insets.top = padding;
    c.insets.bottom = padding;
    c.insets.right = padding;
    c.insets.left = padding;
    c.ipadx = 0;
    orderStatusLbl.setFont(SwingStyle.of(MFont.DISPLAY_2));
    orderStatusLbl.setForeground(getScreenColor());
    orderStatusLbl.setBackground(getScreenBackground());
    orderStatusLbl.setHorizontalAlignment(SwingConstants.RIGHT);
    orderStatusLbl.setVerticalAlignment(SwingConstants.CENTER);
    add(orderStatusLbl, c.clone());

    JTable docsNeededTable = view.getDocumentTable();
    docsNeededTable.setRowHeight(35);
    docsNeededTable.getRowMargin();
    docsNeededTable.setGridColor(getTableGridColor());
    docsNeededTable.setShowVerticalLines(false);
    docsNeededTable.setFillsViewportHeight(true);
    docsNeededTable.setBackground(getScreenBackground());
    docsNeededTable.getTableHeader()
        .setDefaultRenderer(new HeaderRenderer(getHeaderBackground(),
                                               getHeaderColor(),
                                               getHeaderBorderColor(),
                                               columnNameMap));
    docsNeededTable.getTableHeader().setFont(SwingStyle.of(MFont.TITLE));
    docsNeededTable.setDefaultRenderer(String.class, new CellRenderer(String.class,
                                                                      getScreenBackground(),
                                                                      getScreenColor(),
                                                                      SwingStyle.of(getTheme().getSecondary()
                                                                          .getLighterAccent(0)
                                                                          .getBackground()),
                                                                      SwingStyle.of(getTheme().getSecondary()
                                                                          .getLighterAccent(0)
                                                                          .getColor()),
                                                                      pickStatusMap));
    docsNeededTable.setDefaultRenderer(Integer.class, new CellRenderer(Integer.class,
                                                                       getScreenBackground(),
                                                                       getScreenColor(),
                                                                       SwingStyle.of(getTheme().getSecondary()
                                                                           .getLighterAccent(0)
                                                                           .getBackground()),
                                                                       SwingStyle.of(getTheme().getSecondary()
                                                                           .getLighterAccent(0)
                                                                           .getColor()),
                                                                       pickStatusMap));
    docsNeededTable.setDefaultRenderer(BigDecimal.class, new CellRenderer(BigDecimal.class,
                                                                          getScreenBackground(),
                                                                          getScreenColor(),
                                                                          SwingStyle.of(getTheme().getSecondary()
                                                                              .getLighterAccent(0)
                                                                              .getBackground()),
                                                                          SwingStyle.of(getTheme().getSecondary()
                                                                              .getLighterAccent(0)
                                                                              .getColor()),
                                                                          pickStatusMap));
    docsNeededTable.setDefaultRenderer(Timestamp.class, new CellRenderer(Timestamp.class,
                                                                         getScreenBackground(),
                                                                         getScreenColor(),
                                                                         SwingStyle.of(getTheme().getSecondary()
                                                                             .getLighterAccent(0)
                                                                             .getBackground()),
                                                                         SwingStyle.of(getTheme().getSecondary()
                                                                             .getLighterAccent(0)
                                                                             .getColor()),
                                                                         pickStatusMap));

    JScrollPane pickListPane = new JScrollPane(docsNeededTable);
    pickListPane.setBorder(new EmptyBorder(0, 0, 0, 0));
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.WEST;
    c.gridy = 3;
    c.gridx = 0;
    c.gridheight = 4;
    c.gridwidth = 4;
    c.weightx = 1;
    c.weighty = 3;
    c.insets.top = 0;
    c.insets.bottom = padding;
    c.insets.right = padding;
    c.insets.left = padding;
    c.ipadx = 1000;
    c.ipady = 275;
    add(pickListPane, c.clone());

    JLabel feedbackLabel = new JLabel("Feedback");
    feedbackLabel.setFont(getPropertyLabelFont());
    feedbackLabel.setForeground(getPropertyLabelColor());
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.SOUTHWEST;
    c.gridy = 7;
    c.gridx = 0;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.weightx = 1;
    c.weighty = 1;
    c.insets.top = labelGap;
    c.insets.bottom = padding;
    c.insets.right = padding;
    c.insets.left = padding;
    c.ipady = 0;
    c.ipadx = 0;
    add(feedbackLabel, c.clone());
    
    JScrollPane feedbackView = new JScrollPane();
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.WEST;
    c.gridx = 0;
    c.gridy = 8;
    c.gridheight = 2;
    c.gridwidth = 4;
    c.weighty = .25;
    c.insets.top = 0;
    c.insets.top = labelGap;
    c.insets.bottom = padding;
    c.insets.right = padding;
    c.insets.left = padding;
    c.ipady = 50;
//    c.ipadx = 500;
    feedbackView.setViewportView(view.getFeedbackList());
    feedbackView.setBorder(null);
    view.getFeedbackList().setBackground(getFeedbackBackground());
    add(feedbackView, c.clone());
    
    JButton reprintBtn = view.getReprintDocumentButton();
    style(reprintBtn, true, 0);
    c.fill = GridBagConstraints.NONE;
    c.anchor = GridBagConstraints.SOUTHEAST;
    c.gridy = 7;
    c.gridx = 3;
    c.gridheight = 1;
    c.gridwidth = 1;
    c.weightx = 1;
    c.weighty = 1;
    c.insets.top = labelGap;
    c.insets.bottom = padding;
    c.insets.right = padding;
    c.insets.left = padding;
    c.ipady = 10;
    c.ipadx = 100;
    add(reprintBtn, c.clone());
    
    JDialog oneUpDialog = view.getOneUpDialog();
    oneUpDialog.setModal(true);
    oneUpDialog.getContentPane().setBackground(getScreenBackground());
    oneUpDialog.setLocationRelativeTo(this);
    oneUpDialog.setLayout(new GridBagLayout());
    GridBagConstraints con = new GridBagConstraints();
    con.fill = GridBagConstraints.NONE;
    con.anchor = GridBagConstraints.NORTHWEST;
    con.gridx = 0;
    con.gridy = 0;
    con.insets.top = labelGap;
    con.insets.right = padding;
    con.insets.left = padding;
    oneUpDialog.add(new JLabel("Stock Number"), con);
    
    JTextField stockNumTF = view.getStockNumTF();
    stockNumTF.setColumns(10);
    stockNumTF.setForeground(getPropertyColor());
    stockNumTF.setFont(getPropertyFont());
    stockNumTF.setBackground(getScreenBackground());
    stockNumTF.setBorder(new MatteBorder(0, 0, 1, 0, getPropertyLabelColor()));
    con.gridx = 0;
    con.gridy = 1;
    con.gridwidth = 2;
    con.insets.bottom = padding;
    oneUpDialog.add(stockNumTF, con);
    
    JButton selectBtn = view.getSelectBtn();
    style(selectBtn, true, 0);
    con.anchor = GridBagConstraints.SOUTHEAST;
    con.gridx = 1;
    con.gridy = 2;
    con.gridwidth = 1;
    con.insets.bottom = padding;
    oneUpDialog.add(selectBtn, con);
    
    JButton cancelBtn = view.getCancelBtn();
    style(cancelBtn, true, 1);
    con.gridx = 2;
    con.gridy = 2;
    oneUpDialog.add(cancelBtn, con);
    oneUpDialog.pack();
  }

  /**
   * @return the controller
   */
  protected CartonAuditController getController()
  {
    return controller;
  }
  
  protected ITheme getTheme()
  {
    ITheme theme = new GenericTheme(MPallet.BLACK, MPallet.BLUE, MPallet.GREY);
    if (QMainFrame.getMainFrame() != null &&
        QMainFrame.getMainFrame().getTheme() != null)
    {
      theme = QMainFrame.getMainFrame().getTheme();
    }
    return theme;
  }
  
  private Color getFeedbackBackground()
  {
    return SwingStyle.of(getTheme().getText().getLighterStyle(3).getBackground());
  }

  private IStyle getScreenStyle()
  {
    return getTheme().getText().getStyle();
  }

  private Color getPropertyLabelColor()
  {
    return SwingStyle.of(getScreenStyle().getColor().opacity(.8));
  }

  private Font getPropertyLabelFont()
  {
    return SwingStyle.of(MFont.SUBHEADING);
  }

  private Font getPropertyFont()
  {
    return SwingStyle.of(MFont.HEADLINE);
  }

  private Color getPropertyColor()
  {
    return SwingStyle.of(getScreenStyle().getColor());
  }

  private Color getScreenBackground()
  {
    return SwingStyle.of(getScreenStyle().getBackground());
  }

  private Color getScreenColor()
  {
    return SwingStyle.of(getScreenStyle().getColor());
  }

  private Color getTableGridColor()
  {
    IColor filter = getScreenStyle().getColor().opacity(.1);
    IColor gridColor = getScreenStyle().getBackground();
    return SwingStyle.of(gridColor.filter(filter));
  }

  private Color getHeaderBackground()
  {
    return SwingStyle.of(getTheme().getPrimary().getLighterStyle(0).getBackground());
  }

  private Color getHeaderColor()
  {
    return SwingStyle.of(getTheme().getPrimary().getLighterStyle(0).getColor());
  }

  private Color getHeaderBorderColor()
  {
    IColor filter = getTheme().getPrimary().getLighterStyle(0).getColor().opacity(.2);
    IColor color = getTheme().getPrimary().getLighterStyle(0).getBackground();
    return SwingStyle.of(color.filter(filter));
  }

  private void style(JButton button, boolean horizontal, int index)
  {
    button.setText(button.getText().toUpperCase());
    button.setFont(SwingStyle.of(MFont.BUTTON));

    MatteBorder mb = new MatteBorder(!horizontal && index > 0 ? 1 : 0,
                                     horizontal && index > 0 ? 1 : 0,
                                     0,
                                     0,
                                     (Color) SwingStyle.of(getTheme().getSecondary()
                                         .getAccent()
                                         .getBackground()
                                         .filter(getTheme().getSecondary().getAccent().getColor().opacity(.5))));
    EmptyBorder eb = new EmptyBorder(8, 8, 8, 8);
    CompoundBorder cb = new CompoundBorder(mb, eb);
    button.setBorder(cb);
    button.setUI(new MetalButtonUI()
    {
      private IPallet pallet             = getTheme().getSecondary();
      private Color   selectColor        = SwingStyle.of(pallet.getDarkerAccent(0).getBackground());
      private Color   pressedForeground  = SwingStyle.of(pallet.getDarkerAccent(0).getColor());
      private Color   pressedBackground  = SwingStyle.of(pallet.getDarkerAccent(0).getBackground());

      private Color   focusColor         = SwingStyle.of(pallet.getLighterAccent(0).getBackground());
      private Color   focusedForeground  = SwingStyle.of(pallet.getLighterAccent(0).getColor());
      private Color   focusedBackground  = SwingStyle.of(pallet.getLighterAccent(0).getBackground());

      private Color   disabledTextColor  = SwingStyle.of(pallet.getLighterAccent(2).getColor().opacity(.5));
      private Color   disabledForeground = SwingStyle.of(pallet.getLighterAccent(2).getColor());
      private Color   disabledBackground = SwingStyle.of(pallet.getLighterAccent(2).getBackground());

      private Color   foreground         = SwingStyle.of(pallet.getAccent().getColor());
      private Color   background         = SwingStyle.of(pallet.getAccent().getBackground());

      @Override
      protected Color getSelectColor()
      {
        return selectColor;
      }

      @Override
      protected Color getFocusColor()
      {
        return focusColor;
      }

      @Override
      protected Color getDisabledTextColor()
      {
        return disabledTextColor;
      }

      private void update(JButton button)
      {
        if (button.getModel().isPressed())
        {
          button.setForeground(pressedForeground);
          button.setBackground(pressedBackground);
        }
        else if (button.hasFocus())
        {
          button.setForeground(focusedForeground);
          button.setBackground(focusedBackground);
        }
        else if (!button.isEnabled())
        {
          button.setForeground(disabledForeground);
          button.setBackground(disabledBackground);
        }
        else
        {
          button.setForeground(foreground);
          button.setBackground(background);
        }
      }

      @Override
      public void paint(Graphics g, JComponent c)
      {
        update((JButton) c);
        super.paint(g, c);
      }
    });
  }
  
  @SuppressWarnings("serial")
  private static class HeaderRenderer extends DefaultTableCellRenderer
  {
    private Border              firstBorder;
    private Border              border;
    private Font                font = SwingStyle.of(MFont.TITLE);
    private Map<String, String> columnNameMap;

    public HeaderRenderer(Color background, Color foreground, Color border, Map<String, String> columnNameMap)
    {
      setHorizontalAlignment(SwingConstants.CENTER);
      setVerticalAlignment(SwingConstants.CENTER);
      setBackground(background);
      setForeground(foreground);
      this.firstBorder = new EmptyBorder(8, 8, 8, 8);
      this.border = new CompoundBorder(new MatteBorder(0, 1, 0, 0, border),
                                       firstBorder);
      this.columnNameMap = columnNameMap;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column)
    {

      JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      c.setFont(font);
      c.setBorder(column == 0 ? firstBorder : border);
      c.setText(columnNameMap.getOrDefault(table.getColumnName(column), table.getColumnName(column)));
      return c;
    }
  }

  @SuppressWarnings("serial")
  private static class CellRenderer extends DefaultTableCellRenderer
  {
    private Class<?>             klass;
    private EmptyBorder          border;
    private Font                 font = SwingStyle.of(MFont.BODY_1);
    private Map<Integer, String> statusMap;
    private Color                background;
    private Color                foreground;
    private Color                selectedBackground;
    private Color                selectedForeground;

    public CellRenderer(Class<?> klass,
                        Color background,
                        Color foreground,
                        Color selectedBackground,
                        Color selectedForeground,
                        Map<Integer, String> statusMap)
    {
      this.klass = klass;
      this.statusMap = statusMap;
      setVerticalAlignment(SwingConstants.CENTER);
      setFont(SwingStyle.of(MFont.BODY_1));
      setHorizontalAlignment(JLabel.CENTER);
      setBackground(this.background = background);
      setForeground(this.foreground = foreground);
      this.selectedBackground = selectedBackground;
      this.selectedForeground = selectedForeground;
      setBorder(border = new EmptyBorder(8, 8, 8, 8));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column)
    {

      JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      c.setFont(font);
      if (klass == Integer.class ||
          klass == BigDecimal.class)
        c.setHorizontalAlignment(SwingConstants.RIGHT);
      else if (klass == Timestamp.class)
        c.setHorizontalAlignment(SwingConstants.CENTER);
      else
        c.setHorizontalAlignment(SwingConstants.LEFT);
      c.setBorder(border);

      if (isSelected)
      {
        c.setForeground(selectedForeground);
        c.setBackground(selectedBackground);
      }
      else
      {
        c.setForeground(foreground);
        c.setBackground(background);
      }
      return c;
    }
  }
}
