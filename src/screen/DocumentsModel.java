/******************************************************************************
*
* Filename:     DocumentsModel.java
* Description:
*
* (c) COPYRIGHT QC Software, LLC 2017 All Rights Reserved
* No part of this copyrighted work may be reproduced, modified, or distributed
* in any form or by any means without the prior written permission of Queen
* City Software, Inc.
*
* $Id: DocumentsModel.java 1144 2018-03-29 13:29:37Z jas $
* Notes:
*
*****************************************************************************/
package com.qcsoftware.jacent.ui.cartonaudit;

import java.awt.Color;
import java.awt.print.Book;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.SimpleDoc;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Media;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.Orientation;
import org.apache.pdfbox.printing.PDFPageable;

import com.qcsoftware.jacent.tables.LamiCustomerBase;
import com.qcsoftware.library.bean.BeanModelException;
import com.qcsoftware.library.bean.DbTableModel;
import com.qcsoftware.library.database.DbCache;

import Library.Database.DbConnect;
import Library.Print.FlexReportPrinter;
import Library.Print.GetMediaTrays;
import Library.Print.PreviewDlg;
import Library.QcSockets.TcpipNet.TcpipHdr;
import Library.QcSockets.TcpipNet.TcpipNetConnect;
import Library.QcSockets.TcpipNet.TcpipSendReceive;
import Library.Utils.AppInfo;
import Library.Utils.SystemDefs;
import Library.Utils.Tracer;
import Library.Utils.TracerLevels;
import Packages.DbTables.Equipment;
import Packages.OrderMgmt.OrderMgmt;
import Products.LabelMgr.LabelMgr;
import Products.LabelMgr.LabelMgrConstants;
import Products.LabelMgr.LabelMgrMsg;
import Products.LabelMgr.RePrintRequest;

public class DocumentsModel extends DbTableModel<LamiCustomerBase>
{
  /**
   * 
   */
  private static final long          serialVersionUID       = 4046992238838037867L;

  @SuppressWarnings("unused")
  private static final String        svnid                  = "$Id: DocumentsModel.java 1144 2018-03-29 13:29:37Z jas $";

  public static final String         PICK_SHEET_PRINTER     = "pickSheetPrinter";
  public static final String         INVOICE_PRINTER        = "invoicePrinter";
  public static final String         CHECK_IN_SHEET_PRINTER = "checkInSheetPrinter";
  public static final String         MERCHANDISER_PRINTER   = "merchandiserPrinter";
  private final CartonAuditModel     model;
  private String                     pickSheetReportName;
  private String                     invoiceReportName;
  private String                     merchandiserReportName;
  private String                     checkinReportName;
  private boolean                    reportPreviewEnabled;
  private boolean                    enableActionPlan;
  private boolean                    pdfBoxPrint;
  private String                     pdfDir;
  private String                     pdfArchiveDir;
  private boolean                    enablePlanArchive;
  private PrintRequestAttributeSet   pset;
  private List<DocConfig>            docConfigs;
  private File                       bulletin, actioPlan;
  private String                     labelMgr;
  private String                     eqTypeLabelPrinter, eqTypeSsccLabelPrinter, eqTypeGenericLabelPrinter;
  private String                     sscclabelPrinter, labelPrinter;
  private String                     genericLabelPrinter;
  private TcpipNetConnect.Connection labelMgrConn;
  private String                     asnLabelName;
  private String                     ssccLabelName;
  private String                     ediLabelName;
  private String                     hangTagLabelName;
  private String                     fourUPLabelName;
  private String                     oneUPLabelName;
  private String                     htPrinter;
  private String                     fupPrinter;
  private String                     oneUpPrinter;

  public DocumentsModel(DbConnect conn, CartonAuditModel model) throws SQLException
  {
    super(new LamiCustomerBase(conn));
    this.model = model;
  }

  // @Override
  // public boolean refresh(LamiCustomerBase datasource) throws SQLException, BeanModelException
  // {
  // getBean().setClientVendorId(model.getClientVendorId());
  // return super.refresh(datasource);
  // }

