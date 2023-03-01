package org.exoplatform.dlp.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.dlp.dto.DlpPositiveItem;
import org.exoplatform.dlp.processor.DlpOperationProcessor;
import org.exoplatform.dlp.service.DlpPositiveItemService;
import org.exoplatform.dlp.utils.DlpUtils;
import org.exoplatform.portal.config.UserACL;

@RunWith(MockitoJUnitRunner.class)
public class DlpItemRestServicesTest {

  private static final MockedStatic<DlpUtils>     DLP_UTILS     = mockStatic(DlpUtils.class);

  private static final MockedStatic<CommonsUtils> COMMONS_UTILS = mockStatic(CommonsUtils.class);

  @Mock
  private DlpPositiveItemService dlpPositiveItemService;

  @Mock
  private DlpOperationProcessor  dlpOperationProcessor;

  @Mock
  private UserACL                userACL;

  private DlpItemRestServices    dlpItemRestServices;

  private static final String    DLP_GROUP = "/platform/dlp";

  @AfterClass
  public static void afterRunBare() throws Exception { // NOSONAR
    DLP_UTILS.close();
    COMMONS_UTILS.close();
  }

  @Before
  public void setUp() {
    this.dlpItemRestServices = new DlpItemRestServices(dlpPositiveItemService, dlpOperationProcessor);

    COMMONS_UTILS.when(() -> CommonsUtils.getService(UserACL.class)).thenReturn(userACL);
    when(userACL.isUserInGroup(DLP_GROUP)).thenReturn(true);
  }

  @Test
  public void getDlpPositiveItems() throws Exception {
    List<DlpPositiveItem> dlpPositiveItems = new ArrayList<>();
    DlpPositiveItem dlpPositiveItem = new DlpPositiveItem();
    dlpPositiveItem.setId(1L);
    dlpPositiveItem.setKeywords("test");
    dlpPositiveItem.setTitle("title");
    dlpPositiveItems.add(dlpPositiveItem);
    DLP_UTILS.when(() -> DlpUtils.isDlpAdmin()).thenReturn(false);
    Response response = dlpItemRestServices.getDlpPositiveItems(0, 10);
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    DLP_UTILS.when(() -> DlpUtils.isDlpAdmin()).thenCallRealMethod();
    when(dlpPositiveItemService.getDlpPositivesItems(anyInt(), anyInt())).thenReturn(dlpPositiveItems);
    when(dlpPositiveItemService.getDlpPositiveItemsCount()).thenReturn((long) dlpPositiveItems.size());
    Response response1 = dlpItemRestServices.getDlpPositiveItems(0, 10);
    assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
    doThrow(new RuntimeException()).when(dlpPositiveItemService).getDlpPositivesItems(anyInt(), anyInt());
    Response response2 = dlpItemRestServices.getDlpPositiveItems(0, 10);
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response2.getStatus());

  }

  @Test
  public void deleteDlpDocumentById() throws Exception {
    DLP_UTILS.when(() -> DlpUtils.isDlpAdmin()).thenReturn(false);
    Response response = dlpItemRestServices.deleteDlpDocumentById(1L);
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    DLP_UTILS.when(() -> DlpUtils.isDlpAdmin()).thenCallRealMethod();
    Response response1 = dlpItemRestServices.deleteDlpDocumentById(1L);
    verify(dlpPositiveItemService, times(1)).deleteDlpPositiveItem(1L);
    assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
  }

  @Test
  public void getDlpKeywords() throws Exception {
    String keywords = "test, anything";
    DLP_UTILS.when(() -> DlpUtils.isDlpAdmin()).thenReturn(false);
    Response response = dlpItemRestServices.getDlpKeywords();
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    DLP_UTILS.when(() -> DlpUtils.isDlpAdmin()).thenCallRealMethod();
    when(dlpOperationProcessor.getKeywords()).thenReturn(keywords);
    Response response1 = dlpItemRestServices.getDlpKeywords();
    assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
    doThrow(new RuntimeException()).when(dlpOperationProcessor).getKeywords();
    Response response2 = dlpItemRestServices.getDlpKeywords();
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response2.getStatus());
  }

  @Test
  public void setDlpKeywords() throws Exception {
    String keywords = "test, anything";
    DLP_UTILS.when(() -> DlpUtils.isDlpAdmin()).thenReturn(false);
    Response response = dlpItemRestServices.setDlpKeywords(keywords);
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    DLP_UTILS.when(() -> DlpUtils.isDlpAdmin()).thenCallRealMethod();
    Response response1 = dlpItemRestServices.setDlpKeywords(keywords);
    verify(dlpOperationProcessor, times(1)).setKeywords(keywords);;
    assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
    doThrow(new RuntimeException()).when(dlpOperationProcessor).setKeywords(keywords);
    Response response2 = dlpItemRestServices.setDlpKeywords(keywords);
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response2.getStatus());
  }

  @Test
  public void restoreDlpPositiveItems() throws Exception {
    DLP_UTILS.when(() -> DlpUtils.isDlpAdmin()).thenReturn(false);
    Response response = dlpItemRestServices.restoreDlpPositiveItems(1L);
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    DLP_UTILS.when(() -> DlpUtils.isDlpAdmin()).thenCallRealMethod();
    Response response1 = dlpItemRestServices.restoreDlpPositiveItems(1L);
    verify(dlpPositiveItemService, times(1)).restoreDlpPositiveItem(1L);;
    assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
    doThrow(new RuntimeException()).when(dlpPositiveItemService).restoreDlpPositiveItem(1L);
    Response response2 = dlpItemRestServices.restoreDlpPositiveItems(1L);
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response2.getStatus());
  }
}
