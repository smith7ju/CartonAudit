/******************************************************************************
*
* Filename:     CartonAuditModel.java
* Description:
*
* (c) COPYRIGHT QC Software, LLC 2018 All Rights Reserved
* No part of this copyrighted work may be reproduced, modified, or distributed
* in any form or by any means without the prior written permission of QC
* Software, LLC.
*
* $Id: CartonAuditModel.java 1144 2018-03-29 13:29:37Z jas $
* Notes:
*
*****************************************************************************/
package com.qcsoftware.jacent.ui.cartonaudit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.qcsoftware.jacent.tables.LamiOrderHeaderBase;
import com.qcsoftware.library.bean.BeanListModel;
import com.qcsoftware.library.bean.BeanModelException;
import com.qcsoftware.library.bean.DbTableModel;
import com.qcsoftware.library.bean.FeedbackMessageListModel;
import com.qcsoftware.library.database.DbCache;

import Gui.Security.QSecurityManager;
import Library.Database.DbConnect;
import Library.Utils.AppInfo;
import Library.Utils.Closer;
import Library.Utils.Tracer;
import Packages.DbBase.MnemonicStatus;
import Packages.DbTables.ActivityLog;
import Packages.DbTables.ContainerRoutes;
import Packages.DbTables.SystemParameters;
import Packages.DbTables.WorkOrders;

/**
 * @author jas
 *
 */
public class CartonAuditModel extends DbTableModel<ActivityLog>
{
  @SuppressWarnings("unused")
  private static final String          svnid            = "$Id: CartonAuditModel.java 1144 2018-03-29 13:29:37Z jas $";
  public static final String           MOVEABLE_UNIT_ID = "moveableUnitId";
  public static final String           STOCK_NUMBER     = "stockNumber";
  public static final String           DOCUMENT         = "document";
  public static final String           WORK_ORDER_ID    = "workOrderId";
  public static final String           STATUS           = "status";
  public static final String           INVOICE          = "invoice";
  public static final String           ACTIVITY_CODE    = "activityCode";
  private BeanListModel<ActivityModel> docs             = new BeanListModel<>();
  private DocumentsModel               documentsModel;
  private FeedbackMessageListModel     feedback         = new FeedbackMessageListModel();
  List<String>                         docsPrintedList  = new ArrayList<>();
  List<String>                         docsNeededList  = new ArrayList<>();

  public CartonAuditModel(String modelId, DbConnect conn) throws SQLException
  {
    super(modelId, new ActivityLog(conn));
    addColumn(ACTIVITY_CODE);
    addColumn(INVOICE);
    addColumn(STATUS);
    addColumn(DOCUMENT);
    addColumn(STOCK_NUMBER);

    documentsModel = new DocumentsModel(conn, this);
  }

