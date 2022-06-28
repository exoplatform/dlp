package org.exoplatform.dlp.service;

import org.exoplatform.dlp.domain.RestoredDlpItemEntity;
import org.exoplatform.dlp.dto.RestoredDlpItem;

public interface RestoredDlpItemService {

  void addRestoredDlpItem(RestoredDlpItemEntity restoredDlpItemEntity);

  void deleteRestoredDlpItem(Long itemId);

  RestoredDlpItem getRestoredDlpItemByReference(String itemReference) throws Exception;
}
