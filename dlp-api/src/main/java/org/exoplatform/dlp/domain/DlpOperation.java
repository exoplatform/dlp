package org.exoplatform.dlp.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.exoplatform.commons.api.persistence.ExoEntity;

/**
 * Entity for Dlp operation.
 */
@Entity
@ExoEntity
@Table(name = "DLP_QUEUE")
@NamedQuery(name = "DlpOperation.findByEntityIdAndType", query = "SELECT q FROM DlpOperation q WHERE q.entityId = :entityId and q.entityType = :entityType")
@NamedQuery(name = "DlpOperation.findAllFirstWithOffset", query = "SELECT q FROM DlpOperation q ORDER BY q.id")
@NamedQuery(name = "DlpOperation.deleteByEntityId", query = "DELETE FROM DlpOperation q WHERE q.entityId = :entityId")
public class DlpOperation implements Serializable {

  @Id
  @SequenceGenerator(name = "SEQ_DLP_QUEUE_ID", sequenceName = "SEQ_DLP_QUEUE_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_DLP_QUEUE_ID")
  @Column(name = "OPERATION_ID")
  private Long   id;

  @Column(name = "ENTITY_TYPE")
  private String entityType;

  @Column(name = "ENTITY_ID")
  private String entityId;

  // The timestamp is managed by the DB and cannot be set or get
  // It's only use for querying timestamp based dlp operations
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "OPERATION_TIMESTAMP", insertable = false, updatable = false)
  private Date   timestamp;

  public DlpOperation() {
  }

  public DlpOperation(String entityId, String entityType) {
    this.entityId = entityId;
    this.entityType = entityType;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    DlpOperation that = (DlpOperation) o;

    return Objects.equals(entityId, that.entityId)
            && Objects.equals(entityType, that.entityType)
            && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (entityType != null ? entityType.hashCode() : 0);
    result = 31 * result + (entityId != null ? entityId.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "DlpOperation{" + "id=" + id + ", entityType='" + entityType + '\'' + ", entityId='" + entityId + '\'' + ", timestamp="
        + timestamp + '}';
  }
}