  @Override
  public boolean refresh(ActivityLog datasource) throws SQLException, BeanModelException
  {
    boolean success = false;
    DbConnect conn = datasource.getDbConnect();
    docsPrintedList.clear();

    try (LamiOrderHeaderBase header = new LamiOrderHeaderBase(conn);
        WorkOrders wo = new WorkOrders(conn);
        DbCache dbc = new DbCache())
    {
      String workOrderId = dbc
          .getString(conn,
                     String.join("\n",
                                 "select distinct workOrderId",
                                 "  from MoveableUnitItems",
                                 " where moveableUnitId = ?"),
                     getMoveableUnitId());

      if (workOrderId != null)
      {
        header.setWorkOrderId(workOrderId);
        header.getByKey();
        setColumnValue(INVOICE, header.getInvoice());
        setColumnValue(WORK_ORDER_ID, workOrderId);

        wo.setWorkOrderId(workOrderId);
        wo.getByKey();
        int status = wo.getStatus();
        setColumnValue(STATUS, status);
        getFeedback().info("Order Status",
                           String.format("Order Status For Carton [%s] : [%s]", getMoveableUnitId(), 
                                         MnemonicStatus.get(datasource.getDbConnect(), "WORK_ORDER_STATUSES", status)));

        docs.clear();

        ResultSet rs = dbc.getDbSql(datasource.getDbConnect(),
                                    String.join("\n",
                                                "select substring(storageId, 0, (length(storageId) - 7)) document",
                                                "     , count(storageId) printCount",
                                                "     , list(distinct operatorId,', ') operatorId",
                                                "     , max(ts) ts",
                                                "  from activityLog a1",
                                                " where moveableunitid = ?",
                                                "   and activityCode = 'DOCUMENT_PRINTED'",
                                                " group by storageId"),
                                    getMoveableUnitId())
            .executeQuery();
        while (rs.next())
        {
          docsPrintedList.add(rs.getString("document"));
          Tracer.out("%s has already been printed", rs.getString("document"));
          docs.add(new ActivityModel(conn, rs));
        }

        try (ContainerRoutes cr = new ContainerRoutes(conn, getMoveableUnitId(), 1, "PACKOUT"))
        {
          if (cr.getByKey())
          {
            if (!docsPrintedList.stream().anyMatch(doc -> "Invoice".equalsIgnoreCase(doc)))
            {
              docs.add(new ActivityModel(conn, "Invoice"));
              Tracer.out("Adding Invoice to print list");
              docsNeededList.add("Invoice");
            }

            if (!docsPrintedList.stream().anyMatch(doc -> "Check-In".equalsIgnoreCase(doc)))
            {
              docs.add(new ActivityModel(conn, "Check-In"));
              Tracer.out("Adding Check-In to print list");
              docsNeededList.add("Check-In");
            }

            if (!docsPrintedList.stream().anyMatch(doc -> "Merchandiser Reference".equalsIgnoreCase(doc)))
            {
              try (SystemParameters sp = new SystemParameters(conn))
              {
                sp.setName("PRINT_MERCHANDISER_REPORT");
                if (sp.getByKey())
                {
                  if (Boolean.parseBoolean(sp.getValue()))
                  {
                    docs.add(new ActivityModel(conn, "Merchandiser"));
                    Tracer.out("Adding Merchandiser to print list");
                    docsNeededList.add("Merchandiser");
                  }
                }
              }
            }

            if (!docsPrintedList.stream().anyMatch(doc -> "Hang Tag".equalsIgnoreCase(doc)))
            {
              docs.add(new ActivityModel(conn, "Hang Tag"));
              Tracer.out("Adding Hang Tag to print list");
              docsNeededList.add("Hang Tag");
            }

            addLabelsNeeded(datasource);
          }
          else
          {
            success = false;
            docsPrintedList.clear();
            docs.clear();
            getFeedback().warning("Packout Route Not Found",
                                  String.format("No Documents Needed For Carton [%s]", getMoveableUnitId()));
          }
        }
        
        if(docsNeededList.size() == 0)
        {
          getFeedback().success("Docs Printed",
                                String.format("ALL DOCUMENTS PRINTED FOR CARTON [%s]", getMoveableUnitId()));
        }
      }
      else
      {
        success = false;
        docsPrintedList.clear();
        docs.clear();
        getFeedback().error("Carton Not Found",
                            String.format("Carton [%s] Not Found", getMoveableUnitId()));
      }
    }
    return success;
  }

