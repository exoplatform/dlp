/*
 * Copyright (C) 2026 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <gnu.org/licenses>.
 */
package org.exoplatform.dlp.notification.channel.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.mockito.MockedStatic;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.NotificationPluginUtils;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.mail.MailPalette;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.dlp.notification.plugin.DlpAdminDetectedItemPlugin;
import org.exoplatform.dlp.notification.plugin.DlpUserDetectedItemPlugin;
import org.exoplatform.dlp.notification.plugin.DlpUserRestoredItemPlugin;
import org.exoplatform.dlp.utils.DlpUtils;
import org.exoplatform.portal.branding.BrandingService;
import org.exoplatform.portal.branding.MailDocumentShell;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.notification.LinkProviderUtils;

import groovy.text.GStringTemplateEngine;

/**
 * The mail of a DLP notification is written in its recipient's palette: the
 * builder binds it, and the template, which builds its own header, renders on
 * it, logo included.
 */
public class MailTemplatePaletteTest {

  private static final String TEMPLATES = "../dlp-webapp/src/main/webapp/WEB-INF/notification/templates/";

  @Test
  public void theRecipientsPaletteIsBoundToEveryMailsContext() {
    for (String pluginId : new String[] { DlpAdminDetectedItemPlugin.ID, DlpUserDetectedItemPlugin.ID,
        DlpUserRestoredItemPlugin.ID }) {
      TemplateContext templateContext = build(pluginId);
      assertEquals(pluginId, "dark", MailPalette.of(templateContext).get(MailPalette.MODE));
      assertEquals(pluginId, "#1E2328", MailPalette.of(templateContext).get("surface"));
    }
  }

  @Test
  public void theTemplatesAreWrittenInTheRecipientsPaletteWithTheLogo() throws Exception {
    for (String name : new String[] { "DlpAdminDetectedItemPlugin", "DlpUserDetectedItemPlugin", "DlpUserRestoredItemPlugin" }) {
      String html = render(TEMPLATES + name + ".gtmpl", darkPalette());
      assertTrue(name + html, html.contains("bgcolor=\"#14181B\""));
      assertTrue(name + html, html.contains("color:#E6E9EC"));
      assertTrue(name + html, html.contains("class=\"mail-bg mail-link mail-border\""));
      assertTrue(name + " the body text cell is painted", html.contains("<td align=\"left\" bgcolor=\"#1E2328\" style=\"background-color: #1E2328; line-height: 18px;font-size: 14px;color:#E6E9EC;"));
      assertTrue(name + " the body cell is painted", html.contains("bgcolor=\"#1E2328\" style=\"background-color: #1E2328; padding: 25px 0 25px\" class=\"mail-surface\""));
      assertTrue(name + " the body cells carry the surface class", html.contains("class=\"mail-surface mail-text\""));
      assertTrue(name + " shows the logo", html.contains("ACME"));
      for (String literal : new String[] { "#f5f5f5", "#d6d6d6", "#476a98", "#ccc", "#333", "#476A9C" }) {
        assertFalse(name + " no light literal left: " + literal, html.contains(literal));
      }
    }
  }

  @Test
  public void aContextWithoutPaletteRendersInTheLightDefaults() throws Exception {
    String html = render(TEMPLATES + "DlpUserDetectedItemPlugin.gtmpl", null);
    assertTrue(html, html.contains("bgcolor=\"#F0F0F0\""));
    assertTrue(html, html.contains("color:#20282C"));
    assertFalse(html, html.contains("null"));
  }

