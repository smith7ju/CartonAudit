/******************************************************************************
*
* Filename:     CartonAuditController.java
* Description:
*
* (c) COPYRIGHT QC Software, LLC 2018 All Rights Reserved
* No part of this copyrighted work may be reproduced, modified, or distributed
* in any form or by any means without the prior written permission of QC
* Software, LLC.
*
* $Id: CartonAuditController.java 1144 2018-03-29 13:29:37Z jas $
* Notes:
*
*****************************************************************************/
package com.qcsoftware.jacent.ui.cartonaudit;

import java.beans.PropertyChangeEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.qcsoftware.jacent.ui.cartonaudit.DocumentsModel.DocConfig;
import com.qcsoftware.library.bean.AbstractBeanView;
import com.qcsoftware.library.bean.AbstractController;
import com.qcsoftware.library.bean.BeanListModel;
import com.qcsoftware.library.bean.BeanModelException;
import com.qcsoftware.library.bean.FeedbackMessageListModel;
import com.qcsoftware.library.database.DbResource;
import com.qcsoftware.library.database.DbTransaction;

import Library.Database.DbConnect;
import Library.QcSockets.TcpipNet.TcpipNetConnect;
import Library.Utils.AppInfo;
import Library.Utils.Closer;
import Library.Utils.Tracer;
import Packages.DbTables.ActivityLog;
import Packages.DbTables.Equipment;

/**
 * @author jas
 *
 */
public class CartonAuditController extends AbstractController
{
  @SuppressWarnings("unused")
  private static final String svnid              = "$Id: CartonAuditController.java 1144 2018-03-29 13:29:37Z jas $";
  public static final String  CARTON_AUDIT_MODEL = "CartonAuditModel";
  public static final String  CARTON_AUDIT_VIEW  = "CartonAuditView";
  private static final String INVOICE            = "INVOICE";
  private static final String CHECKIN            = "CHECK-IN";
  private static final String ACTIONPLAN         = "ACTION PLAN";
  private static final String BULLETINS          = "BULLETINS";
  private static final String MERCHANDISER       = "MERCHANDISER REFERENCE SHEET";
  private static final String ASN                = "ASN";
  private static final String EDI                = "EDI";
  private static final String SSCC18             = "SSCC-18";
  private static final String FOURUP             = "4UP";
  private static final String ONEUP              = "1UP";
  private static final String HANGTAG            = "HANG TAG";

  public CartonAuditController(DbConnect conn) throws SQLException
  {
    registerModel(new CartonAuditModel(CARTON_AUDIT_MODEL, conn));

    registerModel(getFeedback(),
                  getTableModel());
    
    this.loadPrintersTrayConfig(conn);
    initPrinters(conn);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    super.propertyChange(evt);
    if (evt.getSource() == getCartonAuditModel())
    {

    }
  }

  private void initPrinters(DbConnect conn)
  {
    String appName = AppInfo.getInstance().getAppName();

    new DbTransaction().action(dbr -> {
      ResultSet rs = dbr.getCachedSql(String.join("\n", "select id printer,",
                                                  "       assignedToOperReferId label",
                                                  "  from Equipment",
                                                  " where equipmentType in ('LabelPrinter', 'ZPLLabelPrinter')",
                                                  "   and area = ?",
                                                  " order by area;"),
                                      appName).executeQuery();
      
      while (rs.next())
      {
        if (rs.getString("label").equalsIgnoreCase("hangTag"))
          getCartonAuditModel().getDocumentsModel().setHtPrinter(rs.getString("printer"));

        if (rs.getString("label").equalsIgnoreCase("4UP"))
          getCartonAuditModel().getDocumentsModel().setFupPrinter(rs.getString("printer"));
        
        if (rs.getString("label").equalsIgnoreCase("1UP"))
          getCartonAuditModel().getDocumentsModel().setOneUpPrinter(rs.getString("printer"));
      }
    }).execute(conn);
  }
  
  public CartonAuditModel getCartonAuditModel()
  {
    return getModel(CARTON_AUDIT_MODEL, CartonAuditModel.class);
  }

  public BeanListModel<ActivityModel> getTableModel()
  {
    return getCartonAuditModel().getTable();
  }

  public CartonAuditView getView()
  {
    return getView(CARTON_AUDIT_VIEW, CartonAuditView.class);
  }

