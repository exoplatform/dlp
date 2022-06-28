package org.exoplatform.dlp.notification.plugin;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.dlp.utils.DlpUtils;

public class DlpUserRestoredItemPlugin extends BaseNotificationPlugin {

  public static final String ID = "DlpUserRestoredItemPlugin";

  public DlpUserRestoredItemPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    String dlpItemTitle = ctx.value(DlpUtils.DLP_RESTORED_ITEM_TITLE);
    String dlpItemAuthor = ctx.value(DlpUtils.DLP_RESTORED_ITEM_AUTHOR);
    String dlpItemReference = ctx.value(DlpUtils.DLP_RESTORED_ITEM_REFERENCE);
    return NotificationInfo.instance()
                           .key(getId())
                           .with("itemTitle", dlpItemTitle)
                           .with("itemReference", dlpItemReference)
                           .to(dlpItemAuthor);
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    return true;
  }
}
