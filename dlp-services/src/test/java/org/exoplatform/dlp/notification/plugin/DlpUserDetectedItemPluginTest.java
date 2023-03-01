package org.exoplatform.dlp.notification.plugin;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

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
import org.exoplatform.dlp.dto.DlpPositiveItem;
import org.exoplatform.dlp.service.DlpPositiveItemService;
import org.exoplatform.services.idgenerator.IDGeneratorService;

@RunWith(MockitoJUnitRunner.class)
public class DlpUserDetectedItemPluginTest {

  private static final MockedStatic<ExoContainerContext> EXO_CONTAINER_CONTEXT = mockStatic(ExoContainerContext.class);

  private static final MockedStatic<CommonsUtils>        COMMONS_UTILS         = mockStatic(CommonsUtils.class);

  @Mock
  private DlpPositiveItemService    dlpPositiveItemService;

  @Mock
  private InitParams                initParams;

  private DlpUserDetectedItemPlugin dlpUserDetectedItemPlugin;

  @AfterClass
  public static void afterRunBare() throws Exception { // NOSONAR
    EXO_CONTAINER_CONTEXT.close();
    COMMONS_UTILS.close();
  }

  @Before
  public void setUp() throws Exception {
    this.dlpUserDetectedItemPlugin = new DlpUserDetectedItemPlugin(initParams, dlpPositiveItemService);
    EXO_CONTAINER_CONTEXT.when(() -> ExoContainerContext.getService(IDGeneratorService.class)).thenReturn(null);
  }

  @Test
  public void makeNotification() throws Exception {
    DlpPositiveItem dlpPositiveItem = new DlpPositiveItem();
    dlpPositiveItem.setAuthor("author");
    dlpPositiveItem.setTitle("file.docx");
    dlpPositiveItem.setAuthorDisplayName("user author");
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.append(new ArgumentLiteral<>(Long.class, "dlp_detected_item_id"), 1L);
    when(dlpPositiveItemService.getDlpPositiveItemById(1L)).thenReturn(dlpPositiveItem);

    NotificationInfo notificationInfo = this.dlpUserDetectedItemPlugin.makeNotification(ctx);

    assertEquals("file.docx", notificationInfo.getValueOwnerParameter("itemTitle"));
    assertEquals("user author", notificationInfo.getValueOwnerParameter("itemAuthor"));
    assertEquals("author", notificationInfo.getTo());
  }
}