  public void setPickSheetReportName(String pickSheetReportName)
  {
    this.pickSheetReportName = pickSheetReportName;
  }

  public void setInvoiceReportName(String invoiceReportName)
  {
    this.invoiceReportName = invoiceReportName;
  }

  public void setCheckinReportName(String checkinReportName)
  {
    this.checkinReportName = checkinReportName;
  }

  public void setMerchandiserReportName(String merchandiserReportName)
  {
    this.merchandiserReportName = merchandiserReportName;
  }

  public void setReportPreviewEnabled(boolean reportPreviewEnabled)
  {
    this.reportPreviewEnabled = reportPreviewEnabled;
  }

  public void setEnableActionPlan(boolean enableActionPlan)
  {
    this.enableActionPlan = enableActionPlan;
  }

  public void setEnablePlanArchive(boolean enablePlanArchive)
  {
    this.enablePlanArchive = enablePlanArchive;
  }

  public void setPdfDir(String pdfDir)
  {
    this.pdfDir = pdfDir;
  }

  public void setPdfArchiveDir(String pdfArchiveDir)
  {
    this.pdfArchiveDir = pdfArchiveDir;
  }

  public boolean isPrintAsn()
  {
    return "Y".equalsIgnoreCase(getBean().getAsn()) || "1".equalsIgnoreCase(getBean().getAsn());
  }

  public boolean isPrintSSCC18()
  {
    return getBean().getPrintSSCC18() == null ? false : getBean().getPrintSSCC18();
  }

  public boolean isEnableActionPlan()
  {
    return enableActionPlan;
  }

  public boolean isEnablePlanArchive()
  {
    return enablePlanArchive;
  }

  public String getCheckinReportName()
  {
    return checkinReportName;
  }

  public String getInvoiceReportName()
  {
    return invoiceReportName;
  }

  public String getPickSheetReportName()
  {
    return pickSheetReportName;
  }

  public String getPdfArchiveDir()
  {
    return pdfArchiveDir;
  }

  public String getPdfDir()
  {
    return pdfDir;
  }

  protected PrintService getPrintService()
  {
    return PrintServiceLookup.lookupDefaultPrintService();
  }

  public void printPickSheet(DbConnect conn, String operatorId)
  {
    Tracer.msg(OrderMgmt.TRACER_ORDER_GENERAL,
               "Printing pickSheet for order[%s]; operator[%s]",
               model.getWorkOrderId(), operatorId);
    Map<String, String> reportParams = new HashMap<>();
    reportParams.put("workOrderId", model.getWorkOrderId());
    printFlexReport(conn,
                    "Pick Sheet",
                    pickSheetReportName,
                    PICK_SHEET_PRINTER,
                    reportParams);
  }

  public void printInvoice(DbConnect conn)
  {
    pset = null;
    boolean isTrayAssigned = assignTray(Report.INVOICE);

    if (isTrayAssigned)
    {
      Map<String, String> reportParams = new HashMap<>();
      reportParams.put("containerId", model.getMoveableUnitId());
      reportParams.put("invoiceBarcode", Integer.toString(model.getInvoice()));
      printFlexReport(conn,
                      "Invoice",
                      invoiceReportName,
                      INVOICE_PRINTER,
                      reportParams);
    }
    else
    {
      model.getFeedback()
          .failure(String.format("Failed to print %s; %s", "Invoice", "Failed get Tray id"));
    }
  }

  public void printCheckIn(DbConnect conn)
  {
    pset = null;
    boolean isTrayAssigned = assignTray(Report.CHECK_IN);

    if (isTrayAssigned)
    {
      Map<String, String> reportParams = new HashMap<>();
      reportParams.put("containerId", model.getMoveableUnitId());
      printFlexReport(conn,
                      "CheckIn Sheet",
                      checkinReportName,
                      CHECK_IN_SHEET_PRINTER,
                      reportParams);
    }
    else
    {
      model.getFeedback()
          .failure(String.format("Failed to print %s; %s", "CheckIn Sheet", "Failed get Tray id"));
    }
  }

