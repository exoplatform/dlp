package org.exoplatform.dlp.notification.plugin;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockStatic;

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
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.idgenerator.IDGeneratorService;

@RunWith(MockitoJUnitRunner.class)
public class DlpUserRestoredItemPluginTest {

  private static final MockedStatic<ExoContainerContext> EXO_CONTAINER_CONTEXT = mockStatic(ExoContainerContext.class);

  private static final MockedStatic<CommonsUtils>        COMMONS_UTILS         = mockStatic(CommonsUtils.class);

  @Mock
  private InitParams                initParams;

  private DlpUserRestoredItemPlugin dlpUserRestoredItemPlugin;

  @AfterClass
  public static void afterRunBare() throws Exception { // NOSONAR
    EXO_CONTAINER_CONTEXT.close();
    COMMONS_UTILS.close();
  }

  @Before
  public void setUp() throws Exception {
    dlpUserRestoredItemPlugin = new DlpUserRestoredItemPlugin(initParams);
    EXO_CONTAINER_CONTEXT.when(() -> ExoContainerContext.getService(IDGeneratorService.class)).thenReturn(null);
  }

  @Test
  public void makeNotification() {
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.append(new ArgumentLiteral<>(String.class, "dlp_restored_item_reference"), "1");
    ctx.append(new ArgumentLiteral<>(String.class, "dlp_restored_item_author"), "author");
    ctx.append(new ArgumentLiteral<>(String.class, "dlp_restored_item_title"), "file.docx");

    NotificationInfo notificationInfo = dlpUserRestoredItemPlugin.makeNotification(ctx);
    assertEquals("file.docx", notificationInfo.getValueOwnerParameter("itemTitle"));
    assertEquals("1", notificationInfo.getValueOwnerParameter("itemReference"));
    assertEquals("author", notificationInfo.getTo());
  }
}
