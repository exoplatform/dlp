package org.exoplatform.dlp.utils;

import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.dlp.connector.DlpServiceConnector;
import org.exoplatform.dlp.processor.DlpOperationProcessor;
import org.exoplatform.portal.config.UserACL;

public class DlpUtils {
  
  public final static ArgumentLiteral<Long> DLP_DETECTED_ITEM_ID = new ArgumentLiteral<>(Long.class, "dlp_detected_item_id");

  public final static ArgumentLiteral<String> DLP_RESTORED_ITEM_TITLE = new ArgumentLiteral<>(String.class, "dlp_restored_item_title");

  public final static ArgumentLiteral<String> DLP_RESTORED_ITEM_REFERENCE = new ArgumentLiteral<>(String.class, "dlp_restored_item_reference");

  public final static ArgumentLiteral<String> DLP_RESTORED_ITEM_AUTHOR = new ArgumentLiteral<>(String.class, "dlp_restored_item_author");
  
  public static final String TYPE = "file";
  
  private static final String COLLABORATION_WS = "collaboration";
  
  private static final String DLP_GROUP = "/platform/dlp";
  
  public static final String PRIVATE_PATH = "/private";
  
  /**
   * Gets the link of quarantine page
   *
   * @return
   */
  public static String getQuarantinePageUri(String username) {
    return "/" + PortalContainer.getCurrentPortalContainerName() + "/g/:platform:dlp/dlp-quarantine";
  }

  /**
   * Gets the link of restored dlp file
   * @return
   */
  public static String getDlpRestoredUri(String reference) {
    DlpOperationProcessor dlpOperationProcessor = CommonsUtils.getService(DlpOperationProcessor.class);
    DlpServiceConnector dlpServiceConnector = (DlpServiceConnector) dlpOperationProcessor.getConnectors().get(TYPE);
    return dlpServiceConnector != null ?  "/" + dlpServiceConnector.getItemUrl(reference) : new String();
  }
  
  /**
   * Get the Redirect quarantine page url
   *
   * @return the quarantine page url
   */
  public static String getQuarantineRedirectURL(String username) {
    String portal = PortalContainer.getCurrentPortalContainerName();
    return new StringBuffer(CommonsUtils.getCurrentDomain()).append("/").append(portal).append("/").append("g/:platform:dlp/dlp-quarantine").toString();
  }

  /**
   * Get the Redirect restored item url
   *
   * @return the Redirect restored item url
   */
  public static String getDlpRestoredUrl(String reference) {
    String basePrivateRestUrl = new StringBuffer(CommonsUtils.getCurrentDomain()).append("/").append(CommonsUtils.getRestContextName()).append(PRIVATE_PATH).toString();
    return basePrivateRestUrl + "/documents/view/" + COLLABORATION_WS + "/" + reference;
  }
  
  public static boolean isDlpAdmin() {
    UserACL userACL = CommonsUtils.getService(UserACL.class);
    return userACL.isSuperUser() || userACL.isUserInGroup(DLP_GROUP);
  }
}
