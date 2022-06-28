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
package org.exoplatform.dlp.listener;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.dlp.connector.DlpServiceConnector;
import org.exoplatform.dlp.connector.FileDlpConnector;
import org.exoplatform.dlp.processor.DlpOperationProcessor;
import org.exoplatform.dlp.service.DlpPositiveItemService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;

public class PositiveFileDLPListener extends Listener<DlpPositiveItemService, String> {

  private DlpOperationProcessor dlpOperationProcessor;

  public PositiveFileDLPListener() {
    dlpOperationProcessor = CommonsUtils.getService(DlpOperationProcessor.class);
  }

  public void onEvent(Event<DlpPositiveItemService, String> event) {
    DlpServiceConnector fileConnector = dlpOperationProcessor.getConnectors().get(FileDlpConnector.TYPE);
    fileConnector.removePositiveItem(event.getData());
  }

}
