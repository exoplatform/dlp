package org.exoplatform.dlp.domain;

import java.util.Calendar;

import io.meeds.common.persistence.PortableSequence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity(name = "RestoredDlpItemEntity")
@Table(name = "RESTORED_DLP_ITEMS")
@NamedQuery(name = "RestoredDlpItemEntity.findRestoredDlpItemByReference", query = "SELECT q FROM RestoredDlpItemEntity q WHERE q.reference = :itemReference")
public class RestoredDlpItemEntity {

  @Id
  @PortableSequence(name = "SEQ_RESTORED_DLP_ITEMS_ID")
  @Column(name = "RESTORED_ITEM_ID")
  private Long     id;

  @Column(name = "RESTORED_ITEM_REFERENCE")
  private String   reference;

  @Column(name = "RESTORED_DETECTION_DATE")
  private Calendar detectionDate;

  public Long getId() {
    return id;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public Calendar getDetectionDate() {
    return detectionDate;
  }

  public void setDetectionDate(Calendar detectionDate) {
    this.detectionDate = detectionDate;
  }
}
