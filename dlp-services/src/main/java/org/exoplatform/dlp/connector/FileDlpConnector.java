/*
 * Copyright (C) 2022 eXo Platform SAS
 *
 *  This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <gnu.org/licenses>.
 */
package org.exoplatform.dlp.connector;

import java.net.URLDecoder;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.dlp.domain.DlpPositiveItemEntity;
import org.exoplatform.dlp.domain.RestoredDlpItemEntity;
import org.exoplatform.dlp.dto.RestoredDlpItem;
import org.exoplatform.dlp.processor.DlpOperationProcessor;
import org.exoplatform.dlp.service.DlpPositiveItemService;
import org.exoplatform.dlp.service.RestoredDlpItemService;
import org.exoplatform.ecms.legacy.search.data.SearchContext;
import org.exoplatform.ecms.legacy.search.data.SearchResult;
import org.exoplatform.services.cms.documents.TrashService;
import org.exoplatform.services.cms.impl.Utils;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ExtendedSession;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.wcm.core.NodetypeConstant;
import org.exoplatform.services.wcm.search.connector.FileSearchServiceConnector;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.web.controller.metadata.ControllerDescriptor;
import org.exoplatform.web.controller.router.Router;

import com.google.common.annotations.VisibleForTesting;

import javax.jcr.*;

/**
 * Dlp Connector for Files
 */
public class FileDlpConnector extends DlpServiceConnector {

  public static final String         TYPE                  = "file";

  public static final String         DLP_QUARANTINE_FOLDER = "Quarantine";

  public static final String         EXO_CURRENT_PROVIDER  = "exo:currentProvider";

  private static final Log           LOGGER                = ExoLogger.getExoLogger(FileDlpConnector.class);

  private static final String        COLLABORATION_WS      = "collaboration";

  private RepositoryService          repositoryService;

  public static final String         EXO_RESTORE_LOCATION  = "exo:restoreLocation";

  public static final String         RESTORE_PATH          = "exo:restorePath";

  public static final String         EXO_DLP_ITEM_MIXIN    = "exo:exoDlpItem";

  private IndexingService            indexingService;

  private FileSearchServiceConnector fileSearchServiceConnector;

  private RestoredDlpItemService     restoredDlpItemService;

  private DlpOperationProcessor      dlpOperationProcessor;

  private TrashService               trashService;

  private LinkManager                linkManager;

  public FileDlpConnector(InitParams initParams,
                          FileSearchServiceConnector fileSearchServiceConnector,
                          RepositoryService repositoryService,
                          IndexingService indexingService,
                          DlpOperationProcessor dlpOperationProcessor,
                          RestoredDlpItemService restoredDlpItemService,
                          LinkManager linkManager,
                          TrashService trashService) {
    super(initParams);
    this.repositoryService = repositoryService;
    this.indexingService = indexingService;
    this.fileSearchServiceConnector = fileSearchServiceConnector;
    this.restoredDlpItemService = restoredDlpItemService;
    this.dlpOperationProcessor = dlpOperationProcessor;
    this.linkManager = linkManager;
    this.trashService = trashService;
  }

  @Override
  public boolean processItem(String entityId) {
    LOGGER.debug("Process item {}", entityId);
    if (!itemExist(entityId) || isInTrash(entityId)) {
      // if a document is in trash, we cannot check it because it is not indexed.
      // so we return true to remove it from the queue
      return true;
    }
    if (!isIndexedByEs(entityId) || editorOpened(entityId)) {
      return false;
    } else {
      checkMatchKeywordAndTreatItem(entityId);
    }
    return true;
  }

