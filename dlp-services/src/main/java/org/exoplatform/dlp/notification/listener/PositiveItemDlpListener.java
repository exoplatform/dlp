package org.exoplatform.dlp.notification.listener;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.dlp.domain.DlpPositiveItemEntity;
import org.exoplatform.dlp.service.DlpPositiveItemService;
import org.exoplatform.dlp.utils.DlpUtils;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.dlp.notification.plugin.DlpAdminDetectedItemPlugin;
import org.exoplatform.dlp.notification.plugin.DlpUserDetectedItemPlugin;
import org.exoplatform.dlp.notification.plugin.DlpUserRestoredItemPlugin;

public class PositiveItemDlpListener extends Listener<DlpPositiveItemService, Object> {

  public static final String DLP_DETECT_ITEM_EVENT  = "dlp.listener.event.detect.item";

  public static final String DLP_RESTORE_ITEM_EVENT = "dlp.listener.event.restore.item";

  /**
   * dlp positive item detected/restored
   *
   * @param event
   */
  @Override
  public void onEvent(Event<DlpPositiveItemService, Object> event) {
    if (DLP_DETECT_ITEM_EVENT.equals(event.getEventName())) {
      NotificationContext ctx = NotificationContextImpl.cloneInstance().append(DlpUtils.DLP_DETECTED_ITEM_ID, event.getData());
      ctx.getNotificationExecutor()
         .with(ctx.makeCommand(PluginKey.key(DlpUserDetectedItemPlugin.ID)))
         .with(ctx.makeCommand(PluginKey.key(DlpAdminDetectedItemPlugin.ID)))
         .execute(ctx);
    } else if (DLP_RESTORE_ITEM_EVENT.equals(event.getEventName())) {
      NotificationContext ctx = NotificationContextImpl.cloneInstance()
                                                       .append(DlpUtils.DLP_RESTORED_ITEM_TITLE,
                                                               ((DlpPositiveItemEntity) event.getData()).getTitle())
                                                       .append(DlpUtils.DLP_RESTORED_ITEM_AUTHOR,
                                                               ((DlpPositiveItemEntity) event.getData()).getAuthor())
                                                       .append(DlpUtils.DLP_RESTORED_ITEM_REFERENCE,
                                                               ((DlpPositiveItemEntity) event.getData()).getReference());
      ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(DlpUserRestoredItemPlugin.ID))).execute(ctx);
    }
  }
}