  private void addLabelsNeeded(ActivityLog datasource) throws SQLException, BeanModelException
  {
    DbCache dbc = new DbCache();
    try (ResultSet rs = dbc.getDbSql(datasource.getDbConnect(),
                                     String.join("\n",
                                                 "select lc.*",
                                                 "from LamiCustomer lc",
                                                 "join Workorders w",
                                                 "on w.clientVendorId = lc.clientVendorId",
                                                 "where w.workOrderId = ?"),
                                     getWorkOrderId())
        .executeQuery())
    {
      if (rs.next())
      {
        if ("Y".equals(rs.getString("ASN")) && (!docsPrintedList.stream().anyMatch(doc -> "ASN".equalsIgnoreCase(doc))))
        {
          docs.add(new ActivityModel(datasource.getDbConnect(), "ASN"));
          Tracer.out("Adding ASN to print list");
          docsNeededList.add("ASN");
          if (!docsPrintedList.stream().anyMatch(doc -> "EDI".equalsIgnoreCase(doc)))
          {
            docs.add(new ActivityModel(datasource.getDbConnect(), "EDI"));
            Tracer.out("Adding EDI to print list");
            docsNeededList.add("EDI");
          }
        }
        if (rs.getInt("printSSCC18") == 1
            && (!docsPrintedList.stream().anyMatch(doc -> "SSCC18".equalsIgnoreCase(doc))))
        {
          docs.add(new ActivityModel(datasource.getDbConnect(), "SSCC18"));
          Tracer.out("Adding SSCC18 to print list");
          docsNeededList.add("SSCC18");
        }
        if (rs.getInt("print4UP") == 1 && (!docsPrintedList.stream().anyMatch(doc -> "4UP".equalsIgnoreCase(doc))))
        {
          docs.add(new ActivityModel(datasource.getDbConnect(), "4UP"));
          Tracer.out("Adding 4UP to print list");
          docsNeededList.add("4UP");
        }
        if ("Y".equals(rs.getString("print1UP"))
            && (!docsPrintedList.stream().anyMatch(doc -> "1UP".equalsIgnoreCase(doc))))
        {
          docs.add(new ActivityModel(datasource.getDbConnect(), "1UP"));
          Tracer.out("Adding 1UP to print list");
          docsNeededList.add("1UP");
        }
      }
    }
    finally
    {
      Closer.close(dbc);
    }
  }

  public void lookupMoveableUnit(DbConnect conn, String moveableUnitId) throws SQLException, BeanModelException
  {
    try (DbCache dbc = new DbCache())
    {
      getBean().loadTableData(dbc.getDbTable(conn, ActivityLog.class));
      setColumnValue(STATUS, null);
      setColumnValue(INVOICE, null);
      setColumnValue(WORK_ORDER_ID, null);
      setColumnValue(MOVEABLE_UNIT_ID, moveableUnitId);
      refresh(dbc.getDbTable(conn, ActivityLog.class));
    }
  }

  public void documentPrinted(DbConnect conn, String document) throws SQLException, BeanModelException
  {
    String operator = QSecurityManager.getInstance().getCurrentUser();

    try (ActivityLog al = new ActivityLog(conn))
    {
      al.setActivityCode("DOCUMENT_PRINTED");
      al.setIdx(null);
      al.setStorageId(document + "_Printed");
      al.setWorkOrderId(getWorkOrderId());
      al.setMoveableUnitId(getMoveableUnitId());
      al.setLocationId(AppInfo.getInstance().getAppName());
      al.setOperatorId(operator);

      if (al.insert())
        conn.commit();
      else
        conn.rollback();
    }
    
    try (DbCache dbc = new DbCache())
    {
      refresh(dbc.getDbTable(conn, ActivityLog.class));
    }
  }

  public BeanListModel<ActivityModel> getTable()
  {
    return docs;
  }

  public Integer getInvoice()
  {
    return getBean().getColumnInfo(INVOICE).getValue(Integer.class);
  }

  public void setInvoice(String invoice) throws BeanModelException
  {
    setColumnValue(INVOICE, invoice);
  }

  public String getWorkOrderId()
  {
    return getBean().getWorkOrderId();
  }

  public void setWorkOrderId(String workOrderId) throws BeanModelException
  {
    setColumnValue(WORK_ORDER_ID, workOrderId);
  }

  public String getMoveableUnitId()
  {
    return getBean().getMoveableUnitId();
  }

  public void setStatus(String status) throws BeanModelException
  {
    setColumnValue(STATUS, status);
  }

  public void setDocument(String document) throws BeanModelException
  {
    setColumnValue(DOCUMENT, document);
  }

  public String getDocument()
  {
    return getBean().getColumnInfo(DOCUMENT).getValue(String.class);
  }

  public void setStockNumber(String stockNum) throws BeanModelException
  {
    setColumnValue(STOCK_NUMBER, stockNum);
  }

  public String getStockNumber()
  {
    return getBean().getColumnInfo(STOCK_NUMBER).getValue(String.class);
  }

  public DocumentsModel getDocumentsModel()
  {
    return documentsModel;
  }

  public FeedbackMessageListModel getFeedback()
  {
    return feedback;
  }

  public String getStoreNo()
  {
    return null;
  }
}