  private boolean itemExist(String entityId) {
    ExtendedSession session = null;
    try {
      session = (ExtendedSession) WCMCoreUtils.getSystemSessionProvider()
                                              .getSession(COLLABORATION_WS, repositoryService.getCurrentRepository());
      Item item = session.getNodeByIdentifier(entityId);
      LOGGER.debug("Item {} exists, path={}", entityId, item.getPath());
      return true;

    } catch (ItemNotFoundException e) {
      LOGGER.debug("Item {} not exists", entityId);
      return false;

    } catch (RepositoryException e) {
      LOGGER.error("Error when reading repository", e);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
    return false;
  }

  private boolean isInTrash(String entityId) {
    ExtendedSession session = null;
    try {
      session = (ExtendedSession) WCMCoreUtils.getSystemSessionProvider()
                                              .getSession(COLLABORATION_WS, repositoryService.getCurrentRepository());
      Node node = session.getNodeByIdentifier(entityId);
      boolean result = trashService.isInTrash(node);
      LOGGER.debug("Item {} isInTrash={}", entityId, result);
      return result;
    } catch (Exception e) {
      LOGGER.error("Error when check if node {} is in trash", entityId, e);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
    return false;
  }

  @Override
  public void restorePositiveItem(String itemReference) {
    ExtendedSession session = null;
    try {
      long startTime = System.currentTimeMillis();
      session = (ExtendedSession) WCMCoreUtils.getSystemSessionProvider()
                                              .getSession(COLLABORATION_WS, repositoryService.getCurrentRepository());
      Node node = session.getNodeByIdentifier(itemReference);
      String fileName = node.getName();
      removeMixinForRestoredItemSymlinks(node);
      restoreFromQuarantine(node.getPath());
      indexingService.reindex(TYPE, itemReference);
      saveRestoredDlpItem(node.getUUID());
      long endTime = System.currentTimeMillis();
      long totalTime = endTime - startTime;
      LOGGER.info("service={} operation={} parameters=\"fileName:{}\" status=ok "
          + "duration_ms={}", DlpOperationProcessor.DLP_FEATURE, "restoreDLPItem", fileName, totalTime);
    } catch (Exception e) {
      LOGGER.error("Error while treating file dlp connector item", e);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  private boolean isIndexedByEs(String entityId) {
    SearchContext searchContext = null;
    try {
      searchContext = new SearchContext(new Router(new ControllerDescriptor()), "");
      boolean result = fileSearchServiceConnector.isIndexed(searchContext, entityId);
      LOGGER.debug("Item {} isindexedByEs={}", entityId, result);
      return result;
    } catch (Exception ex) {
      LOGGER.error("Can not create SearchContext", ex);
    }
    return false;
  }

  private void checkMatchKeywordAndTreatItem(String entityId) {
    SearchContext searchContext = null;
    String dlpKeywords = dlpOperationProcessor.getKeywords();
    if (dlpKeywords != null && !dlpKeywords.isEmpty()) {
      try {
        searchContext = new SearchContext(new Router(new ControllerDescriptor()), "");
        Collection<SearchResult> searchResults =
                                               fileSearchServiceConnector.searchByEntityId(searchContext, dlpKeywords, entityId);
        if (!getDetectedKeywords(searchResults, dlpOperationProcessor.getKeywords()).isEmpty()) {
          treatItem(entityId, searchResults);
        } else {
          LOGGER.debug("Item {} does not match keywords", entityId);
        }
      } catch (Exception ex) {
        LOGGER.error("Can not create SearchContext", ex);
      }
    }
  }

  @VisibleForTesting
  protected void treatItem(String entityId, Collection<SearchResult> searchResults) {
    ExtendedSession session = null;
    try {
      long startTime = System.currentTimeMillis();
      session = (ExtendedSession) WCMCoreUtils.getSystemSessionProvider()
                                              .getSession(COLLABORATION_WS, repositoryService.getCurrentRepository());
      Workspace workspace = session.getWorkspace();
      Node node = session.getNodeByIdentifier(entityId);
      String fileName = node.getName();
      String restorePath = fixRestorePath(node.getPath());
      RestoredDlpItem restoredDlpItem = findRestoredDlpItem(node.getUUID());
      if (!node.getPath().startsWith("/" + DLP_QUARANTINE_FOLDER + "/")
          && (restoredDlpItem == null || getNodeLastModifiedDate(node) > restoredDlpItem.getDetectionDate())) {
        LOGGER.debug("Item {} is put in DLP quarantine", entityId);
        addMixinForRestoredItemSymlinks(node);
        workspace.move(node.getPath(), "/" + DLP_QUARANTINE_FOLDER + "/" + fileName);
        indexingService.unindex(TYPE, entityId);
        saveDlpPositiveItem(node, searchResults);
        addRestorePathInfo(node.getName(), restorePath);
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        LOGGER.info("service={} operation={} parameters=\"fileName:{}\" status=ok "
            + "duration_ms={}", DlpOperationProcessor.DLP_FEATURE, DLP_POSITIVE_DETECTION, fileName, totalTime);
      }
    } catch (Exception e) {
      LOGGER.error("Error while treating file dlp connector item", e);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  private Long getNodeLastModifiedDate(Node node) throws Exception {
    Calendar calendar = node
                            .hasProperty(NodetypeConstant.EXO_LAST_MODIFIED_DATE) ? node.getProperty(NodetypeConstant.EXO_LAST_MODIFIED_DATE).getDate() : node.getProperty(NodetypeConstant.EXO_DATE_CREATED).getDate();
    return calendar.getTimeInMillis();
  }

  private void saveDlpPositiveItem(Node node, Collection<SearchResult> searchResults) throws Exception {
    DlpPositiveItemService dlpPositiveItemService = CommonsUtils.getService(DlpPositiveItemService.class);
    DlpPositiveItemEntity dlpPositiveItemEntity = new DlpPositiveItemEntity();
    dlpPositiveItemEntity.setReference(node.getUUID());
    if (node.hasProperty(NodetypeConstant.EXO_TITLE)) {
      String title = node.getProperty(NodetypeConstant.EXO_TITLE).getString();
      dlpPositiveItemEntity.setTitle(URLDecoder.decode(title, "UTF-8"));
    }
    if (node.hasProperty(NodetypeConstant.EXO_LAST_MODIFIER)) {
      String author = node.getProperty(NodetypeConstant.EXO_LAST_MODIFIER).getString();
      dlpPositiveItemEntity.setAuthor(author);
    }
    dlpPositiveItemEntity.setType(TYPE);
    dlpPositiveItemEntity.setDetectionDate(Calendar.getInstance());
    dlpPositiveItemEntity.setKeywords(getDetectedKeywords(searchResults, dlpOperationProcessor.getKeywords()));
    dlpPositiveItemService.addDlpPositiveItem(dlpPositiveItemEntity);
  }

  @Override
  public void removePositiveItem(String itemReference) {
    ExtendedSession session = null;
    try {
      session = (ExtendedSession) WCMCoreUtils.getSystemSessionProvider()
                                              .getSession(COLLABORATION_WS, repositoryService.getCurrentRepository());
      Node dlpQuarantineNode = (Node) session.getItem("/" + DLP_QUARANTINE_FOLDER);
      Node node = session.getNodeByIdentifier(itemReference);

      if (node != null && dlpQuarantineNode != null) {
        Utils.removeDeadSymlinks(node, false);
        node.remove();
        dlpQuarantineNode.save();
      }
    } catch (Exception e) {
      LOGGER.error("Error while deleting dlp file item", e);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  @Override
  public boolean checkExternal(String userId) {
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId);
    return identity.getProfile().getProperty("external") != null && identity.getProfile().getProperty("external").equals("true");
  }

  @Override
  public String getItemUrl(String itemReference) {
    ExtendedSession session = null;
    try {
      session = (ExtendedSession) WCMCoreUtils.getSystemSessionProvider()
                                              .getSession(COLLABORATION_WS, repositoryService.getCurrentRepository());
      Node node = session.getNodeByIdentifier(itemReference);
      return WCMCoreUtils.getLinkInDocumentsApplication(node.getPath());
    } catch (Exception e) {
      LOGGER.error("Error while getting dlp item url, itemId={}", itemReference, e);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
    return null;
  }

  private String getDetectedKeywords(Collection<SearchResult> searchResults, String dlpKeywords) {
    List<String> detectedKeywords = new ArrayList<>();
    List<String> dlpKeywordsList = Arrays.asList(dlpKeywords.split(","));
    searchResults.stream()
                 .map(SearchResult::getExcerpts)
                 .map(Map::values)
                 .filter(excerptValue -> !excerptValue.isEmpty())
                 .flatMap(Collection::stream)
                 .flatMap(Collection::stream)
                 .forEach(s -> dlpKeywordsList.stream()
                                .filter(key -> removeAccents(s).contains(escapeSpecialCharacters(removeAccents(key)))
                                    || removeAccents(s).contains(key))
                                .forEach(key -> {
                                  if (!key.isEmpty() && !detectedKeywords.contains(key)) {
                                    detectedKeywords.add(key);
                                  }
                                }));
    return detectedKeywords.stream().collect(Collectors.joining(", "));
  }

  private String escapeSpecialCharacters(String keyword) {
    List<String> keywordParts = Arrays.stream(keyword.split("[+\\-=&|><!(){}\\[\\]^\"*?:/ @$#]+"))
                                      .distinct()
                                      .collect(Collectors.toList());
    for (String s : keywordParts) {
      keyword = keyword.replace(s, "<em>" + s + "</em>");
    }
    // ES replace any occurrence of single quote (') with an apostrophe (’),
    // so we need to do the same to get the detected keywords.
    return keyword.replace("'", "’");
  }

  private void saveRestoredDlpItem(String nodeUID) {
    RestoredDlpItemEntity restoredDlpItemEntity = new RestoredDlpItemEntity();
    restoredDlpItemEntity.setReference(nodeUID);
    restoredDlpItemEntity.setDetectionDate(Calendar.getInstance());
    restoredDlpItemService.addRestoredDlpItem(restoredDlpItemEntity);
  }

  private RestoredDlpItem findRestoredDlpItem(String nodeUID) throws Exception {
    return restoredDlpItemService.getRestoredDlpItemByReference(nodeUID);
  }

  private void addRestorePathInfo(String nodeName, String restorePath) throws Exception {
    NodeIterator nodes = Objects.requireNonNull(this.getQuarantineHomeNode()).getNodes(nodeName);
    Node node = null;
    while (nodes.hasNext()) {
      Node currentNode = nodes.nextNode();
      if (node == null) {
        node = currentNode;
      } else {
        if (node.getIndex() < currentNode.getIndex()) {
          node = currentNode;
        }
      }
    }
    if (node != null) {
      node.addMixin(EXO_RESTORE_LOCATION);
      node.setProperty(RESTORE_PATH, restorePath);
      node.save();
    }
  }

  private Node getQuarantineHomeNode() {
    try {
      Session session = WCMCoreUtils.getSystemSessionProvider()
                                    .getSession(COLLABORATION_WS, repositoryService.getCurrentRepository());
      return (Node) session.getItem("/" + DLP_QUARANTINE_FOLDER);
    } catch (Exception e) {
      return null;
    }

  }

  private String fixRestorePath(String path) {
    int leftBracket = path.lastIndexOf('[');
    int rightBracket = path.lastIndexOf(']');
    if (leftBracket == -1 || rightBracket == -1 || (leftBracket >= rightBracket))
      return path;

    try {
      Integer.parseInt(path.substring(leftBracket + 1, rightBracket));
    } catch (Exception ex) {
      return path;
    }
    return path.substring(0, leftBracket);
  }

  private void restoreFromQuarantine(String securityNodePath) throws Exception {

    Node securityHomeNode = this.getQuarantineHomeNode();
    if (securityHomeNode == null) {
      return;
    }
    Session restoreSession = securityHomeNode.getSession();
    Node securityNode = (Node) restoreSession.getItem(securityNodePath);
    String restorePath = securityNode.getProperty(RESTORE_PATH).getString();
    restoreSession.getWorkspace().move(securityNodePath, restorePath);
    // we set the EXO_LAST_MODIFIER to set the updater who validate the document in dlp administration.
    if(securityNode.hasProperty(NodetypeConstant.EXO_LAST_MODIFIER)){
      restoreSession.getNodeByUUID(securityNode.getUUID())
                    .setProperty(NodetypeConstant.EXO_LAST_MODIFIER ,securityNode.getProperty(NodetypeConstant.EXO_LAST_MODIFIER).getString());
    }
    removeRestorePathInfo(restoreSession, restorePath);
    securityHomeNode.save();
    restoreSession.save();
  }

  private void removeRestorePathInfo(Session session, String restorePath) throws Exception {
    Node sameNameNode = ((Node) session.getItem(restorePath));
    Node parent = sameNameNode.getParent();
    String name = sameNameNode.getName();
    NodeIterator nodeIter = parent.getNodes(name);
    while (nodeIter.hasNext()) {
      Node node = nodeIter.nextNode();
      if (node.isNodeType(EXO_RESTORE_LOCATION))
        node.removeMixin(EXO_RESTORE_LOCATION);
    }
  }

  private boolean editorOpened(String entityId) {
    Node node = null;
    ExtendedSession session = null;
    try {
      session = (ExtendedSession) WCMCoreUtils.getSystemSessionProvider()
                                              .getSession(COLLABORATION_WS, repositoryService.getCurrentRepository());
      node = session.getNodeByIdentifier(entityId);
      boolean result = node.hasProperty(EXO_CURRENT_PROVIDER);
      LOGGER.debug("Item {} isEditorOpened={}", entityId, result);
      return result;
    } catch (RepositoryException e) {
      LOGGER.error("Error while checking editor status", e);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
    return false;
  }

  private String removeAccents(String string) {
    if (StringUtils.isNotBlank(string)) {
      string = Normalizer.normalize(string, Normalizer.Form.NFD);
      string = string.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }
    try {
      return URLDecoder.decode(string.toLowerCase(), "UTF-8");
    } catch (Exception e) {
      return string.toLowerCase();
    }
  }

  private void addMixinForRestoredItemSymlinks(Node node) throws Exception {
    List<Node> itemLinks = linkManager.getAllLinks(node, NodetypeConstant.EXO_SYMLINK, WCMCoreUtils.getSystemSessionProvider());
    if (itemLinks != null && !itemLinks.isEmpty()) {
      for (Node nodeLink : itemLinks) {
        nodeLink.addMixin(EXO_DLP_ITEM_MIXIN);
        nodeLink.save();
      }
    }
  }

  private void removeMixinForRestoredItemSymlinks(Node node) throws Exception {
    List<Node> itemLinks = linkManager.getAllLinks(node, NodetypeConstant.EXO_SYMLINK, WCMCoreUtils.getSystemSessionProvider());
    if (itemLinks != null && !itemLinks.isEmpty()) {
      for (Node nodeLink : itemLinks) {
        nodeLink.removeMixin(EXO_DLP_ITEM_MIXIN);
        nodeLink.save();
      }
    }
  }
}
