package org.exoplatform.dlp.notification.plugin;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.dlp.dto.DlpPositiveItem;
import org.exoplatform.dlp.service.DlpPositiveItemService;
import org.exoplatform.dlp.utils.DlpUtils;
import org.exoplatform.container.xml.InitParams;

public class DlpUserDetectedItemPlugin extends BaseNotificationPlugin {

  public static final String     ID = "DlpUserDetectedItemPlugin";

  private DlpPositiveItemService dlpPositiveItemService;

  public DlpUserDetectedItemPlugin(InitParams initParams, DlpPositiveItemService dlpPositiveItemService) {
    super(initParams);
    this.dlpPositiveItemService = dlpPositiveItemService;
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    long dlpItemId = ctx.value(DlpUtils.DLP_DETECTED_ITEM_ID);
    DlpPositiveItem dlpPositiveItem = null;
    try {
      dlpPositiveItem = dlpPositiveItemService.getDlpPositiveItemById(dlpItemId);
      return NotificationInfo.instance()
                             .key(getId())
                             .with("itemTitle", dlpPositiveItem.getTitle())
                             .with("itemAuthor", dlpPositiveItem.getAuthorDisplayName())
                             .to(dlpPositiveItem.getAuthor());
    } catch (Exception e) {
      ctx.setException(e);
    }

    return null;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    return true;
  }
}
