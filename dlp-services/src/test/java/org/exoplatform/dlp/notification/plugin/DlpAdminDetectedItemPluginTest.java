package org.exoplatform.dlp.notification.plugin;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.dlp.dto.DlpPositiveItem;
import org.exoplatform.dlp.service.DlpPositiveItemService;
import org.exoplatform.services.idgenerator.IDGeneratorService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;

@RunWith(MockitoJUnitRunner.class)
public class DlpAdminDetectedItemPluginTest {

  private static final MockedStatic<ExoContainerContext> EXO_CONTAINER_CONTEXT = mockStatic(ExoContainerContext.class);

  private static final MockedStatic<CommonsUtils>        COMMONS_UTILS         = mockStatic(CommonsUtils.class);

  @Mock
  private InitParams                 initParams;

  @Mock
  private DlpPositiveItemService     dlpPositiveItemService;

  @Mock
  private OrganizationService        organizationService;

  private DlpAdminDetectedItemPlugin dlpAdminDetectedItemPlugin;

  @AfterClass
  public static void afterRunBare() throws Exception { // NOSONAR
    EXO_CONTAINER_CONTEXT.close();
    COMMONS_UTILS.close();
  }

  @Before
  public void setUp() throws Exception {
    dlpAdminDetectedItemPlugin = new DlpAdminDetectedItemPlugin(initParams, dlpPositiveItemService, organizationService);
    EXO_CONTAINER_CONTEXT.when(() -> ExoContainerContext.getService(IDGeneratorService.class)).thenReturn(null);
  }

  @Test
  public void makeNotification() throws Exception {
    DlpPositiveItem dlpPositiveItem = new DlpPositiveItem();
    dlpPositiveItem.setAuthor("author");
    dlpPositiveItem.setTitle("file.docx");
    dlpPositiveItem.setAuthorDisplayName("user author");
    when(dlpPositiveItemService.getDlpPositiveItemById(1L)).thenReturn(dlpPositiveItem);
    UserHandler userHandler = mock(UserHandler.class);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    @SuppressWarnings("unchecked")
    ListAccess<User> dlpMembersAccess = (ListAccess<User>) mock(ListAccess.class);
    when(dlpMembersAccess.getSize()).thenReturn(1);
    User user = mock(User.class);
    when(user.getUserName()).thenReturn("userName");
    when(userHandler.findUsersByGroupId("/platform/dlp")).thenReturn(dlpMembersAccess);
    User[] usersArray = { user };
    when(dlpMembersAccess.load(0, dlpMembersAccess.getSize())).thenReturn(usersArray);
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.append(new ArgumentLiteral<>(Long.class, "dlp_detected_item_id"), 1L);

    NotificationInfo notificationInfo = dlpAdminDetectedItemPlugin.makeNotification(ctx);
    assertEquals("file.docx", notificationInfo.getValueOwnerParameter("itemTitle"));
    assertEquals(List.of("userName"), notificationInfo.getSendToUserIds());
  }
}
