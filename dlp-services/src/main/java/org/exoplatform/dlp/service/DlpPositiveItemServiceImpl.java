package org.exoplatform.dlp.service;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.dlp.connector.DlpServiceConnector;
import org.exoplatform.dlp.dao.DlpPositiveItemDAO;
import org.exoplatform.dlp.domain.DlpPositiveItemEntity;
import org.exoplatform.dlp.dto.DlpPositiveItem;
import org.exoplatform.dlp.processor.DlpOperationProcessor;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;

public class DlpPositiveItemServiceImpl implements DlpPositiveItemService {

  private static final Log         LOG = ExoLogger.getLogger(DlpPositiveItemServiceImpl.class);

  private final DlpPositiveItemDAO dlpPositiveItemDAO;

  private DlpOperationProcessor    dlpOperationProcessor;

  private ListenerService          listenerService;

  private OrganizationService      organizationService;

  public DlpPositiveItemServiceImpl(DlpPositiveItemDAO dlpPositiveItemDAO,
                                    OrganizationService organizationService,
                                    ListenerService listenerService,
                                    DlpOperationProcessor dlpOperationProcessor) {
    this.dlpPositiveItemDAO = dlpPositiveItemDAO;
    this.organizationService = organizationService;
    this.listenerService = listenerService;
    this.dlpOperationProcessor = dlpOperationProcessor;
  }

  public DlpPositiveItem getDlpPositiveItemById(Long itemId) throws Exception {
    DlpPositiveItemEntity itemEntity = dlpPositiveItemDAO.find(itemId);
    return itemEntity != null ? fillDlpPositiveItemFromEntity(itemEntity) : null;
  }

  @Override
  public List<DlpPositiveItem> getDlpPositivesItems(int offset, int limit) throws Exception {
    List<DlpPositiveItemEntity> dlpPositiveItemEntities = dlpPositiveItemDAO.getDlpPositiveItems(offset, limit);
    return fillDlpPositiveItemsFromEntities(dlpPositiveItemEntities);
  }

  @Override
  public void addDlpPositiveItem(DlpPositiveItemEntity dlpPositiveItemEntity) {
    dlpPositiveItemDAO.create(dlpPositiveItemEntity);
    try {
      listenerService.broadcast(new Event("dlp.listener.event.detect.item", null, dlpPositiveItemEntity.getId()));
    } catch (Exception e) {
      LOG.error("Error when broadcasting dlp detect positive item", e);
    }
  }

  @Override
  public void deleteDlpPositiveItem(Long itemId) {
    DlpPositiveItemEntity dlpPositiveItemEntity = dlpPositiveItemDAO.find(itemId);
    if (dlpPositiveItemEntity != null) {
      long startTime = System.currentTimeMillis();
      dlpPositiveItemDAO.delete(dlpPositiveItemEntity);
      try {
        listenerService.broadcast(new Event("dlp.listener.event.delete.item", null, dlpPositiveItemEntity.getReference()));
      } catch (Exception e) {
        LOG.error("Error when broadcasting delete file event", e);
      }
      long endTime = System.currentTimeMillis();
      long totalTime = endTime - startTime;
      LOG.info("service={} operation={} parameters=\"fileName:{}\" status=ok " + "duration_ms={}",
               DlpOperationProcessor.DLP_FEATURE,
               "dlpPositiveItemDeletion",
               dlpPositiveItemEntity.getTitle(),
               totalTime);
    } else {
      LOG.warn("The DlpItem's {} not found.", itemId);
    }
  }

  @Override
  public DlpPositiveItem getDlpPositiveItemByReference(String itemReference) throws Exception {
    DlpPositiveItemEntity dlpPositiveItemEntity = dlpPositiveItemDAO.findDlpPositiveItemByReference(itemReference);
    return fillDlpPositiveItemFromEntity(dlpPositiveItemEntity);
  }

  @Override
  public Long getDlpPositiveItemsCount() {
    return dlpPositiveItemDAO.count();
  }

  private List<DlpPositiveItem> fillDlpPositiveItemsFromEntities(List<DlpPositiveItemEntity> dlpPositiveItemEntities) throws Exception {
    List<DlpPositiveItem> dlpPositiveItems = new ArrayList<>();
    for (DlpPositiveItemEntity dlpPositiveItemEntity : dlpPositiveItemEntities) {
      DlpPositiveItem dlpPositiveItem = fillDlpPositiveItemFromEntity(dlpPositiveItemEntity);
      dlpPositiveItems.add(dlpPositiveItem);
    }
    return dlpPositiveItems;
  }

  @Override
  public void restoreDlpPositiveItem(Long itemId) {
    try {
      DlpPositiveItemEntity dlpPositiveItemEntity = dlpPositiveItemDAO.find(itemId);
      if (dlpPositiveItemEntity != null) {
        dlpPositiveItemDAO.delete(dlpPositiveItemEntity);
        listenerService.broadcast(new Event("dlp.listener.event.restore.item", null, dlpPositiveItemEntity));
      } else {
        LOG.warn("The DlpItem's {} not found.", itemId);
      }
    } catch (Exception e) {
      LOG.error("Error when broadcasting restore file event", e);
    }
  }

  private DlpPositiveItem fillDlpPositiveItemFromEntity(DlpPositiveItemEntity dlpPositiveItemEntity) throws Exception {
    DlpPositiveItem dlpPositiveItem = new DlpPositiveItem();
    DlpServiceConnector dlpServiceConnector = dlpOperationProcessor.getConnectors()
                                                                                         .get(dlpPositiveItemEntity.getType());
    dlpPositiveItem.setId(dlpPositiveItemEntity.getId());
    dlpPositiveItem.setType(dlpPositiveItemEntity.getType());
    dlpPositiveItem.setKeywords(dlpPositiveItemEntity.getKeywords());
    dlpPositiveItem.setAuthor(dlpPositiveItemEntity.getAuthor());
    dlpPositiveItem.setReference(dlpPositiveItemEntity.getReference());
    User user = organizationService.getUserHandler().findUserByName(dlpPositiveItemEntity.getAuthor());
    if (user != null) {
      dlpPositiveItem.setAuthorDisplayName(user.getDisplayName());
      dlpPositiveItem.setExternal(dlpServiceConnector != null
          && dlpServiceConnector.checkExternal(dlpPositiveItemEntity.getAuthor()));
    }
    dlpPositiveItem.setTitle(dlpPositiveItemEntity.getTitle());
    dlpPositiveItem.setItemUrl(dlpServiceConnector != null ? dlpServiceConnector.getItemUrl(dlpPositiveItemEntity.getReference())
                                                           : "");
    dlpPositiveItem.setDetectionDate(dlpPositiveItemEntity.getDetectionDate().getTimeInMillis());
    return dlpPositiveItem;
  }
}
