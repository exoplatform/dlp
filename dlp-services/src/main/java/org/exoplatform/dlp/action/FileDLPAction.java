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
package org.exoplatform.dlp.action;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jcr.observation.Event;

import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.dlp.connector.FileDlpConnector;
import org.exoplatform.dlp.processor.DlpOperationProcessor;
import org.exoplatform.dlp.queue.QueueDlpService;
import org.exoplatform.services.cms.documents.TrashService;
import org.exoplatform.services.ext.action.InvocationContext;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.PropertyImpl;
import org.exoplatform.services.jcr.impl.ext.action.AdvancedAction;
import org.exoplatform.services.jcr.impl.ext.action.AdvancedActionException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.wcm.core.NodetypeConstant;

/**
 * JCR action which listens on all nodes events to validate them
 */
public class FileDLPAction implements AdvancedAction {
  private static final Log          LOGGER                 = ExoLogger.getExoLogger(FileDLPAction.class);

  private TrashService              trashService;

  private QueueDlpService           queueDlpService;

  private ExoFeatureService         featureService;

  private DlpOperationProcessor     dlpOperationProcessor;

  private static final String       EXO_EDITORS_RUNTIME_ID = "exo:editorsId";

  private static final List<String> excludedPropertyNames  =
                                                          Collections.unmodifiableList(Arrays.asList(EXO_EDITORS_RUNTIME_ID,
                                                                                                     FileDlpConnector.EXO_CURRENT_PROVIDER,
                                                                                                     FileDlpConnector.RESTORE_PATH));

  public FileDLPAction() {
    this.trashService = CommonsUtils.getService(TrashService.class);
    this.queueDlpService = CommonsUtils.getService(QueueDlpService.class);
    this.featureService = CommonsUtils.getService(ExoFeatureService.class);
    this.dlpOperationProcessor = CommonsUtils.getService(DlpOperationProcessor.class);
  }

  @Override
  public boolean execute(Context context) throws Exception {
    if (!featureService.isActiveFeature(DlpOperationProcessor.DLP_FEATURE)
        || StringUtils.isBlank(dlpOperationProcessor.getKeywords())) {
      return false;
    }
    int eventType = (Integer) context.get(InvocationContext.EVENT);
    NodeImpl node;
    switch (eventType) {
    case Event.NODE_ADDED:
      node = (NodeImpl) context.get(InvocationContext.CURRENT_ITEM);
      if (node != null && !trashService.isInTrash(node)) {
        if (node.isNodeType(NodetypeConstant.NT_RESOURCE)) {
          node = node.getParent();
        }
        String entityId = node.getInternalIdentifier();
        queueDlpService.addToQueue(FileDlpConnector.TYPE, entityId);
      }
      break;
    case Event.PROPERTY_ADDED:
    case Event.PROPERTY_CHANGED:
      PropertyImpl property = (PropertyImpl) context.get(InvocationContext.CURRENT_ITEM);
      if (property != null && !excludedPropertyNames.contains(property.getName())) {
        node = property.getParent();
        if (node != null && !trashService.isInTrash(node)) {
          if (node.isNodeType(NodetypeConstant.NT_RESOURCE)) {
            node = node.getParent();
          }
          if (node.isNodeType(NodetypeConstant.NT_FILE)) {
            String entityId = node.getInternalIdentifier();
            queueDlpService.addToQueue(FileDlpConnector.TYPE, entityId);
          }
        }
      }
      break;
    }
    return true;
  }

  @Override
  public void onError(Exception e, Context context) throws AdvancedActionException {
    LOGGER.error("Error while validating file", e);
  }
}