  public void printMerchandiserSheet(DbConnect conn)
  {
    pset = null;
    boolean isTrayAssigned = assignTray(Report.MERCH);

    if (isTrayAssigned)
    {
      Map<String, String> reportParams = new HashMap<>();
      reportParams.put("containerId", model.getMoveableUnitId());
      reportParams.put("invoiceBarcode", Integer.toString(model.getInvoice()));
      printFlexReport(conn,
                      "Merchandiser",
                      merchandiserReportName,
                      MERCHANDISER_PRINTER,
                      reportParams);
    }
    else
    {
      model.getFeedback()
          .failure(String.format("Failed to print %s; %s", "Merchandiser Sheet", "Failed get Tray id"));
    }
  }

  public void printBulletin(DbConnect conn)
  {
    pset = null;
    loadBulletin(conn);

    if (getBulletin() != null)
    {
      boolean isTrayAssigned = assignTray(Report.BULLETINS);

      if (isTrayAssigned)
      {
        printPdf("Bulletin", getBulletin());
      }
      else
      {
        model.getFeedback()
            .failure(String.format("Failed to print %s; %s", "Bulletin", "Failed get Tray id"));
      }
    }
  }

  private File getBulletin()
  {
    return bulletin;
  }

  public void printActionPlan(DbConnect conn)
  {
    pset = null;
    loadActionPlan(conn);

    if (getActionPlan() != null)
    {
      boolean isTrayAssigned = assignTray(Report.ACTION_PLAN);

      if (isTrayAssigned)
      {
        printPdf("Action Plan", getActionPlan());
      }
      else
      {
        model.getFeedback()
            .failure(String.format("Failed to print %s; %s", "Action Plan", "Failed get Tray id"));
      }
    }
  }

  private File getActionPlan()
  {
    return this.actioPlan;
  }

  /**
   * @param asnLabelName the asnLabelName to set
   */
  protected void setAsnLabelName(String asnLabelName)
  {
    this.asnLabelName = asnLabelName;
  }

  /**
   * @param ediLabelName the ediLabelName to set
   */
  protected void setEdiLabelName(String ediLabelName)
  {
    this.ediLabelName = ediLabelName;
  }

  /**
   * @param ssccLabelName the ssccLabelName to set
   */
  protected void setSsccLabelName(String ssccLabelName)
  {
    this.ssccLabelName = ssccLabelName;
  }

  protected void printFlexReport(DbConnect conn,
                                 String reportDescription,
                                 String reportName,
                                 String equipmentType,
                                 Map<String, String> reportParams)
  {
    try (DbCache dbc = new DbCache())
    {
      FlexReportPrinter printer = new FlexReportPrinter(conn);
      printer.setXmlReport(String.join(File.separator,
                                       SystemDefs.getDefinition("CONFIG_DIR"),
                                       reportName));
      printer.setDebug(reportPreviewEnabled);

      printer.setPrintService(getPrintService());

      if ("Pick Sheet".equals(reportDescription))
      {
        Equipment equipment = dbc.getDbTable(conn, Equipment.class);
        equipment.setArea(AppInfo.getInstance().getAppName());
        equipment.setEquipmentType(equipmentType);
        if (equipment.getSingleRow("*",
                                   "area = ? and equipmentType = ?",
                                   equipment.getColumnInfo("area"),
                                   equipment.getColumnInfo("equipmentType")))
        {
          for (PrintService service : PrintServiceLookup.lookupPrintServices(null, null))
          {
            if (equipment.getHostId().equals(service.getName()))
            {
              printer.setPrintService(service);
              break;
            }
          }
        }
      }
      else
      {
        printer.setAttributes(pset);
      }

      for (Entry<String, String> entry : reportParams.entrySet())
        printer.setProperty(entry.getKey(), entry.getValue());

      if (printer.print())
        model.getFeedback().info(String.format("Printing %s...", reportDescription));
      else if (printer.getErrMsg() != null)
        model.getFeedback()
            .failure(String.format("Failed to print %s; %s", reportDescription, printer.getErrMsg()));
      else
        model.getFeedback()
            .failure(String.format("Failed to print %s", reportDescription));
    }
    catch (Throwable t)
    {
      Tracer.err(t);
      model.getFeedback().error(t);
    }

  }

