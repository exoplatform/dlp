package org.exoplatform.dlp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.dlp.connector.DlpServiceConnector;
import org.exoplatform.dlp.dao.DlpOperationDAO;
import org.exoplatform.dlp.dao.DlpPositiveItemDAO;
import org.exoplatform.dlp.domain.DlpPositiveItemEntity;
import org.exoplatform.dlp.dto.DlpPositiveItem;
import org.exoplatform.dlp.processor.impl.DlpOperationProcessorImpl;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.idm.UserImpl;

@RunWith(MockitoJUnitRunner.class)
public class DlpPositiveItemServiceTest {

  @Mock
  private DlpOperationDAO            dlpOperationDAO;

  private DlpOperationProcessorImpl  dlpOperationProcessor;

  private DlpPositiveItemDAO         dlpPositiveItemDAO;

  private DlpPositiveItemServiceImpl dlpPositiveItemService;

  private DlpServiceConnector        dlpServiceConnector;

  @Mock
  private DlpServiceConnector        dlpServiceConnector1;

  @Mock
  private DlpServiceConnector        dlpServiceConnector2;

  @Mock
  private ListenerService            listenerService;

  private OrganizationService        organizationService;

  private UserHandler                userHandler;

  @After
  public void clean() {
    dlpOperationProcessor.getConnectors().clear();
    dlpPositiveItemDAO.deleteAll();
    RequestLifeCycle.end();
  }

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    organizationService = mock(OrganizationService.class);
    dlpServiceConnector = mock(DlpServiceConnector.class);
    listenerService = mock(ListenerService.class);
    userHandler = mock(UserHandler.class);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    UserImpl user = new UserImpl();
    user.setUserName("root");
    user.setFullName("root root");
    Mockito.when(userHandler.findUserByName("root")).thenReturn(user);
    PortalContainer container = PortalContainer.getInstance();
    dlpPositiveItemDAO = container.getComponentInstanceOfType(DlpPositiveItemDAO.class);
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
    initDlpServiceConnector();
    dlpOperationProcessor = new DlpOperationProcessorImpl(dlpOperationDAO);
    dlpPositiveItemService = new DlpPositiveItemServiceImpl(dlpPositiveItemDAO,
                                                            organizationService,
                                                            listenerService,
                                                            dlpOperationProcessor);
  }

  private void initDlpServiceConnector() {
    when(dlpServiceConnector.getType()).thenReturn("file");
    when(dlpServiceConnector.getDisplayName()).thenReturn("file");
    when(dlpServiceConnector.isEnable()).thenReturn(true);
    when(dlpServiceConnector.getItemUrl("reference1234")).thenReturn("/Security/file");
    when(dlpServiceConnector.getType()).thenReturn("file");
    lenient().when(dlpServiceConnector.getDisplayName()).thenReturn("file");
    lenient().when(dlpServiceConnector.isEnable()).thenReturn(true);
    lenient().when(dlpServiceConnector.getItemUrl("ref")).thenReturn("/Security/file12");
    when(dlpServiceConnector1.getType()).thenReturn("file1");
    lenient().when(dlpServiceConnector1.getDisplayName()).thenReturn("file1");
    lenient().when(dlpServiceConnector1.isEnable()).thenReturn(true);
    lenient().when(dlpServiceConnector1.getItemUrl("ref1")).thenReturn("/Security/file123");
    when(dlpServiceConnector2.getType()).thenReturn("file2");
    lenient().when(dlpServiceConnector2.getDisplayName()).thenReturn("file2");
    lenient().when(dlpServiceConnector2.isEnable()).thenReturn(true);
    lenient().when(dlpServiceConnector2.getItemUrl("ref2")).thenReturn("/Security/file1234");
  }

  @Test
  public void testGetDlpPositiveItemByReference() throws Exception {
    // Given
    DlpPositiveItemEntity dlpPositiveItemEntity = new DlpPositiveItemEntity();
    dlpPositiveItemEntity.setType("file");
    dlpPositiveItemEntity.setTitle("file");
    dlpPositiveItemEntity.setDetectionDate(Calendar.getInstance());
    dlpPositiveItemEntity.setAuthor("root");
    dlpPositiveItemEntity.setKeywords("test1");
    dlpPositiveItemEntity.setReference("reference1234");

    // When
    dlpPositiveItemService.addDlpPositiveItem(dlpPositiveItemEntity);
    dlpOperationProcessor.addConnector(dlpServiceConnector);
    DlpPositiveItem dlpPositiveItem = dlpPositiveItemService.getDlpPositiveItemByReference("reference1234");

    // Then
    assertNotNull(dlpPositiveItem);
  }

  @Test
  public void testGetDlpPositivesItems() throws Exception {
    // Given
    DlpPositiveItemEntity dlpPositiveItemEntity = new DlpPositiveItemEntity();
    dlpPositiveItemEntity.setType("file");
    dlpPositiveItemEntity.setTitle("file");
    dlpPositiveItemEntity.setReference("ref");
    dlpPositiveItemEntity.setDetectionDate(Calendar.getInstance());
    dlpPositiveItemEntity.setAuthor("root");
    dlpPositiveItemEntity.setKeywords("Keywords");

    DlpPositiveItemEntity dlpPositiveItemEntity1 = new DlpPositiveItemEntity();
    dlpPositiveItemEntity1.setType("file1");
    dlpPositiveItemEntity1.setTitle("file1");
    dlpPositiveItemEntity1.setReference("ref1");
    dlpPositiveItemEntity1.setDetectionDate(Calendar.getInstance());
    dlpPositiveItemEntity1.setAuthor("root");
    dlpPositiveItemEntity.setKeywords("Keywords1");

    DlpPositiveItemEntity dlpPositiveItemEntity2 = new DlpPositiveItemEntity();
    dlpPositiveItemEntity2.setType("file2");
    dlpPositiveItemEntity2.setTitle("file2");
    dlpPositiveItemEntity2.setReference("ref2");
    dlpPositiveItemEntity2.setDetectionDate(Calendar.getInstance());
    dlpPositiveItemEntity2.setAuthor("root");
    dlpPositiveItemEntity.setKeywords("Keywords2");

    // When
    dlpPositiveItemService.addDlpPositiveItem(dlpPositiveItemEntity);
    dlpOperationProcessor.addConnector(dlpServiceConnector);
    dlpPositiveItemService.addDlpPositiveItem(dlpPositiveItemEntity1);
    dlpOperationProcessor.addConnector(dlpServiceConnector1);
    dlpPositiveItemService.addDlpPositiveItem(dlpPositiveItemEntity2);
    dlpOperationProcessor.addConnector(dlpServiceConnector2);

    List<DlpPositiveItem> dlpPositiveItems = dlpPositiveItemService.getDlpPositivesItems(0, 20);
    Long size = dlpPositiveItemService.getDlpPositiveItemsCount();

    // Then
    assertNotNull(dlpPositiveItems);
    assertEquals(3, size.intValue());

    // when
    dlpPositiveItemService.deleteDlpPositiveItem(dlpPositiveItems.get(0).getId());
    size = dlpPositiveItemService.getDlpPositiveItemsCount();

    // then
    assertEquals(2, size.intValue());
  }
}
