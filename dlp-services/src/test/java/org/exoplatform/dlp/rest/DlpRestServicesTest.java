package org.exoplatform.dlp.rest;

import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.dlp.utils.DlpUtils;
import org.exoplatform.portal.config.UserACL;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.core.Response;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DlpUtils.class, CommonsUtils.class })
public class DlpRestServicesTest {

  private DlpRestServices     dlpRestServices;

  @Mock
  private UserACL             userACL;

  private static final String DLP_GROUP = "/platform/dlp";

  @Before
  public void setUp() {
    dlpRestServices = new DlpRestServices();
    PowerMockito.mockStatic(DlpUtils.class);
    PowerMockito.mockStatic(CommonsUtils.class);
    when(CommonsUtils.getService(UserACL.class)).thenReturn(userACL);
    when(userACL.isUserInGroup(DLP_GROUP)).thenReturn(true);
    when(userACL.isSuperUser()).thenReturn(true);

  }

  @Test
  public void changeFeatureActivation() throws Exception {
    ExoFeatureService exoFeatureService = mock(ExoFeatureService.class);
    when(CommonsUtils.getService(ExoFeatureService.class)).thenReturn(exoFeatureService);
    when(DlpUtils.isDlpAdmin()).thenReturn(false);
    Response response = dlpRestServices.changeFeatureActivation("false");
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    doCallRealMethod().when(DlpUtils.class, "isDlpAdmin");
    Response response1 = dlpRestServices.changeFeatureActivation("true");
    verify(exoFeatureService, times(1)).saveActiveFeature("dlp", true);
    assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
    doThrow(new RuntimeException()).when(exoFeatureService).saveActiveFeature("dlp", true);
    Response response2 = dlpRestServices.changeFeatureActivation("true");
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response2.getStatus());
  }
}
