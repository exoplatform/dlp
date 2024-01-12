package org.exoplatform.dlp.dao;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.dlp.domain.RestoredDlpItemEntity;

public class RestoredDlpItemDAOImpl extends GenericDAOJPAImpl<RestoredDlpItemEntity, Long> implements RestoredDlpItemDAO {

  @Override
  public RestoredDlpItemEntity findRestoredDlpItemByReference(String itemReference) {
    TypedQuery<RestoredDlpItemEntity> query = getEntityManager()
        .createNamedQuery("RestoredDlpItemEntity.findRestoredDlpItemByReference", RestoredDlpItemEntity.class)
        .setParameter("itemReference", itemReference);
    try {
      return query.getSingleResult();
    } catch (NoResultException ex) {
      return null;
    }
  }
}
