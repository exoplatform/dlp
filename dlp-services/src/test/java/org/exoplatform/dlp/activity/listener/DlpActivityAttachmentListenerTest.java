package org.exoplatform.dlp.activity.listener;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.dlp.domain.DlpPositiveItemEntity;
import org.exoplatform.dlp.dto.DlpPositiveItem;
import org.exoplatform.dlp.service.DlpPositiveItemService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.social.core.storage.cache.CachedActivityStorage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DlpActivityAttachmentListenerTest {

  private PortalContainer               container;

  @Mock
  private DlpPositiveItemService        dlpPositiveItemService;

  private DlpActivityAttachmentListener dlpActivityAttachmentListener;

  @Mock
  private CachedActivityStorage         activityStorage;

  @Before
  public void setUp() {
    container = PortalContainer.getInstance();
    CachedActivityStorage cachedActivityStorage = container.getComponentInstanceOfType(CachedActivityStorage.class);
    assertNotNull(cachedActivityStorage);
    dlpActivityAttachmentListener = new DlpActivityAttachmentListener(dlpPositiveItemService, activityStorage, container);
  }

  @Test
  public void onEvent() throws Exception {
    DlpPositiveItem dlpPositiveItem = new DlpPositiveItem();
    dlpPositiveItem.setReference("12zeddzed2ze25eedzed");
    dlpPositiveItem.setType("file");
    Event<Object, Object> event = new Event<>("event_name", null, dlpPositiveItem);
    dlpActivityAttachmentListener.onEvent(event);
    verify(activityStorage, times(1)).clearActivityCachedByAttachmentId(dlpPositiveItem.getReference());

    DlpPositiveItemEntity dlpPositiveItemEntity = new DlpPositiveItemEntity();
    dlpPositiveItemEntity.setReference("12zeddzed2ze25eedzed");
    dlpPositiveItemEntity.setType("file");
    Event<Object, Object> event1 = new Event<>("event_name", null, dlpPositiveItemEntity);
    dlpActivityAttachmentListener.onEvent(event1);
    verify(activityStorage, times(2)).clearActivityCachedByAttachmentId(dlpPositiveItemEntity.getReference());

    when(dlpPositiveItemService.getDlpPositiveItemById(1L)).thenReturn(dlpPositiveItem);
    when(dlpPositiveItemService.getDlpPositiveItemByReference("12zeddzed2ze25eedzed")).thenReturn(dlpPositiveItem);
    Event<Object, Object> event2 = new Event<>("event_name", null, 1L);
    dlpActivityAttachmentListener.onEvent(event2);
    verify(activityStorage, times(3)).clearActivityCachedByAttachmentId(dlpPositiveItem.getReference());

    Event<Object, Object> event3 = new Event<>("event_name", null, "12zeddzed2ze25eedzed");
    dlpActivityAttachmentListener.onEvent(event3);
    verify(activityStorage, times(4)).clearActivityCachedByAttachmentId(dlpPositiveItem.getReference());
  }
}
