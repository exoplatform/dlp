package org.exoplatform.dlp.dao;

import java.util.List;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.dlp.domain.DlpOperation;

public interface DlpOperationDAO extends GenericDAO<DlpOperation, Long> {

  List<DlpOperation> findAllFirstWithOffset(int offset, int limit);

  List<DlpOperation> findByEntityIdAndType(String entityId, String entityType);

  int deleteByEntityId(String entityId);
}