  /**
   * load config details for workstation from 'JacentPrintersDocPrintersConfig'
   * into docConfigs List<DocConfig>
   */
  protected void loadPrintersTrayConfig(DbConnect conn)
  { 
    DbResource dbr = null;
    try
    {
      dbr = new DbResource(conn);
      DocConfig docConfig = null;
      List<DocConfig> docConfigs = new ArrayList<>();
      
      ResultSet rs = dbr.queryRs(String.join("\n", "select *"
                                                 , "     , (if(flags & 1 > 0)then 1 else 0 endif) printMethod"
                                                 , "  from JacentPrintersDocPrintersConfig"
                                                 , " where Workstation = ?")
                                                 , AppInfo.getInstance().getAppName());
      
      while(rs.next())
      {
        docConfig = getCartonAuditModel().getDocumentsModel().getDocConfig();
        docConfig.setWorkstation(rs.getString("Workstation"));
        docConfig.setTray(rs.getString("Tray"));
        docConfig.setActionPlan(rs.getString("ActionPlan").equalsIgnoreCase("Yes") ? true : false);
        docConfig.setBulletins(rs.getString("Bulletins").equalsIgnoreCase("Yes") ? true : false);
        docConfig.setCheckIn(rs.getString("CheckIn").equalsIgnoreCase("Yes") ? true : false);
        docConfig.setInvoice(rs.getString("Invoice").equalsIgnoreCase("Yes") ? true : false);
        docConfig.setMerch(rs.getString("Merchandiser").equalsIgnoreCase("Yes") ? true : false);
        
        getCartonAuditModel().getDocumentsModel().setPdfBoxPrint(rs.getInt("printMethod") > 0 ? true : false);
        
        docConfigs.add(docConfig);
      }
      
      getCartonAuditModel().getDocumentsModel().setDocConfigs(docConfigs);
    }
    catch(Exception e)
    {
      dbr.rollback();
      Tracer.out("Failed load tray config /n" + e.toString() );
    }
    finally
    {
      Closer.close(dbr);
    }
  }
  
  public void lookupMoveableUnit(DbConnect conn, String moveableUnitId)
  {
    Tracer.out("Searching for carton [%s]", moveableUnitId);
    try
    {
      getCartonAuditModel().lookupMoveableUnit(conn, moveableUnitId);
      conn.commit();
      getView().populateWorkOrderId(getCartonAuditModel().getWorkOrderId());
      getView().populateInvoice(getCartonAuditModel().getInvoice());
    }
    catch (Throwable t)
    {
      Tracer.err(t);
      conn.rollback();
      getView().promptForCarton();
    }
  }

  public void printDocument(DbConnect conn) throws SQLException
  {
    switch (getCartonAuditModel().getDocument().toUpperCase())
    {
      case INVOICE:
        getCartonAuditModel().getDocumentsModel().printInvoice(conn);
        break;
      case CHECKIN:
        getCartonAuditModel().getDocumentsModel().printCheckIn(conn);
        break;
      case ACTIONPLAN:
        getCartonAuditModel().getDocumentsModel().printActionPlan(conn);
        break;
      case MERCHANDISER:
        getCartonAuditModel().getDocumentsModel().printMerchandiserSheet(conn);
        break;
      case BULLETINS:
        getCartonAuditModel().getDocumentsModel().printBulletin(conn);
        break;
      case ASN:
        getCartonAuditModel().getDocumentsModel().printAsn(conn);
      case EDI:
        getCartonAuditModel().getDocumentsModel().printEdi(conn);
        break;
      case SSCC18:
        getCartonAuditModel().getDocumentsModel().printSSCC18(conn);
        break;
      case FOURUP:
        getCartonAuditModel().getDocumentsModel().print4UpLabels();
        break;
      case ONEUP:
        //display dialog for stock#
        getCartonAuditModel().getDocumentsModel().print1UpLabels();
        break;
      case HANGTAG:
        getCartonAuditModel().getDocumentsModel().printHangTag();
        break;
    }
    
    try
    {
      getCartonAuditModel().documentPrinted(conn, getCartonAuditModel().getDocument().toUpperCase());
    }
    catch (BeanModelException e)
    {
      Tracer.err(e);
    }
  }

  public void setInvoiceLabel(String invoice)
  {
    try
    {
      getCartonAuditModel().setInvoice(invoice);
    }
    catch (BeanModelException e)
    {
      Tracer.err(e);
    }
  }

  public void setWorkOrderLabel(String workOrderId)
  {
    try
    {
      getCartonAuditModel().setWorkOrderId(workOrderId);
    }
    catch (BeanModelException e)
    {
      Tracer.err(e);
    }
  }

  public void setWorkOrderStatusLabel(String status)
  {
    try
    {
      getCartonAuditModel().setStatus(status);
    }
    catch (BeanModelException e)
    {
      Tracer.err(e);
    }
  }

  public void setReportPreviewEnabled(boolean isPreviewEnabled)
  {
    getCartonAuditModel().getDocumentsModel().setReportPreviewEnabled(isPreviewEnabled);
  }
  
  public void setPickSheetReportName(String reportName)
  {
    getCartonAuditModel().getDocumentsModel().setPickSheetReportName(reportName);
  }
  
