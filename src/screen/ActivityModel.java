/******************************************************************************
*
* Filename:     TableModel.java
* Description:
*
* (c) COPYRIGHT QC Software, LLC 2018 All Rights Reserved
* No part of this copyrighted work may be reproduced, modified, or distributed
* in any form or by any means without the prior written permission of QC
* Software, LLC.
*
* $Id: ActivityModel.java 1144 2018-03-29 13:29:37Z jas $
* Notes:
*
*****************************************************************************/
package com.qcsoftware.jacent.ui.cartonaudit;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.qcsoftware.library.bean.BeanModelException;
import com.qcsoftware.library.bean.DbTableModel;
import com.qcsoftware.library.database.DbCache;

import Library.Database.DbConnect;
import Packages.DbTables.ActivityLog;
import Packages.DbTables.WorkOrderLineOperations;

/**
 * @author jas
 *
 */
public class ActivityModel extends DbTableModel<ActivityLog>
{
  @SuppressWarnings("unused")
  private static final String svnid      = "$Id: ActivityModel.java 1144 2018-03-29 13:29:37Z jas $";
  public static final String  DOCUMENT   = "document";
  public static final String  OPERATOR   = "operatorId";
  public static final String  PRINTCOUNT = "printCount";
  public static final String  PRINTTIME  = "ts";

  public ActivityModel(DbConnect conn) throws SQLException
  {
    super(new ActivityLog(conn));
    addColumn(DOCUMENT);
    addColumn(PRINTCOUNT);
    addColumn(OPERATOR);
    addColumn(PRINTTIME);
  }
  
  public ActivityModel(DbConnect conn, String document) throws SQLException, BeanModelException
  {
    this(conn);
    
    setColumnValue(DOCUMENT, document);
    setColumnValue(PRINTCOUNT, 0);
  }

  public ActivityModel(DbConnect conn, ResultSet rs) throws SQLException, BeanModelException
  {
    this(conn);
    try (DbCache dbc = new DbCache())
    {
      loadTableData(rs);
    }
  }

  @Override
  public boolean refresh(ActivityLog datasource) throws SQLException, BeanModelException
  {
    try (DbCache dbc = new DbCache())
    {
      ResultSet rs = dbc.getDbSql(datasource.getDbConnect(),
                                  "select 'invoice' , 0")
          .executeQuery();
      if (rs.next())
      {
        loadTableData(rs);
      }
    }

    return false;
  }
}
