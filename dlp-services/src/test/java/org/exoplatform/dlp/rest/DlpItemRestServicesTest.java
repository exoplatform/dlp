package org.exoplatform.dlp.rest;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.dlp.dto.DlpPositiveItem;
import org.exoplatform.dlp.processor.DlpOperationProcessor;
import org.exoplatform.dlp.service.DlpPositiveItemService;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DlpUtils.class, CommonsUtils.class})
public class DlpItemRestServicesTest {

  @Mock
  private DlpPositiveItemService dlpPositiveItemService;

  @Mock
  private DlpOperationProcessor  dlpOperationProcessor;

  @Mock
  private UserACL                userACL;

  private DlpItemRestServices    dlpItemRestServices;

  private static final String    DLP_GROUP = "/platform/dlp";

  @Before
  public void setUp() {
    this.dlpItemRestServices = new DlpItemRestServices(dlpPositiveItemService, dlpOperationProcessor);
    PowerMockito.mockStatic(DlpUtils.class);
    PowerMockito.mockStatic(CommonsUtils.class);
    when(CommonsUtils.getService(UserACL.class)).thenReturn(userACL);
    when(userACL.isUserInGroup(DLP_GROUP)).thenReturn(true);
    when(userACL.isSuperUser()).thenReturn(true);
  }

  @Test
  public void getDlpPositiveItems() throws Exception {
    List<DlpPositiveItem> dlpPositiveItems = new ArrayList<>();
    DlpPositiveItem dlpPositiveItem = new DlpPositiveItem();
    dlpPositiveItem.setId(1L);
    dlpPositiveItem.setKeywords("test");
    dlpPositiveItem.setTitle("title");
    dlpPositiveItems.add(dlpPositiveItem);
    when(DlpUtils.isDlpAdmin()).thenReturn(false);
    Response response = dlpItemRestServices.getDlpPositiveItems(0, 10);
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    doCallRealMethod().when(DlpUtils.class, "isDlpAdmin");
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
    when(DlpUtils.isDlpAdmin()).thenReturn(false);
    Response response = dlpItemRestServices.deleteDlpDocumentById(1L);
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    doCallRealMethod().when(DlpUtils.class, "isDlpAdmin");
    Response response1 = dlpItemRestServices.deleteDlpDocumentById(1L);
    verify(dlpPositiveItemService, times(1)).deleteDlpPositiveItem(1L);
    assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
  }

  @Test
  public void getDlpKeywords() throws Exception {
    String keywords = "test, anything";
    when(DlpUtils.isDlpAdmin()).thenReturn(false);
    Response response = dlpItemRestServices.getDlpKeywords();
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    doCallRealMethod().when(DlpUtils.class, "isDlpAdmin");
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
    when(DlpUtils.isDlpAdmin()).thenReturn(false);
    Response response = dlpItemRestServices.setDlpKeywords(keywords);
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    doCallRealMethod().when(DlpUtils.class, "isDlpAdmin");
    Response response1 = dlpItemRestServices.setDlpKeywords(keywords);
    verify(dlpOperationProcessor, times(1)).setKeywords(keywords);;
    assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
    doThrow(new RuntimeException()).when(dlpOperationProcessor).setKeywords(keywords);
    Response response2 = dlpItemRestServices.setDlpKeywords(keywords);
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response2.getStatus());
  }

  @Test
  public void restoreDlpPositiveItems() throws Exception {
    when(DlpUtils.isDlpAdmin()).thenReturn(false);
    Response response = dlpItemRestServices.restoreDlpPositiveItems(1L);
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    doCallRealMethod().when(DlpUtils.class, "isDlpAdmin");
    Response response1 = dlpItemRestServices.restoreDlpPositiveItems(1L);
    verify(dlpPositiveItemService, times(1)).restoreDlpPositiveItem(1L);;
    assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
    doThrow(new RuntimeException()).when(dlpPositiveItemService).restoreDlpPositiveItem(1L);
    Response response2 = dlpItemRestServices.restoreDlpPositiveItems(1L);
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response2.getStatus());
  }
}
