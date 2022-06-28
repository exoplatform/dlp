package org.exoplatform.dlp.notification.plugin;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.dlp.dto.DlpPositiveItem;
import org.exoplatform.dlp.service.DlpPositiveItemService;
import org.exoplatform.services.idgenerator.IDGeneratorService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.management.*" })
@PrepareForTest({ CommonsUtils.class, PluginKey.class, CommonsUtils.class, ExoContainerContext.class })
public class DlpUserDetectedItemPluginTest {

  @Mock
  private DlpPositiveItemService    dlpPositiveItemService;

  @Mock
  private InitParams                initParams;

  private DlpUserDetectedItemPlugin dlpUserDetectedItemPlugin;

  @Before
  public void setUp() throws Exception {
    this.dlpUserDetectedItemPlugin = new DlpUserDetectedItemPlugin(initParams, dlpPositiveItemService);
    PowerMockito.mockStatic(CommonsUtils.class);
    PowerMockito.mockStatic(ExoContainerContext.class);
    when(ExoContainerContext.getService(IDGeneratorService.class)).thenReturn(null);
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
