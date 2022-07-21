package org.exoplatform.dlp.connector;

import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.dlp.dto.RestoredDlpItem;
import org.exoplatform.dlp.processor.DlpOperationProcessor;
import org.exoplatform.dlp.service.DlpPositiveItemService;
import org.exoplatform.dlp.service.RestoredDlpItemService;
import org.exoplatform.services.cms.documents.TrashService;
import org.exoplatform.services.cms.impl.Utils;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ExtendedSession;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.NodetypeConstant;
import org.exoplatform.services.wcm.search.connector.FileSearchServiceConnector;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.jcr.*;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ WCMCoreUtils.class, CommonsUtils.class, Utils.class })
@PowerMockIgnore({ "javax.management.*" })
public class FileDlpConnectorTest {

  @Mock
  private RepositoryService          repositoryService;

  @Mock
  private IndexingService            indexingService;

  @Mock
  private FileSearchServiceConnector fileSearchServiceConnector;

  @Mock
  private RestoredDlpItemService     restoredDlpItemService;

  @Mock
  private DlpOperationProcessor      dlpOperationProcessor;

  @Mock
  private TrashService               trashService;

  @Mock
  private LinkManager                linkManager;

  private FileDlpConnector           fileDlpConnector;

  @Before
  public void setUp() throws Exception {
    InitParams params = new InitParams();
    PropertiesParam constructorParams = new PropertiesParam();
    constructorParams.setName("constructor.params");
    constructorParams.setProperty("type", "file");
    constructorParams.setProperty("enabled", "true");
    constructorParams.setProperty("displayName", "file");
    params.addParameter(constructorParams);
    PowerMockito.mockStatic(WCMCoreUtils.class);
    PowerMockito.mockStatic(CommonsUtils.class);
    PowerMockito.mockStatic(Utils.class);
    fileDlpConnector = new FileDlpConnector(params,
                                            fileSearchServiceConnector,
                                            repositoryService,
                                            indexingService,
                                            dlpOperationProcessor,
                                            restoredDlpItemService,
                                            linkManager,
                                            trashService);
  }

  @Test
  public void processItem() throws Exception {
    ManageableRepository repository = mock(ManageableRepository.class);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    ExtendedSession session = mock(ExtendedSession.class);
    SessionProvider sessionProvider = mock(SessionProvider.class);
    when(WCMCoreUtils.getSystemSessionProvider()).thenReturn(sessionProvider);
    when(sessionProvider.getSession("collaboration", repository)).thenReturn(session);

    Node item = mock(Node.class);
    when(session.getNodeByIdentifier("1")).thenReturn(item);
    when(fileSearchServiceConnector.isIndexed(any(), anyString())).thenReturn(true);
    when(trashService.isInTrash(any())).thenReturn(true);
    assertTrue(fileDlpConnector.processItem("1"));

    when(trashService.isInTrash(any())).thenReturn(false);
    when(dlpOperationProcessor.getKeywords()).thenReturn("test");
    SearchResult searchResult = new SearchResult("url", "title", "test", "detail", "imageUri", 1321332, 1L);
    searchResult.setExcerpts(Map.of("title", List.of("test")));
    when(fileSearchServiceConnector.searchByEntityId(any(), anyString(), anyString())).thenReturn(List.of(searchResult));
    when(session.getWorkspace()).thenReturn(mock(Workspace.class));
    when(item.getName()).thenReturn("fileName");
    when(item.getPath()).thenReturn("path/Quarantine");
    when(item.getUUID()).thenReturn("123");
    when(restoredDlpItemService.getRestoredDlpItemByReference(item.getUUID())).thenReturn(new RestoredDlpItem());
    boolean process = fileDlpConnector.processItem("1");
    verify(fileSearchServiceConnector, times(1)).searchByEntityId(any(), anyString(), anyString());
    assertTrue(process);

    doThrow(new RepositoryException()).when(session).getNodeByIdentifier("1");
    assertTrue(fileDlpConnector.processItem("1"));
  }

  @Test
  public void restorePositiveItem() throws Exception {
    ManageableRepository repository = mock(ManageableRepository.class);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    ExtendedSession session = mock(ExtendedSession.class);
    SessionProvider sessionProvider = mock(SessionProvider.class);
    when(WCMCoreUtils.getSystemSessionProvider()).thenReturn(sessionProvider);
    when(sessionProvider.getSession("collaboration", repository)).thenReturn(session);

    Node item = mock(Node.class);
    Property property = mock(Property.class);
    Workspace workspace = mock(Workspace.class);
    Node parent = mock(Node.class);
    NodeIterator nodeIterator = mock(NodeIterator.class);

    when(session.getNodeByIdentifier("1")).thenReturn(item);
    when(item.getName()).thenReturn("fileName");
    Node link = mock(Node.class);
    when(linkManager.getAllLinks(item, NodetypeConstant.EXO_SYMLINK, sessionProvider)).thenReturn(List.of(link));
    when(session.getItem("/Quarantine")).thenReturn(item);
    when(item.getSession()).thenReturn(session);
    when(item.getProperty("exo:restorePath")).thenReturn(property);
    when(property.getString()).thenReturn("restorePath");
    when(item.getPath()).thenReturn("path");
    when(session.getItem("path")).thenReturn(item);
    when(session.getNodeByUUID(item.getUUID())).thenReturn(item);
    when(item.getProperty(NodetypeConstant.EXO_LAST_MODIFIER)).thenReturn(property);
    when(session.getWorkspace()).thenReturn(workspace);
    when(session.getItem("restorePath")).thenReturn(item);

    when(item.getParent()).thenReturn(parent);
    when(parent.getName()).thenReturn("parentName");
    when(parent.getNodes(item.getName())).thenReturn(nodeIterator);
    fileDlpConnector.restorePositiveItem("1");

    verify(workspace, times(1)).move("path", "restorePath");
    verify(link, times(1)).removeMixin("exo:exoDlpItem");
    verify(link, times(1)).save();
    verify(indexingService, times(1)).reindex("file", "1");
    verify(restoredDlpItemService, times(1)).addRestoredDlpItem(any());
  }