  public void setInvoiceReportName(String reportName)
  {
    getCartonAuditModel().getDocumentsModel().setInvoiceReportName(reportName);
  }
  
  public void setCheckinReportName(String reportName)
  {
    getCartonAuditModel().getDocumentsModel().setCheckinReportName(reportName);
  }
  
  public void setMerchandiserReportName(String reportName)
  {
    getCartonAuditModel().getDocumentsModel().setMerchandiserReportName(reportName);
  }
  
  public void setEnableActionPlan(boolean isEnableActionPlan)
  {
    getCartonAuditModel().getDocumentsModel().setEnableActionPlan(isEnableActionPlan);
  }
  
  public void setEnablePlanArchive(boolean enablePlanArchive)
  {
    getCartonAuditModel().getDocumentsModel().setEnablePlanArchive(enablePlanArchive);
  }
  
  public void setPdfDir(String pdfDir)
  {
    getCartonAuditModel().getDocumentsModel().setPdfDir(pdfDir);
  }
  
  public void setPdfArchiveDir(String pdfArchiveDir)
  {
    getCartonAuditModel().getDocumentsModel().setPdfArchiveDir(pdfArchiveDir);
  }
  
  public void setLabelMgrConn(TcpipNetConnect.Connection labelMgrConn)
  {
    getCartonAuditModel().getDocumentsModel().setLabelMgrConn(labelMgrConn);
  }
  
  public void setLabelMgr(String labelMgr)
  {
    getCartonAuditModel().getDocumentsModel().setLabelMgr(labelMgr);
  }
  
  protected void setEqTypeSsccLabelPrinter(String eqTypeSsccLabelPrinter)
  {
    getCartonAuditModel().getDocumentsModel().setEqTypeSsccLabelPrinter(eqTypeSsccLabelPrinter);
  }
  
  protected void setEqTypeLabelPrinter(String eqTypeLabelPrinter)
  {
    getCartonAuditModel().getDocumentsModel().setEqTypeLabelPrinter(eqTypeLabelPrinter);
  }
  
  protected void setEqTypeGenericLabelPrinter(String eqTypeLabelPrinter)
  {
    getCartonAuditModel().getDocumentsModel().setEqTypeGenericLabelPrinter(eqTypeLabelPrinter);
  }
  
  protected void setAsnLabelName(String asnLabelName)
  {
    getCartonAuditModel().getDocumentsModel().setAsnLabelName(asnLabelName);
  }
  
  protected void setEdiLabelName(String ediLabelName)
  {
    getCartonAuditModel().getDocumentsModel().setEdiLabelName(ediLabelName);
  }

  protected void setSsccLabelName(String ssccLabelName)
  {
    getCartonAuditModel().getDocumentsModel().setSsccLabelName(ssccLabelName);
  }
  
  public void initLabelPrinterNames(DbConnect conn)
  {
    Equipment eq = null;
    String labelPrinter = null
         , sscclabelPrinter = null;
    String genericLabelPrinter = null;
    
    String appName = AppInfo.getInstance().getAppName();

    try
    {
      eq = new Equipment(conn, appName, getCartonAuditModel().getDocumentsModel().getEqTypeLabelPrinter());
      if(eq.getByKey())
        labelPrinter = eq.getHostId();
      else
      {
        eq.setId("default");
        if(eq.getByKey())
          labelPrinter = eq.getHostId();
      }

      eq.setId(appName);
      eq.setEquipmentType(getCartonAuditModel().getDocumentsModel().getEqTypeSsccLabelPrinter());
      
      if(eq.getByKey())
      {
        sscclabelPrinter = eq.getHostId();
      }
      else
      {
        eq.setId("default");
        if(eq.getByKey())
          sscclabelPrinter = eq.getHostId();
      }
      
      eq.setId(appName);
      eq.setEquipmentType(getCartonAuditModel().getDocumentsModel().getEqTypeGenericLabelPrinter());
      
      if(eq.getByKey())
      {
        genericLabelPrinter = eq.getHostId();
      }
      else
      {
        eq.setId("default");
        if(eq.getByKey())
          genericLabelPrinter = eq.getHostId();
      }
      
      getCartonAuditModel().getDocumentsModel().setLabelPrinter(labelPrinter);
      getCartonAuditModel().getDocumentsModel().setSscclabelPrinter(sscclabelPrinter);
      getCartonAuditModel().getDocumentsModel().setGenericLabelPrinter(genericLabelPrinter);
    }
    catch(Throwable t)
    {
      Tracer.err(t);
    }
    finally
    {
      Closer.close(eq);
    }
  }

  public FeedbackMessageListModel getFeedback()
  {
    return getCartonAuditModel().getFeedback();
  }
}
