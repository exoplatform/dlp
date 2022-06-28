package org.exoplatform.dlp.service;

import static org.junit.Assert.assertNotNull;

import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.dlp.dao.RestoredDlpItemDAO;
import org.exoplatform.dlp.domain.RestoredDlpItemEntity;
import org.exoplatform.dlp.dto.RestoredDlpItem;

@RunWith(MockitoJUnitRunner.class)
public class RestoredDlpItemServiceTest {

  private RestoredDlpItemDAO         restoredDlpItemDAO;

  private RestoredDlpItemServiceImpl restoredDlpItemService;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    PortalContainer container = PortalContainer.getInstance();
    restoredDlpItemDAO = container.getComponentInstanceOfType(RestoredDlpItemDAO.class);
    restoredDlpItemService = new RestoredDlpItemServiceImpl(restoredDlpItemDAO);
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
  }

  @Test
  public void testGetRestoredDlpItemByReference() {
    // Given
    RestoredDlpItemEntity restoredDlpItemEntity = new RestoredDlpItemEntity();
    restoredDlpItemEntity.setReference("nodeUID");
    restoredDlpItemEntity.setDetectionDate(Calendar.getInstance());
    // When
    restoredDlpItemService.addRestoredDlpItem(restoredDlpItemEntity);
    RestoredDlpItem restoredDlpItem = restoredDlpItemService.getRestoredDlpItemByReference("nodeUID");

    // Then
    assertNotNull(restoredDlpItem);
  }
  
  @After
  public void tearDown() {
    restoredDlpItemDAO.deleteAll();
    RequestLifeCycle.end();
  }
}
