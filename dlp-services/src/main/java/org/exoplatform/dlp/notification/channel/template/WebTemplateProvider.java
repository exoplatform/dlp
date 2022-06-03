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
import org.exoplatform.webui.utils.TimeConvertUtils;

@TemplateConfigs (
   templates = {
       @TemplateConfig( pluginId= DlpUserDetectedItemPlugin.ID, template="war:/intranet-notification/templates/DlpUserDetectedItemPlugin.gtmpl"),
       @TemplateConfig( pluginId= DlpAdminDetectedItemPlugin.ID, template="war:/intranet-notification/templates/DlpAdminDetectedItemPlugin.gtmpl"),
       @TemplateConfig( pluginId = DlpUserRestoredItemPlugin.ID, template = "war:/intranet-notification/templates/DlpUserRestoredItemPlugin.gtmpl")
   }
)
public class WebTemplateProvider extends TemplateProvider {

  private static final String IS_INTRANET = "isIntranet";
  private static final String READ = "READ";
  private static final String NOTIFICATION_ID = "NOTIFICATION_ID";
  private static final String LAST_UPDATED_TIME = "LAST_UPDATED_TIME";
  private static final String ITEM_TITLE = "ITEM_TITLE";
  private static final String DLP_PAGE_URL = "DLP_PAGE_URL";
  private static final String ITEM_URL = "ITEM_URL";
  private static final String DATE_FORMAT = "EE, dd yyyy";
  private static final String ITEM_TITLE_NOTIFICATION_PARAM = "itemTitle";
  private static final String READ_NOTIFICATION_PARAM = "read";
  private static final String UNREAD_NOTIFICATION_PARAM = "unread";
  
  /**
   * Defines the template builder for DlpUserDetectedItemPlugin
   */
  private AbstractTemplateBuilder dlpUserDetectedItem = new AbstractTemplateBuilder() {

    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      NotificationInfo notification = ctx.getNotificationInfo();

      String language = getLanguage(notification);

      TemplateContext templateContext = TemplateContext.newChannelInstance(getChannelKey(), notification.getKey().getId(), language);

      templateContext.put(IS_INTRANET, "true");
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(notification.getLastModifiedDate());
      templateContext.put(READ, Boolean.TRUE.equals(Boolean.valueOf(notification.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey())) ? READ_NOTIFICATION_PARAM : UNREAD_NOTIFICATION_PARAM));
      templateContext.put(NOTIFICATION_ID, notification.getId());
      templateContext.put(LAST_UPDATED_TIME, TimeConvertUtils.convertXTimeAgoByTimeServer(cal.getTime(), DATE_FORMAT, new Locale(language), TimeConvertUtils.YEAR));
      templateContext.put(ITEM_TITLE, notification.getValueOwnerParameter(ITEM_TITLE_NOTIFICATION_PARAM));
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

      templateContext.put(IS_INTRANET, "true");
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(notification.getLastModifiedDate());
      templateContext.put(READ, Boolean.TRUE.equals(Boolean.valueOf(notification.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey()))) ? READ_NOTIFICATION_PARAM : UNREAD_NOTIFICATION_PARAM);
      templateContext.put(NOTIFICATION_ID, notification.getId());
      templateContext.put(LAST_UPDATED_TIME, TimeConvertUtils.convertXTimeAgoByTimeServer(cal.getTime(), DATE_FORMAT, new Locale(language), TimeConvertUtils.YEAR));
      templateContext.put(ITEM_TITLE, notification.getValueOwnerParameter(ITEM_TITLE_NOTIFICATION_PARAM));
      templateContext.put(DLP_PAGE_URL, DlpUtils.getQuarantinePageUri());
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

      templateContext.put(IS_INTRANET, "true");
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(notification.getLastModifiedDate());
      templateContext.put(READ, Boolean.TRUE.equals(Boolean.valueOf(notification.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey()))) ? READ_NOTIFICATION_PARAM : UNREAD_NOTIFICATION_PARAM);
      templateContext.put(NOTIFICATION_ID, notification.getId());
      templateContext.put(LAST_UPDATED_TIME, TimeConvertUtils.convertXTimeAgoByTimeServer(cal.getTime(), DATE_FORMAT, new Locale(language), TimeConvertUtils.YEAR));
      templateContext.put(ITEM_TITLE, notification.getValueOwnerParameter(ITEM_TITLE_NOTIFICATION_PARAM));
      templateContext.put(ITEM_URL, DlpUtils.getDlpRestoredUri(notification.getValueOwnerParameter("itemReference")));

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
