package org.exoplatform.dlp.dao;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.dlp.domain.RestoredDlpItemEntity;

public interface RestoredDlpItemDAO extends GenericDAO<RestoredDlpItemEntity, Long> {

  RestoredDlpItemEntity findRestoredDlpItemByReference(String itemReference);
}
