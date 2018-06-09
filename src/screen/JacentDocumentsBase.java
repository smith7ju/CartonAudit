/******************************************************************************
*
* Filename:     JacentDocumentsBase.java
* Description:
*
* (c) COPYRIGHT QC Software, LLC 2018 All Rights Reserved
* No part of this copyrighted work may be reproduced, modified, or distributed
* in any form or by any means without the prior written permission of QC
* Software, LLC.
*
* $Id: JacentDocumentsBase.java 1084 2018-01-29 19:39:26Z jas $
* Notes:
*
*****************************************************************************/
package com.qcsoftware.jacent.ui.cartonaudit;

import java.sql.SQLException;

import com.qcsoftware.jacent.tables.LamiOrderHeaderBase;

import Library.Database.DbColumnInfo;
import Library.Database.DbConnect;
import Library.Database.DbException;

/**
 * @author jas
 *
 */
public class JacentDocumentsBase extends Packages.DbBase.BaseTable
{
  @SuppressWarnings("unused")
  private static final String svnid = "$Id: JacentDocumentsBase.java 1084 2018-01-29 19:39:26Z jas $";
  
  protected DbColumnInfo document,
      moveableUnitId,
      idx,
      workOrderId,
      printsNeeded,
      printCount,
      operator,
      printTs;

  public JacentDocumentsBase(DbConnect conn) throws SQLException
  {
    super(conn, "JACENTDOCUMENTS");
    
    document = getColumnInfo("document");
    moveableUnitId = getColumnInfo("moveableUnitId");
    idx = getColumnInfo("idx");
    workOrderId = getColumnInfo("workOrderId");
    printsNeeded = getColumnInfo("printsNeeded");
    printCount = getColumnInfo("printCount");
    operator = getColumnInfo("operator");
    printTs = getColumnInfo("printTs");
  }
  
  public JacentDocumentsBase(DbConnect conn, String document, String moveableUnitId) throws SQLException, DbException
  {
    this(conn);
    
    this.document.setValue(document);
    this.moveableUnitId.setValue(moveableUnitId);
  }

  /**
   * Gets the value of document.
   *
   * @return value of document.
   */
  public java.lang.String getDocument()
  {
    return(document.getValue(java.lang.String.class));
  }
  
  /**
   * Gets the value of moveableUnitId.
   *
   * @return value of moveableUnitId.
   */
  public java.lang.String getMoveableUnitId()
  {
    return(moveableUnitId.getValue(java.lang.String.class));
  }
  
  /**
   * Gets the value of idx.
   *
   * @return value of idx.
   */
  public java.lang.Integer getidx()
  {
    return(idx.getValue(java.lang.Integer.class));
  }
  
  /**
   * Gets the value of workOrderId.
   *
   * @return value of workOrderId.
   */
  public java.lang.String getWorkOrderId()
  {
    return(workOrderId.getValue(java.lang.String.class));
  }
  
  /**
   * Gets the value of printsNeeded.
   *
   * @return value of printsNeeded.
   */
  public java.lang.Integer getPrintsNeeded()
  {
    return(printsNeeded.getValue(java.lang.Integer.class));
  }
  
  /**
   * Gets the value of printCount.
   *
   * @return value of printCount.
   */
  public java.lang.Integer getPrintCount()
  {
    return(printCount.getValue(java.lang.Integer.class));
  }
  
  /**
   * Gets the value of operator.
   *
   * @return value of operator.
   */
  public java.lang.String getOperator()
  {
    return(operator.getValue(java.lang.String.class));
  }
  
  /**
   * Gets the value of printTs.
   *
   * @return value of printTs.
   */
  public java.sql.Timestamp getPrintTs()
  {
    return(printTs.getValue(java.sql.Timestamp.class));
  }
  
  /**
   * Sets the value of document (required column value).
   *
   * @param value the document value.
   */
  public void setDocument(java.lang.String value)
  {
    this.document.setValue(value);
  }
  
  /**
   * Sets the value of moveableUnitId (required column value).
   *
   * @param value the moveableUnitId value.
   */
  public void setMoveableUnitId(java.lang.String value)
  {
    this.moveableUnitId.setValue(value);
  }
  
  /**
   * Sets the value of idx (required column value).
   *
   * @param value the idx value.
   */
  public void setIdx(java.lang.Integer value)
  {
    this.idx.setValue(value);
  }
  
  /**
   * Sets the value of workOrderId (optional column value).
   *
   * @param value the workOrderId value.
   */
  public void setWorkOrderId(java.lang.String value)
  {
    this.workOrderId.setValue(value);
  }
  
  /**
   * Sets the value of printsNeeded (optional column value).
   *
   * @param value the printsNeeded value.
   */
  public void setPrintsNeeded(java.lang.Integer value)
  {
    this.printsNeeded.setValue(value);
  }
  
  /**
   * Sets the value of printCount (optional column value).
   *
   * @param value the printCount value.
   */
  public void setPrintCount(java.lang.Integer value)
  {
    this.printCount.setValue(value);
  }
  
  /**
   * Sets the value of operator (optional column value).
   *
   * @param value the operator value.
   */
  public void setOperator(java.lang.String value)
  {
    this.operator.setValue(value);
  }
  
  /**
   * Sets the value of printTs (optional column value).
   *
   * @param value the printTs value.
   */
  public void setPrintTs(java.sql.Timestamp value)
  {
    this.printTs.setValue(value);
  }
  
  /**
   * Creates a copy of the table object.
   *
   * @return copy of the table object.
   */
  public JacentDocumentsBase clone()
  {
    JacentDocumentsBase cloned;
    
    cloned = (JacentDocumentsBase)super.clone();
    
    cloned.document = cloned.getColumnInfo("document");
    cloned.moveableUnitId = cloned.getColumnInfo("moveableUnitId");
    cloned.idx = cloned.getColumnInfo("idx");
    cloned.workOrderId = cloned.getColumnInfo("workOrderId");
    cloned.printsNeeded = cloned.getColumnInfo("printsNeeded");
    cloned.printCount = cloned.getColumnInfo("printCount");
    cloned.operator = cloned.getColumnInfo("operator");
    cloned.printTs = cloned.getColumnInfo("printTs");
    
    return cloned;
  }
}
