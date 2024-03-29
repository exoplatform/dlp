package org.exoplatform.dlp.queue.impl;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.dlp.dao.DlpOperationDAO;
import org.exoplatform.dlp.domain.DlpOperation;
import org.exoplatform.dlp.queue.QueueDlpService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class QueueDlpServiceImpl implements QueueDlpService {

  private static final Log      LOGGER = ExoLogger.getExoLogger(QueueDlpServiceImpl.class);

  private final DlpOperationDAO dlpOperationDAO;

  public QueueDlpServiceImpl(DlpOperationDAO dlpOperationDAO) {
    this.dlpOperationDAO = dlpOperationDAO;
  }

  @Override
  public void addToQueue(String connectorName, String id) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("Entity id is null");
    }
    dlpOperationDAO.create(getDlpOperation(connectorName, id));
    LOGGER.debug("Entity with id: {} and connector: {} has been added to Dlp Queue", id, connectorName);
  }

  @Override
  public void removeAllItemFromQueue(String id) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("Entity id is null");
    }
    dlpOperationDAO.deleteByEntityId(id);
    LOGGER.debug("All entries for item id [} have been deleted from DLP queue", id);

  }

  private DlpOperation getDlpOperation(String connector, String entityId) {
    DlpOperation dlpOperation = new DlpOperation();
    dlpOperation.setEntityType(connector);
    if (entityId != null) {
      dlpOperation.setEntityId(entityId);
    }
    return dlpOperation;
  }
}
