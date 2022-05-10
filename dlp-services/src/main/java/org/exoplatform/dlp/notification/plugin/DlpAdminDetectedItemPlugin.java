package org.exoplatform.dlp.notification.plugin;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.dlp.dto.DlpPositiveItem;
import org.exoplatform.dlp.service.DlpPositiveItemService;
import org.exoplatform.dlp.utils.DlpUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DlpAdminDetectedItemPlugin extends BaseNotificationPlugin {

  public static final String     ID     = "DlpAdminDetectedItemPlugin";

  private static final Log       LOGGER = ExoLogger.getExoLogger(DlpAdminDetectedItemPlugin.class);

  private DlpPositiveItemService dlpPositiveItemService;

  private OrganizationService    organizationService;

  private UserACL                userACL;

  public DlpAdminDetectedItemPlugin(InitParams initParams,
                                    DlpPositiveItemService dlpPositiveItemService,
                                    OrganizationService organizationService,
                                    UserACL userACL) {
    super(initParams);
    this.dlpPositiveItemService = dlpPositiveItemService;
    this.organizationService = organizationService;
    this.userACL = userACL;
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
      return NotificationInfo.instance().key(getId()).with("itemTitle", dlpPositiveItem.getTitle()).to(getRecipients());
    } catch (Exception e) {
      ctx.setException(e);
    }

    return null;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    return true;
  }

  private List<String> getRecipients() {
    // Get DLP members group
    List<String> members = new ArrayList<>();
    try {
      ListAccess<User> dlpMembersAccess = organizationService.getUserHandler().findUsersByGroupId(userACL.getDlpGroups());
      int totalAdminGroupMembersSize = dlpMembersAccess.getSize();
      User[] users = dlpMembersAccess.load(0, totalAdminGroupMembersSize);
      return Arrays.stream(users).map(User::getUserName).collect(Collectors.toList());
    } catch (Exception e) {
      LOGGER.error("Error when getting DLP group members");
    }
    return members;
  }
}
