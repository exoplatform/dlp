package org.exoplatform.dlp.notification.channel.template;

import java.io.Writer;

import org.exoplatform.commons.api.notification.NotificationContext;
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
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;

@TemplateConfigs(templates = {
    @TemplateConfig(pluginId = DlpAdminDetectedItemPlugin.ID, template = "war:/notification/templates/DlpAdminDetectedItemPlugin.gtmpl"),
    @TemplateConfig(pluginId = DlpUserDetectedItemPlugin.ID, template = "war:/notification/templates/DlpUserDetectedItemPlugin.gtmpl"),
    @TemplateConfig(pluginId = DlpUserRestoredItemPlugin.ID, template = "war:/notification/templates/DlpUserRestoredItemPlugin.gtmpl")})

public class MailTemplateProvider extends TemplateProvider {

  private static final String ITEM_TITLE_NOTIFICATION_PARAM = "itemTitle";
  private static final String ITEM_TITLE = "ITEM_TITLE";

  /** Defines the template builder for DlpAdminDetectedItemPlugin*/
  private AbstractTemplateBuilder dlpAdminDetectedItem = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();

      NotificationInfo notification = ctx.getNotificationInfo();

      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      templateContext.put(ITEM_TITLE, notification.getValueOwnerParameter(ITEM_TITLE_NOTIFICATION_PARAM));
      templateContext.put("DLP_PAGE_URL", DlpUtils.getQuarantineRedirectURL());
      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);
      String subject = TemplateUtils.processSubject(templateContext);
      String body = TemplateUtils.processGroovy(templateContext);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());

      return messageInfo.subject(subject).body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }
  };

  /** Defines the template builder for DlpUserDetectedItemPlugin*/
  private AbstractTemplateBuilder dlpUserDetectedItem = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();

      NotificationInfo notification = ctx.getNotificationInfo();

      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);

      templateContext.put(ITEM_TITLE, notification.getValueOwnerParameter(ITEM_TITLE_NOTIFICATION_PARAM));
      templateContext.put("ITEM_AUTHOR", notification.getValueOwnerParameter("itemAuthor"));
      String subject = TemplateUtils.processSubject(templateContext);
      String body = TemplateUtils.processGroovy(templateContext);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());

      return messageInfo.subject(subject).body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }
  };

  /** Defines the template builder for DlpUserRestoredItemPlugin*/
  private AbstractTemplateBuilder dlpUserRestoredItem = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();

      NotificationInfo notification = ctx.getNotificationInfo();

      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
       SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);
      templateContext.put(ITEM_TITLE, notification.getValueOwnerParameter(ITEM_TITLE_NOTIFICATION_PARAM));
      templateContext.put("ITEM_URL", DlpUtils.getDlpRestoredUrl(notification.getValueOwnerParameter("itemReference")));

      String subject = TemplateUtils.processSubject(templateContext);
      String body = TemplateUtils.processGroovy(templateContext);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());

      return messageInfo.subject(subject).body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }
  };

  public MailTemplateProvider(InitParams initParams) {
    super(initParams);
    this.templateBuilders.put(PluginKey.key(DlpAdminDetectedItemPlugin.ID), dlpAdminDetectedItem);
    this.templateBuilders.put(PluginKey.key(DlpUserDetectedItemPlugin.ID), dlpUserDetectedItem);
    this.templateBuilders.put(PluginKey.key(DlpUserRestoredItemPlugin.ID), dlpUserRestoredItem);
  }
}
