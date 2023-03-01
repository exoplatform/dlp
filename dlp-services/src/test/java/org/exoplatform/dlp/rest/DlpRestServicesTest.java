package org.exoplatform.dlp.rest;

import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.dlp.utils.DlpUtils;
import org.exoplatform.portal.config.UserACL;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.core.Response;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DlpRestServicesTest {

  private static final MockedStatic<DlpUtils>     DLP_UTILS     = mockStatic(DlpUtils.class);

  private static final MockedStatic<CommonsUtils> COMMONS_UTILS = mockStatic(CommonsUtils.class);

  private DlpRestServices     dlpRestServices;

  @Mock
  private UserACL             userACL;

  private static final String DLP_GROUP = "/platform/dlp";

  @AfterClass
  public static void afterRunBare() throws Exception { // NOSONAR
    DLP_UTILS.close();
    COMMONS_UTILS.close();
  }

  @Before
  public void setUp() {
    dlpRestServices = new DlpRestServices();

    COMMONS_UTILS.when(() -> CommonsUtils.getService(UserACL.class)).thenReturn(userACL);
    when(userACL.isUserInGroup(DLP_GROUP)).thenReturn(true);
  }

  @Test
  public void changeFeatureActivation() throws Exception {
    ExoFeatureService exoFeatureService = mock(ExoFeatureService.class);
    COMMONS_UTILS.when(() -> CommonsUtils.getService(ExoFeatureService.class)).thenReturn(exoFeatureService);
    DLP_UTILS.when(() -> DlpUtils.isDlpAdmin()).thenReturn(false);
    Response response = dlpRestServices.changeFeatureActivation("false");
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    DLP_UTILS.when(() -> DlpUtils.isDlpAdmin()).thenCallRealMethod();
    Response response1 = dlpRestServices.changeFeatureActivation("true");
    verify(exoFeatureService, times(1)).saveActiveFeature("dlp", true);
    assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
    doThrow(new RuntimeException()).when(exoFeatureService).saveActiveFeature("dlp", true);
    Response response2 = dlpRestServices.changeFeatureActivation("true");
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response2.getStatus());
  }
}
