package org.exoplatform.dlp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DlpPositiveItem {

  private Long    id;

  private String  title;

  private String  author;

  private String  authorDisplayName;

  private String  type;

  private String  keywords;

  private boolean isExternal;

  private Long    detectionDate;

  private String  reference;

  private String  itemUrl;
}