  protected void printPdf(String pdfDescription,
                          File pdf)
  {
    boolean okayToArchive = false;

    try
    {
      if (pdf.exists())
      {
        try (PDDocument document = PDDocument.load(pdf))
        {
          File parentDir = new File(pdfDir);

          if (parentDir.equals(pdf.getParentFile()))
            okayToArchive = pdf.exists();

          if (reportPreviewEnabled)
          {
            PDFPageable pageable = new PDFPageable(document);
            PreviewDlg dlg = new PreviewDlg();
            Book bk = new Book();
            for (int i = 0; i < document.getNumberOfPages(); i++)
            {
              bk.append(pageable.getPrintable(i), pageable.getPageFormat(i));
            }

            dlg.showBook(bk);
            document.close();

            if (okayToArchive)
              archiveFile(pdf);
          }
          else
          {
            model.getFeedback().info(String.format("Printing %s...", pdfDescription));

            if (isPdfBoxPrint())
            {
              PrinterJob job = PrinterJob.getPrinterJob();
              job.setPageable(new org.apache.pdfbox.printing.PDFPageable(document, Orientation.AUTO, true));
              job.setPrintService(getPrintService());
              job.print(pset);
            }
            else
            {
              FileInputStream fis = new FileInputStream(pdf);
              Doc pdfDoc = new SimpleDoc(fis, DocFlavor.INPUT_STREAM.AUTOSENSE, null);
              DocPrintJob printJob = getPrintService().createPrintJob();
              printJob.print(pdfDoc, pset);
            }

            model.getFeedback().info(String.format("Printed %s...", pdfDescription));

            if (okayToArchive)
              archiveFile(pdf);
          }
        }
        catch (Throwable t)
        {
          Tracer.err(t);
          model.getFeedback().error(t);
        }
      }
    }
    catch (Throwable t)
    {
      Tracer.err(t);
      model.getFeedback().error(t);
    }
  }

  /**
   * archives action plan pdf file to archive directory if specified
   */
  private void archiveFile(File f)
  {
    File nf;

    if (pdfArchiveDir != null && enablePlanArchive)
    {
      Tracer.msg(TracerLevels.TRACER_CONFIG_FILE, "Archiving file..." + f.getName());
      model.getFeedback().success("Archiving file..." + f.getName());

      nf = new File(pdfArchiveDir + File.separator + f.getName());
      try
      {
        Files.move(f.toPath(), nf.toPath(),
                   StandardCopyOption.REPLACE_EXISTING);
      }
      catch (Throwable t)
      {
        Tracer.err(t);
        model.getFeedback().warning(t.toString());
      }
    }
  }

  /*
   * Load bulletin file from specified dir
   */
  public void loadBulletin(DbConnect conn)
  {
    String orderId = model.getWorkOrderId();
    int idx = orderId.indexOf(".");
    if (idx > 0)
      orderId = orderId.substring(0, idx);

    if (pdfDir != null)
    {
      File dir = new File(pdfDir);
      if (dir.exists())
      {
        for (File bulletin : dir.listFiles(new BulletinFilter(dir, orderId)))
        {
          setBulletin(bulletin);
          break;
        }
      }
    }

    if (getBulletin() == null &&
        pdfArchiveDir != null)
    {
      File dir = new File(pdfArchiveDir);
      if (dir.exists())
      {
        for (File bulletin : dir.listFiles(new BulletinFilter(dir, orderId)))
        {
          setBulletin(bulletin);
          break;
        }
      }
    }

    if (getBulletin() != null)
      model.getFeedback().success("Bulletin found");
    else
      model.getFeedback().warning("Bulletin not found");
  }

