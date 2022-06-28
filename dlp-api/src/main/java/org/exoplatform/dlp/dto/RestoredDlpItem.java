package org.exoplatform.dlp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestoredDlpItem {

  private Long   detectionDate;

  private Long   id;

  private String reference;
}
