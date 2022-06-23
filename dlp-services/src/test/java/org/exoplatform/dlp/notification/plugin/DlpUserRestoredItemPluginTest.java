package org.exoplatform.dlp.notification.plugin;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
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
public class DlpUserRestoredItemPluginTest {

  @Mock
  private InitParams                initParams;

  private DlpUserRestoredItemPlugin dlpUserRestoredItemPlugin;

  @Before
  public void setUp() throws Exception {
    dlpUserRestoredItemPlugin = new DlpUserRestoredItemPlugin(initParams);
    PowerMockito.mockStatic(CommonsUtils.class);
    PowerMockito.mockStatic(ExoContainerContext.class);
    when(ExoContainerContext.getService(IDGeneratorService.class)).thenReturn(null);
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
