package org.exoplatform.dlp.notification.channel.template;

import java.io.Writer;
import java.util.Calendar;
import java.util.Locale;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.dlp.notification.plugin.DlpAdminDetectedItemPlugin;
import org.exoplatform.dlp.notification.plugin.DlpUserDetectedItemPlugin;
import org.exoplatform.dlp.notification.plugin.DlpUserRestoredItemPlugin;
import org.exoplatform.dlp.utils.DlpUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.utils.TimeConvertUtils;

@TemplateConfigs (
   templates = {
       @TemplateConfig( pluginId= DlpUserDetectedItemPlugin.ID, template="war:/intranet-notification/templates/DlpUserDetectedItemPlugin.gtmpl"),
       @TemplateConfig( pluginId= DlpAdminDetectedItemPlugin.ID, template="war:/intranet-notification/templates/DlpAdminDetectedItemPlugin.gtmpl"),
       @TemplateConfig( pluginId = DlpUserRestoredItemPlugin.ID, template = "war:/intranet-notification/templates/DlpUserRestoredItemPlugin.gtmpl")
   }
)
public class WebTemplateProvider extends TemplateProvider {
  private static final Log        LOG                          = ExoLogger.getLogger(WebTemplateProvider.class);

  private static final String ACCEPT_INVITATION_TO_CONNECT = "social/intranet-notification/confirmInvitationToConnect";
  private static final String REFUSE_INVITATION_TO_CONNECT = "social/intranet-notification/ignoreInvitationToConnect";
  private static final String VALIDATE_SPACE_REQUEST = "social/intranet-notification/validateRequestToJoinSpace";
  private static final String REFUSE_SPACE_REQUEST = "social/intranet-notification/refuseRequestToJoinSpace";
  private static final String ACCEPT_SPACE_INVITATION = "social/intranet-notification/acceptInvitationToJoinSpace";
  private static final String REFUSE_SPACE_INVITATION = "social/intranet-notification/ignoreInvitationToJoinSpace";
  private static final String MESSAGE_JSON_FILE_NAME = "message.json";

  /**
   * Defines the template builder for DlpUserDetectedItemPlugin
   */
  private AbstractTemplateBuilder dlpUserDetectedItem = new AbstractTemplateBuilder() {

    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      NotificationInfo notification = ctx.getNotificationInfo();

      String language = getLanguage(notification);

      TemplateContext templateContext = TemplateContext.newChannelInstance(getChannelKey(), notification.getKey().getId(), language);

      templateContext.put("isIntranet", "true");
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(notification.getLastModifiedDate());
      templateContext.put("READ", Boolean.valueOf(notification.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey())) ? "read" : "unread");
      templateContext.put("NOTIFICATION_ID", notification.getId());
      templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgoByTimeServer(cal.getTime(), "EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));
      templateContext.put("ITEM_TITLE", notification.getValueOwnerParameter("itemTitle"));
      //
      String body = TemplateUtils.processGroovy(templateContext);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      MessageInfo messageInfo = new MessageInfo();
      return messageInfo.body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }
  };


  /**
   * Defines the template builder for DlpAdminDetectedItemPlugin
   */
  private AbstractTemplateBuilder dlpAdminDetectedItem = new AbstractTemplateBuilder() {

    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      NotificationInfo notification = ctx.getNotificationInfo();

      String language = getLanguage(notification);

      TemplateContext templateContext = TemplateContext.newChannelInstance(getChannelKey(), notification.getKey().getId(), language);

      templateContext.put("isIntranet", "true");
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(notification.getLastModifiedDate());
      templateContext.put("READ", Boolean.valueOf(notification.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey())) ? "read" : "unread");
      templateContext.put("NOTIFICATION_ID", notification.getId());
      templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgoByTimeServer(cal.getTime(), "EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));
      templateContext.put("ITEM_TITLE", notification.getValueOwnerParameter("itemTitle"));
      templateContext.put("DLP_PAGE_URL", DlpUtils.getQuarantinePageUri(notification.getTo()));
      //
      String body = TemplateUtils.processGroovy(templateContext);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      MessageInfo messageInfo = new MessageInfo();
      return messageInfo.body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }
  };

  /**
   * Defines the template builder for DlpUserRestoredItemPlugin
   */
  private AbstractTemplateBuilder dlpUserRestoredItem = new AbstractTemplateBuilder() {

    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      NotificationInfo notification = ctx.getNotificationInfo();

      String language = getLanguage(notification);

      TemplateContext templateContext = TemplateContext.newChannelInstance(getChannelKey(), notification.getKey().getId(), language);

      templateContext.put("isIntranet", "true");
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(notification.getLastModifiedDate());
      templateContext.put("READ", Boolean.valueOf(notification.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey())) ? "read" : "unread");
      templateContext.put("NOTIFICATION_ID", notification.getId());
      templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgoByTimeServer(cal.getTime(), "EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));
      templateContext.put("ITEM_TITLE", notification.getValueOwnerParameter("itemTitle"));
      templateContext.put("ITEM_URL", DlpUtils.getDlpRestoredUri(notification.getValueOwnerParameter("itemReference")));

      String body = TemplateUtils.processGroovy(templateContext);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      MessageInfo messageInfo = new MessageInfo();
      return messageInfo.body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }
  };
  
  public WebTemplateProvider(InitParams initParams) {
    super(initParams);
    this.templateBuilders.put(PluginKey.key(DlpUserDetectedItemPlugin.ID), dlpUserDetectedItem);
    this.templateBuilders.put(PluginKey.key(DlpAdminDetectedItemPlugin.ID), dlpAdminDetectedItem);
    this.templateBuilders.put(PluginKey.key(DlpUserRestoredItemPlugin.ID), dlpUserRestoredItem);
  }
}
