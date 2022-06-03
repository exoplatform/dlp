package org.exoplatform.dlp.activity.listener;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.dlp.domain.DlpPositiveItemEntity;
import org.exoplatform.dlp.dto.DlpPositiveItem;
import org.exoplatform.dlp.service.DlpPositiveItemService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.listener.*;
import org.exoplatform.social.core.storage.api.ActivityStorage;

@Asynchronous
public class DlpActivityAttachmentListener extends Listener<Object, Object> {

  private PortalContainer        container;

  //TODO commented for test not working
//  private CachedActivityStorage  activityStorage;

  private DlpPositiveItemService dlpPositiveItemService;

  public DlpActivityAttachmentListener(DlpPositiveItemService dlpPositiveItemService,
                                       ActivityStorage activityStorage,
                                       PortalContainer container) {
    this.container = container;
    this.dlpPositiveItemService = dlpPositiveItemService;
    //TODO commented for test not working
//    if (activityStorage instanceof CachedActivityStorage) {
//      this.activityStorage = (CachedActivityStorage) activityStorage;
//    }
  }

  @Override
  public void onEvent(Event<Object, Object> event) throws Exception {
    //TODO commented for test not working
//    if (activityStorage == null) {
//      return;
//    }
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
    try {
      String attachmentId = null;
      if (event.getData() instanceof DlpPositiveItem) {
        DlpPositiveItem quarantineItem = (DlpPositiveItem) event.getData();
        attachmentId = quarantineItem.getReference();
      } else if (event.getData() instanceof DlpPositiveItemEntity) {
        DlpPositiveItemEntity quarantineItem = (DlpPositiveItemEntity) event.getData();
        if (quarantineItem == null || !StringUtils.equals(quarantineItem.getType(), "file")) {
          return;
        }
        attachmentId = quarantineItem.getReference();
      } else if (event.getData() instanceof Long) {
        long dlpItemId = (Long) event.getData();
        DlpPositiveItem quarantineItem = dlpPositiveItemService.getDlpPositiveItemById(dlpItemId);
        if (quarantineItem == null || !StringUtils.equals(quarantineItem.getType(), "file")) {
          return;
        }
        attachmentId = quarantineItem.getReference();
      } else if (event.getData() instanceof String) {
        String dlpItemReference = (String) event.getData();
        DlpPositiveItem quarantineItem = dlpPositiveItemService.getDlpPositiveItemByReference(dlpItemReference);
        if (quarantineItem == null || !StringUtils.equals(quarantineItem.getType(), "file")) {
          return;
        }
        attachmentId = quarantineItem.getReference();
      }
      if (StringUtils.isBlank(attachmentId)) {
        return;
      }
      //TODO commented for test not working
//      activityStorage.clearActivityCachedByAttachmentId(attachmentId);
    } finally {
      RequestLifeCycle.end();
    }
  }
}