  @Test
  public void treatItem() throws Exception {
    ManageableRepository repository = mock(ManageableRepository.class);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    ExtendedSession session = mock(ExtendedSession.class);
    SessionProvider sessionProvider = mock(SessionProvider.class);
    when(WCMCoreUtils.getSystemSessionProvider()).thenReturn(sessionProvider);
    when(sessionProvider.getSession("collaboration", repository)).thenReturn(session);
    Node item = mock(Node.class);
    Workspace workspace = mock(Workspace.class);

    when(session.getWorkspace()).thenReturn(workspace);
    when(session.getNodeByIdentifier("1")).thenReturn(item);
    when(item.getName()).thenReturn("fileName");
    when(item.getPath()).thenReturn("/Quarantine/path");
    when(item.getUUID()).thenReturn("123");
    when(restoredDlpItemService.getRestoredDlpItemByReference(item.getUUID())).thenReturn(new RestoredDlpItem());
    SearchResult searchResult = new SearchResult("url", "title", "test", "detail", "imageUri", 1321332, 1L);
    searchResult.setExcerpts(Map.of("title", List.of("test")));
    fileDlpConnector.treatItem("1", List.of(searchResult));

    verify(workspace, times(0)).move(item.getPath(), "/Quarantine/fileName");

    when(restoredDlpItemService.getRestoredDlpItemByReference(item.getUUID())).thenReturn(null);
    Node link = mock(Node.class);
    when(linkManager.getAllLinks(item, NodetypeConstant.EXO_SYMLINK, sessionProvider)).thenReturn(List.of(link));
    when(item.getPath()).thenReturn("/path");
    when(CommonsUtils.getService(DlpPositiveItemService.class)).thenReturn(mock(DlpPositiveItemService.class));
    Property titleProperty = mock(Property.class);
    Property lastModifierProperty = mock(Property.class);

    when(titleProperty.getString()).thenReturn("title");
    when(item.getProperty(NodetypeConstant.EXO_TITLE)).thenReturn(titleProperty);
    when(item.hasProperty(NodetypeConstant.EXO_TITLE)).thenReturn(true);

    when(lastModifierProperty.getString()).thenReturn("user");
    when(item.hasProperty(NodetypeConstant.EXO_LAST_MODIFIER)).thenReturn(true);
    when(item.getProperty(NodetypeConstant.EXO_LAST_MODIFIER)).thenReturn(lastModifierProperty);
    when(dlpOperationProcessor.getKeywords()).thenReturn("test");
    when(session.getItem("/Quarantine")).thenReturn(item);
    NodeIterator nodeIterator = mock(NodeIterator.class);
    when(item.getNodes("fileName")).thenReturn(nodeIterator);
    when(nodeIterator.hasNext()).thenReturn(false);

    fileDlpConnector.treatItem("1", List.of(searchResult));
    verify(indexingService, times(1)).unindex("file", "1");
    verify(workspace, times(1)).move(item.getPath(), "/Quarantine/fileName");
  }

  @Test
  public void removePositiveItem() throws Exception {
    ManageableRepository repository = mock(ManageableRepository.class);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    ExtendedSession session = mock(ExtendedSession.class);
    SessionProvider sessionProvider = mock(SessionProvider.class);
    when(WCMCoreUtils.getSystemSessionProvider()).thenReturn(sessionProvider);
    when(sessionProvider.getSession("collaboration", repository)).thenReturn(session);
    Node item = mock(Node.class);
    Node dlpQuarantineNode = mock(Node.class);
    Workspace workspace = mock(Workspace.class);

    when(session.getWorkspace()).thenReturn(workspace);
    when(session.getNodeByIdentifier("1")).thenReturn(item);
    when(session.getItem("/Quarantine")).thenReturn(dlpQuarantineNode);

    fileDlpConnector.removePositiveItem("1");

    PowerMockito.verifyStatic(Utils.class, times(1));
    Utils.removeDeadSymlinks(item, false);
    verify(item, times(1)).remove();
    verify(dlpQuarantineNode, times(1)).save();
  }

  @Test
  public void getItemUrl() throws Exception {
    ManageableRepository repository = mock(ManageableRepository.class);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    ExtendedSession session = mock(ExtendedSession.class);
    SessionProvider sessionProvider = mock(SessionProvider.class);
    when(WCMCoreUtils.getSystemSessionProvider()).thenReturn(sessionProvider);
    when(sessionProvider.getSession("collaboration", repository)).thenReturn(session);
    Node item = mock(Node.class);
    Workspace workspace = mock(Workspace.class);
    when(session.getWorkspace()).thenReturn(workspace);
    when(session.getNodeByIdentifier("1")).thenReturn(item);
    when(item.getPath()).thenReturn("path");
    when(WCMCoreUtils.getLinkInDocumentsApplication(item.getPath())).thenReturn("/url/path");
    assertEquals("/url/path", fileDlpConnector.getItemUrl("1"));
  }
}