  private void setBulletin(File bulletin)
  {
    this.bulletin = bulletin;
  }

  /**
   * @return the docConfigs
   */
  private List<DocConfig> getDocConfigs()
  {
    if (docConfigs == null)
      docConfigs = new ArrayList<>();

    return docConfigs;
  }

  /**
   * @param docConfigs the docConfigs to set
   */
  protected void setDocConfigs(List<DocConfig> docConfigs)
  {
    this.docConfigs = docConfigs;
  }

  /**
   * Lookup tray to print basing on report/workstation data
   * if none configured at 'JacentPrintersDocPrintersConfig'
   * return false
   * 
   * @param report
   * @param ps
   * @return
   */
  private boolean assignTray(Report report)
  {
    boolean retval = false;

    String tray = null;

    tray = getTray(report);

    if (tray != null)
    {
      for (Media m : GetMediaTrays.getTrays(getPrintService()))
      {
        String mediaTray = m.toString().trim();
        if (mediaTray.equalsIgnoreCase(tray))
        {
          pset = new HashPrintRequestAttributeSet();
          Tracer.out("Adding attribute to PrintRequestAttributeSet: " + m.toString());
          pset.add(m);
          retval = true;
          break;
        }
      }
    }

    return retval;
  }

  /*
   * Helper method to return tray id for specified report
   */
  private String getTray(Report report)
  {
    String tray = null;

    switch (report)
    {
      case ACTION_PLAN:
        tray = getDocConfigs().stream()
            .filter(c -> c.isActionPlan())
            .findFirst()
            .map(fc -> fc.getTray())
            .orElse(null);

        break;

      case BULLETINS:
        tray = getDocConfigs().stream()
            .filter(c -> c.isBulletins())
            .findFirst()
            .map(fc -> fc.getTray())
            .orElse(null);
        break;

      case CHECK_IN:
        tray = getDocConfigs().stream()
            .filter(c -> c.isCheckIn())
            .findFirst()
            .map(fc -> fc.getTray())
            .orElse(null);
        break;

      case INVOICE:
        tray = getDocConfigs().stream()
            .filter(c -> c.isInvoice())
            .findFirst()
            .map(fc -> fc.getTray())
            .orElse(null);
        break;

      case MERCH:
        tray = getDocConfigs().stream()
            .filter(c -> c.isMerch())
            .findFirst()
            .map(fc -> fc.getTray())
            .orElse(null);

      default:
        break;
    }
    ;

    return tray.trim();
  }

  private class BulletinFilter implements FilenameFilter
  {
    String bulletinMatch = null;
    File   pdfDir;

    public BulletinFilter(File pdfDir, String invoice)
    {
      this.pdfDir = pdfDir;
      bulletinMatch = (invoice + "DB").toUpperCase();
    }

    @Override
    public boolean accept(File dir, String name)
    {
      boolean accept = false;
      String tmpName = name.toUpperCase();

      if (pdfDir.equals(dir))
      {
        if (tmpName.startsWith(bulletinMatch) &&
            tmpName.endsWith("PDF"))
          accept = true;
      }

      return accept;
    }
  }

  /*
   * Load action plan pdf from specified dir
   */
  public void loadActionPlan(DbConnect conn)
  {
    String orderNo = model.getWorkOrderId();
    int idx = orderNo.indexOf(".");
    if (idx > 0)
      orderNo = orderNo.substring(0, idx);

    File pdf = null;

    if (pdfDir != null)
      pdf = new File(pdfDir + File.separator + orderNo + ".pdf");

    if ((pdf == null || !pdf.exists()) && (pdfArchiveDir != null))
      pdf = new File(pdfArchiveDir + File.separator + orderNo + ".pdf");

    if (pdf != null)
      model.getFeedback().success("Order's Action Plan found");
    else
    {
      if ((pdf == null || !pdf.exists()) && (pdfDir != null))
        pdf = new File(pdfDir + File.separator + model.getStoreNo() + "AP.pdf");

      if ((pdf == null || !pdf.exists()) && (pdfArchiveDir != null))
        pdf = new File(pdfArchiveDir + File.separator + model.getStoreNo() + "AP.pdf");
    }

    if (pdf != null)
    {
      setActionPlan(pdf);
      model.getFeedback().success("Action Plan found");
    }
    else
      model.getFeedback().warning("Action Plan not found");
  }