  private static TemplateContext build(String pluginId) {
    BrandingService brandingService = mock(BrandingService.class);
    when(brandingService.getDisplayMode("mary")).thenReturn("dark");
    when(brandingService.getMailPalette(true)).thenReturn(new HashMap<>(Map.of("surface", "#1E2328")));
    IdentityManager identityManager = mock(IdentityManager.class);
    Identity receiver = mock(Identity.class);
    Profile profile = mock(Profile.class);
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary", true)).thenReturn(receiver);
    when(receiver.getProfile()).thenReturn(profile);
    when(receiver.getRemoteId()).thenReturn("mary");
    when(profile.getProperty(Profile.FIRST_NAME)).thenReturn("Mary");
    PortalContainer container = mock(PortalContainer.class);
    when(container.getComponentInstanceOfType(IdentityManager.class)).thenReturn(identityManager);
    NotificationInfo notification = mock(NotificationInfo.class);
    when(notification.getKey()).thenReturn(new PluginKey(pluginId));
    when(notification.getValueOwnerParameter(anyString())).thenReturn("value");
    when(notification.getTo()).thenReturn("mary");
    NotificationContext ctx = mock(NotificationContext.class);
    when(ctx.getNotificationInfo()).thenReturn(notification);
    AtomicReference<TemplateContext> rendered = new AtomicReference<>();

    try (MockedStatic<CommonsUtils> commonsUtils = mockStatic(CommonsUtils.class);
         MockedStatic<PortalContainer> portalContainer = mockStatic(PortalContainer.class);
         MockedStatic<NotificationPluginUtils> pluginUtils = mockStatic(NotificationPluginUtils.class);
         MockedStatic<LinkProviderUtils> linkProviderUtils = mockStatic(LinkProviderUtils.class);
         MockedStatic<DlpUtils> dlpUtils = mockStatic(DlpUtils.class);
         MockedStatic<TemplateUtils> templateUtils = mockStatic(TemplateUtils.class)) {
      commonsUtils.when(() -> CommonsUtils.getService(BrandingService.class)).thenReturn(brandingService);
      portalContainer.when(PortalContainer::getInstance).thenReturn(container);
      pluginUtils.when(() -> NotificationPluginUtils.getLanguage(anyString())).thenReturn("en");
      linkProviderUtils.when(() -> LinkProviderUtils.getRedirectUrl(anyString(), anyString())).thenReturn("/settings");
      linkProviderUtils.when(LinkProviderUtils::getBaseUrl).thenReturn("https://acme.test");
      dlpUtils.when(DlpUtils::getQuarantineRedirectURL).thenReturn("/quarantine");
      dlpUtils.when(() -> DlpUtils.getDlpRestoredUrl(any())).thenReturn("/restored");
      templateUtils.when(() -> TemplateUtils.processSubject(any(TemplateContext.class))).thenReturn("subject");
      templateUtils.when(() -> TemplateUtils.processGroovy(any(TemplateContext.class))).thenAnswer(call -> {
        rendered.set(call.getArgument(0));
        return "body";
      });

      new MailTemplateProvider(new InitParams()).getTemplateBuilder().get(PluginKey.key(pluginId)).buildMessage(ctx);
    }
    return rendered.get();
  }

  private static Map<String, String> darkPalette() {
    Map<String, String> palette = new HashMap<>(MailDocumentShell.LIGHT_DEFAULTS);
    palette.put(MailDocumentShell.BACKGROUND, "#14181B");
    palette.put(MailDocumentShell.SURFACE, "#1E2328");
    palette.put(MailDocumentShell.TEXT, "#E6E9EC");
    palette.put(MailDocumentShell.SECONDARY_TEXT, "#9AA3AB");
    palette.put(MailDocumentShell.LINK, "#5FA9AC");
    palette.put(MailDocumentShell.BORDER, "#3A4148");
    palette.put(MailDocumentShell.PRIMARY, "#2F6F73");
    palette.put(MailDocumentShell.COMPANY_NAME, "ACME");
    palette.put(MailPalette.MODE, "dark");
    return palette;
  }

  private static String render(String template, Map<String, String> palette) throws Exception {
    StringWriter writer = new StringWriter();
    TemplateContext ctx = new TemplateContext();
    ctx.put("_writer", writer);
    ctx.put("_templateContext", ctx);
    ctx.put("_ctx", new Element(ctx, writer));
    for (String key : new String[] { "ITEM_TITLE", "ITEM_AUTHOR", "ITEM_URL", "DLP_PAGE_URL" }) {
      ctx.put(key, key.toLowerCase());
    }
    if (palette != null) {
      ctx.put(MailPalette.CONTEXT_KEY, palette);
    }
    try (MockedStatic<CommonsUtils> commonsUtils = mockStatic(CommonsUtils.class)) {
      commonsUtils.when(CommonsUtils::getCurrentDomain).thenReturn("https://acme.test");
      new GStringTemplateEngine().createTemplate(Files.readString(Paths.get(template), StandardCharsets.UTF_8))
                                 .make(ctx)
                                 .writeTo(writer);
    }
    return writer.toString();
  }

  /** The template's helper, as the mail channel gives it: resources and includes. */
  public static class Element {
    private final TemplateContext ctx;

    private final StringWriter    writer;

    public Element(TemplateContext ctx, StringWriter writer) {
      this.ctx = ctx;
      this.writer = writer;
    }

    public String appRes(String key, String... args) {
      return key;
    }

    public void include(String path, TemplateContext context) {
      if (path.endsWith("NotificationLogo.gtmpl")) {
        writer.write(MailPalette.logoHtml(MailPalette.of(ctx)));
      } else {
        writer.write("<include " + path + "/>");
      }
    }
  }
}
