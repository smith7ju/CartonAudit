/******************************************************************************
*
* Filename:     FrmCartonAudit.java
* Description:
*
* (c) COPYRIGHT QC Software, LLC 2018 All Rights Reserved
* No part of this copyrighted work may be reproduced, modified, or distributed
* in any form or by any means without the prior written permission of QC
* Software, LLC.
*
* $Id: CartonAuditFrame.java 1144 2018-03-29 13:29:37Z jas $
* Notes:
*
*****************************************************************************/
package com.qcsoftware.jacent.ui.cartonaudit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Hashtable;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.w3c.dom.Element;

import Gui.Basic.QInternalFrame;
import Library.QcSockets.TcpipNet.TcpipNetClient;
import Library.QcSockets.TcpipNet.TcpipNetConnect;
import Products.LabelMgr.LabelMgrMsg;

/**
 * @author jas
 *
 */
public class CartonAuditFrame extends QInternalFrame
{
  @SuppressWarnings("unused")
  private static final String        svnid = "$Id: CartonAuditFrame.java 1144 2018-03-29 13:29:37Z jas $";
  private TcpipNetConnect.Connection labelMgrConn;

  public CartonAuditFrame()
  {
    super("Carton Audit");
  }

  public void init(Hashtable<String, String> params)
  {
    super.init(params);
    setContentPane(new CartonAuditPanel(getDbConnect()));
    pack();

    labelMgrConn = TcpipNetClient.create(params.getOrDefault("labelMgr", LabelMgrMsg.LABEL_MGR), null);

    ((CartonAuditPanel) getContentPane()).getController()
        .setEqTypeLabelPrinter(params.getOrDefault("labelPrinterEquipmentType", "ZPLLabelPrinter"));

    ((CartonAuditPanel) getContentPane()).getController()
        .setEqTypeSsccLabelPrinter(params.getOrDefault("ssccPrinterEquipmentType", "ZPLLabelPrinter"));
    
    ((CartonAuditPanel) getContentPane()).getController()
    .setEqTypeGenericLabelPrinter(params.getOrDefault("genericLabelPrinterEquipmentType", "LabelPrinter"));

    ((CartonAuditPanel) getContentPane()).getController()
        .setAsnLabelName(params.getOrDefault("asnLabelName", "ASN"));
    
    ((CartonAuditPanel) getContentPane()).getController()
    .setAsnLabelName(params.getOrDefault("ediLabelName", "EDI"));

    ((CartonAuditPanel) getContentPane()).getController()
        .setSsccLabelName(params.getOrDefault("ssccLabelName", "SSCC"));

    ((CartonAuditPanel) getContentPane()).getController()
        .setReportPreviewEnabled(Boolean.parseBoolean(params.getOrDefault("reportPreviewEnabled", "true")));

    ((CartonAuditPanel) getContentPane()).getController()
        .setPickSheetReportName(params.getOrDefault("pickSheetReportName", "pickSheetFlexReport.xml"));

    ((CartonAuditPanel) getContentPane()).getController()
        .setInvoiceReportName(params.getOrDefault("invoiceReportName", "invoiceFlexReport.xml"));

    ((CartonAuditPanel) getContentPane()).getController()
        .setCheckinReportName(params.getOrDefault("checkinReportName", "invoiceCheckInSheetFlexReport.xml"));

    ((CartonAuditPanel) getContentPane()).getController()
        .setMerchandiserReportName(params.getOrDefault("merchandiserReportName", "merchandiserReferenceSheet.xml"));

    ((CartonAuditPanel) getContentPane()).getController()
        .setEnableActionPlan(Boolean.parseBoolean(params.getOrDefault("enableActionPlan", "false")));

    ((CartonAuditPanel) getContentPane()).getController()
        .setEnablePlanArchive(Boolean.parseBoolean(params.getOrDefault("enablePlanArchive", "false")));

    ((CartonAuditPanel) getContentPane()).getController()
        .setPdfDir(params.getOrDefault("pdfDir", "Q:\\Projects\\Jacent\\System\\Ftp\\Incoming"));

    ((CartonAuditPanel) getContentPane()).getController()
        .setPdfArchiveDir(params.getOrDefault("pdfArchiveDir", "Q:\\Projects\\Jacent\\System\\Ftp\\Incoming\\Archive"));

    ((CartonAuditPanel) getContentPane()).getController().setLabelMgrConn(labelMgrConn);
    
    ((CartonAuditPanel) getContentPane()).getController()
        .setLabelMgr(params.getOrDefault("labelMgr", LabelMgrMsg.LABEL_MGR));

    ((CartonAuditPanel) getContentPane()).getController().initLabelPrinterNames(getDbConnect());
  }

  @Override
  public void freeAll()
  {
    super.freeAll();
    labelMgrConn.logoff(true);
  }
}