  private void setActionPlan(File actionPlan)
  {
    this.actioPlan = actionPlan;
  }

  public void printAsn(DbConnect conn)
  {
    RePrintRequest request = new RePrintRequest(LabelMgrMsg.GENERIC_PRINT_REQUEST);

    request.addValue("moveableUnitId", model.getMoveableUnitId());
    request.addValue(LabelMgrConstants.LABEL_ATTR, asnLabelName);

    model.getFeedback().info(String.format("Printing %s...", "ASN"));
    sendLabelRequest(request, "ASN", labelPrinter);
  }

  public void printEdi(DbConnect conn)
  {
    RePrintRequest request = new RePrintRequest(LabelMgrMsg.GENERIC_PRINT_REQUEST);

    request.addValue("moveableUnitId", model.getMoveableUnitId());
    request.addValue(LabelMgrConstants.LABEL_ATTR, ediLabelName);

    model.getFeedback().info(String.format("Printing %s...", "EDI"));
    sendLabelRequest(request, "EDI", labelPrinter);
  }

  public void printSSCC18(DbConnect conn)
  {
    RePrintRequest request = new RePrintRequest(LabelMgrMsg.GENERIC_PRINT_REQUEST);

    request.addValue("moveableUnitId", model.getMoveableUnitId());
    request.addValue(LabelMgrConstants.LABEL_ATTR, ssccLabelName);

    model.getFeedback().info(String.format("Printing %s...", "SSCC"));
    sendLabelRequest(request, "SSCC", sscclabelPrinter);
  }

  public void printHangTag()
  {
    int rv;
    RePrintRequest req = new RePrintRequest(LabelMgrMsg.MU_REPRINT_REQUEST);
    req.setPrinterName(htPrinter);
    req.addValue(LabelMgrConstants.LABEL_ATTR, "HANGTAG");

    req.addValue("moveableUnitId", model.getMoveableUnitId());

    rv = req.send(labelMgr);
    if (rv < 1)
      model.getFeedback().error("Print request(HANGTAG) failed to send");
    else
      model.getFeedback().info(String.format("Printing %s...", "Shelf Label"));
  }
  
  public void print4UpLabels()
  {
    int rv;
    RePrintRequest req = new RePrintRequest(LabelMgrMsg.MU_REPRINT_REQUEST);
    req.setPrinterName(getFupPrinter());
    req.addValue(LabelMgrConstants.LABEL_ATTR, "4UP");
   
    req.addValue("moveableUnitId", model.getMoveableUnitId());
    
    rv = req.send(labelMgr);
    if (rv < 1)
      model.getFeedback().error("Print request(4UP) failed to send");
    else
      model.getFeedback().info(String.format("Printing %s...", "4UP Labels"));
  }
  
  public void print1UpLabels()
  {
    int rv;
    RePrintRequest req = new RePrintRequest(LabelMgrMsg.MU_REPRINT_REQUEST);
    req.setPrinterName(getOneUpPrinter());
    req.addValue(LabelMgrConstants.LABEL_ATTR, "1UP");

    req.addValue("moveableUnitId", model.getMoveableUnitId());
    req.addValue("stk", model.getStockNumber());
    
    rv = req.send(labelMgr);
    if (rv < 1)
      model.getFeedback().error("Print request(1UP) failed to send");
    else
      model.getFeedback().info(String.format("Printing %s...", "1UP Labels"));
  }

