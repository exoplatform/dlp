package org.exoplatform.dlp.domain;

import org.exoplatform.commons.api.persistence.ExoEntity;

import jakarta.persistence.*;
import java.util.Calendar;

/**
 * Entity for restored Dlp item.
 */
@Entity(name = "RestoredDlpItemEntity")
@ExoEntity
@Table(name = "RESTORED_DLP_ITEMS")
@NamedQuery(name = "RestoredDlpItemEntity.findRestoredDlpItemByReference", query = "SELECT q FROM RestoredDlpItemEntity q WHERE q.reference = :itemReference")

public class RestoredDlpItemEntity {

  @Id
  @SequenceGenerator(name = "SEQ_RESTORED_DLP_ITEMS_ID", sequenceName = "SEQ_RESTORED_DLP_ITEMS_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_RESTORED_DLP_ITEMS_ID")
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
