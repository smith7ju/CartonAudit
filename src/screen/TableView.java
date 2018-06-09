/******************************************************************************
*
* Filename:     TableView.java
* Description:
*
* (c) COPYRIGHT QC Software, LLC 2018 All Rights Reserved
* No part of this copyrighted work may be reproduced, modified, or distributed
* in any form or by any means without the prior written permission of QC
* Software, LLC.
*
* $Id: TableView.java 1144 2018-03-29 13:29:37Z jas $
* Notes:
*
*****************************************************************************/
package com.qcsoftware.jacent.ui.cartonaudit;

import com.qcsoftware.library.bean.BeanListModel;
import com.qcsoftware.ui.mvc.swing.BeanListTableView;

/**
 * @author jas
 *
 */
public class TableView extends BeanListTableView<CartonAuditController, ActivityModel>
{
  /**
   * @param controller
   * @param model
   * @param rowClass
   * @param column
   */

  @SuppressWarnings("unused")
  private static final String svnid = "$Id: TableView.java 1144 2018-03-29 13:29:37Z jas $";

  public TableView(CartonAuditController controller)
  {
    super(controller,
          controller.getTableModel(),
          ActivityModel.class,
          ActivityModel.DOCUMENT,
          ActivityModel.PRINTCOUNT,
          ActivityModel.OPERATOR,
          ActivityModel.PRINTTIME);
  }

  @Override
  protected Object getCell(ActivityModel row, int columnIndex)
  {
    return row.getColumnValue(getColumnName(columnIndex));
  }

  @Override
  public Class<?> getColumnClass(int columnIndex)
  {
    switch (getColumnName(columnIndex))
    {
      case ActivityModel.PRINTCOUNT:
        return Integer.class;
      case ActivityModel.DOCUMENT:
      default:
        return String.class;
    }
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return false;
  }
}