  /**
   * sends a label request to the label manager
   * 
   * @param request
   * @param labelType
   * @param destPrinterName
   */
  @SuppressWarnings("rawtypes")
  private void sendLabelRequest(RePrintRequest request, String labelType,
                                String destPrinterName)
  {
    String message = null,
        dlgTitle = null;

    if (!labelMgrConn.isClosed())
    {
      if (request != null)
      {
        TcpipHdr response = null;
        TcpipSendReceive<TcpipHdr> tsr = new TcpipSendReceive<TcpipHdr>(request, TcpipHdr.class);

        request.setPrinterName(destPrinterName);
        Tracer.msg(LabelMgr.TRACER_LABEL,
                   "Setting printer [%s].",
                   destPrinterName);

        Tracer.msg(LabelMgr.TRACER_LABEL,
                   "Sending print quest to [%s].",
                   labelMgr);

        response = tsr.send(labelMgr, 5000);

        if (response != null)
        {
          switch (response.getStatus())
          {
            case LabelMgrMsg.INVALID_MOVEABLE_UNIT:
              message = "Invalid Carton Number";
              dlgTitle = "Re-Print Error";
              break;

            case LabelMgrMsg.INVALID_PRINTER_NAME:
              message = "Invalid Printer Name";
              dlgTitle = "Re-Print Error";
              break;

            case LabelMgrMsg.INVALID_SEQUENCE_NO:
              message = "Invalid Sequence Number";
              dlgTitle = "Re-Print Error";
              break;

            case LabelMgrMsg.INVALID_WORK_ORDER_ID:
              message = "Invalid Order Number";
              dlgTitle = "Re-Print Error";
              break;

            case LabelMgrMsg.PRINTER_BUSY:
              message = "Printer has too many jobs in queue.";
              dlgTitle = "Printer Busy";
              break;

            case LabelMgrMsg.PRINTER_OFFLINE:
              message = "Printer is off-line.";
              dlgTitle = "Printer Off-line";
              break;

            default:
              message = "Printing " + labelType + " to " + destPrinterName;

          }
        }
        else
        {
          message = "Time-out occurred waiting for " +
                    "response from [" + labelMgr + "].";
          dlgTitle = "Re-Print Error";
        }
      }

      if (dlgTitle != null)
        model.getFeedback().warning(dlgTitle + " " + message);

    }
    else
    {
      model.getFeedback().warning("No connection to the Label Manager.");
    }
  }

  public boolean isPrintBulletin()
  {
    boolean isPrint = false;
    loadBulletin(null);

    if (getBulletin() != null)
      isPrint = true;

    return isPrint;
  }

  protected DocConfig getDocConfig()
  {
    return new DocConfig();
  }

  /**
   * @param labelMgr the labelMgr to set
   */
  protected void setLabelMgr(String labelMgr)
  {
    this.labelMgr = labelMgr;
  }

  /**
   * @param labelMgrConn the labelMgrConn to set
   */
  protected void setLabelMgrConn(TcpipNetConnect.Connection labelMgrConn)
  {
    this.labelMgrConn = labelMgrConn;
  }

  /**
   * @return the eqTypeLabelPrinter
   */
  protected String getEqTypeLabelPrinter()
  {
    return eqTypeLabelPrinter;
  }

  /**
   * @param eqTypeLabelPrinter the eqTypeLabelPrinter to set
   */
  protected void setEqTypeLabelPrinter(String eqTypeLabelPrinter)
  {
    this.eqTypeLabelPrinter = eqTypeLabelPrinter;
  }

  /**
   * @return the eqTypeGenericLabelPrinter
   */
  protected String getEqTypeGenericLabelPrinter()
  {
    return eqTypeGenericLabelPrinter;
  }

  /**
   * @param eqTypeGenericLabelPrinter the eqTypeGenericLabelPrinter to set
   */
  protected void setEqTypeGenericLabelPrinter(String eqTypeGenericLabelPrinter)
  {
    this.eqTypeGenericLabelPrinter = eqTypeGenericLabelPrinter;
  }

  /**
   * @return the eqTypeSsccLabelPrinter
   */
  protected String getEqTypeSsccLabelPrinter()
  {
    return eqTypeSsccLabelPrinter;
  }

  /**
   * @param eqTypeSsccLabelPrinter the eqTypeSsccLabelPrinter to set
   */
  protected void setEqTypeSsccLabelPrinter(String eqTypeSsccLabelPrinter)
  {
    this.eqTypeSsccLabelPrinter = eqTypeSsccLabelPrinter;
  }

  /**
   * @param sscclabelPrinter the sscclabelPrinter to set
   */
  protected void setSscclabelPrinter(String sscclabelPrinter)
  {
    this.sscclabelPrinter = sscclabelPrinter;
  }

  /**
   * @param labelPrinter the labelPrinter to set
   */
  protected void setLabelPrinter(String labelPrinter)
  {
    this.labelPrinter = labelPrinter;
  }

  protected void setGenericLabelPrinter(String genericLabelPrinter)
  {
    this.genericLabelPrinter = genericLabelPrinter;
  }

  public String getHtPrinter()
  {
    return htPrinter;
  }

  public void setHtPrinter(String htPrinter)
  {
    this.htPrinter = htPrinter;
  }

  public String getFupPrinter()
  {
    return fupPrinter;
  }

  public void setFupPrinter(String fupPrinter)
  {
    this.fupPrinter = fupPrinter;
  }

  public String getOneUpPrinter()
  {
    return oneUpPrinter;
  }

  public void setOneUpPrinter(String oneUpPrinter)
  {
    this.oneUpPrinter = oneUpPrinter;
  }

  /**
   * @return the pdfBoxPrint
   */
  protected boolean isPdfBoxPrint()
  {
    return pdfBoxPrint;
  }

  /**
   * @param pdfBoxPrint the pdfBoxPrint to set
   */
  protected void setPdfBoxPrint(boolean pdfBoxPrint)
  {
    this.pdfBoxPrint = pdfBoxPrint;
  }

  /*
   * POJO to keep document config data
   */
  protected class DocConfig
  {
    private String  workstation, tray;

    private boolean actionPlan, bulletins, checkIn, invoice, merch;

    /**
     * @return the workstation
     */
    @SuppressWarnings("unused")
    private String getWorkstation()
    {
      return workstation;
    }

    /**
     * @param workstation the workstation to set
     */
    void setWorkstation(String workstation)
    {
      this.workstation = workstation;
    }

    /**
     * @return the tray
     */
    private String getTray()
    {
      return tray;
    }

    /**
     * @param tray the tray to set
     */
    void setTray(String tray)
    {
      this.tray = tray;
    }

    /**
     * @return the actionPlan
     */
    private boolean isActionPlan()
    {
      return actionPlan;
    }

    /**
     * @param actionPlan the actionPlan to set
     */
    void setActionPlan(boolean actionPlan)
    {
      this.actionPlan = actionPlan;
    }

    /**
     * @return the bulletins
     */
    private boolean isBulletins()
    {
      return bulletins;
    }

    /**
     * @param bulletins the bulletins to set
     */
    void setBulletins(boolean bulletins)
    {
      this.bulletins = bulletins;
    }

    /**
     * @return the checkIn
     */
    private boolean isCheckIn()
    {
      return checkIn;
    }

    /**
     * @param checkIn the checkIn to set
     */
    void setCheckIn(boolean checkIn)
    {
      this.checkIn = checkIn;
    }

    /**
     * @return the invoice
     */
    private boolean isInvoice()
    {
      return invoice;
    }

    /**
     * @param invoice the invoice to set
     */
    void setInvoice(boolean invoice)
    {
      this.invoice = invoice;
    }

    /**
     * @return the merch
     */
    private boolean isMerch()
    {
      return merch;
    }

    /**
     * @param merch the merch to set
     */
    void setMerch(boolean merch)
    {
      this.merch = merch;
    }
  }

  protected enum Report
  {
   ACTION_PLAN,
   BULLETINS,
   CHECK_IN,
   INVOICE,
   MERCH
  }
}
