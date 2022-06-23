package org.exoplatform.dlp.rest;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.dlp.dto.DlpPositiveItem;
import org.exoplatform.dlp.processor.DlpOperationProcessor;
import org.exoplatform.dlp.service.DlpPositiveItemService;
import org.exoplatform.dlp.utils.DlpUtils;
import org.exoplatform.portal.rest.CollectionEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/dlp/items")
@Api(value = "/dlp/items", description = "Manages Dlp positive items") // NOSONAR
public class DlpItemRestServices implements ResourceContainer {

  private static final Log       LOG = ExoLogger.getLogger(DlpItemRestServices.class);

  private DlpPositiveItemService dlpPositiveItemService;

  private DlpOperationProcessor  dlpOperationProcessor;

  public DlpItemRestServices(DlpPositiveItemService dlpPositiveItemService, DlpOperationProcessor dlpOperationProcessor) {
    this.dlpPositiveItemService = dlpPositiveItemService;
    this.dlpOperationProcessor = dlpOperationProcessor;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Retrieves the list of dlp positive items", httpMethod = "GET", response = Response.class, produces = "application/json", notes = "Return list of dlp positive items in json format")
  @ApiResponses(value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
      @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
      @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error") })
  public Response getDlpPositiveItems(@ApiParam(value = "Offset", required = false, defaultValue = "0")
  @QueryParam("offset")
  int offset,
                                      @ApiParam(value = "Limit", required = false, defaultValue = "20")
                                      @QueryParam("limit")
                                      int limit) {
    if (!DlpUtils.isDlpAdmin()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    try {
      List<DlpPositiveItem> dlpPositiveItems = dlpPositiveItemService.getDlpPositivesItems(offset, limit);
      Long size = dlpPositiveItemService.getDlpPositiveItemsCount();
      CollectionEntity<DlpPositiveItem> collectionDlpPositiveItem = new CollectionEntity<>(dlpPositiveItems,
                                                                                           offset,
                                                                                           limit,
                                                                                           size.intValue());
      return Response.ok(collectionDlpPositiveItem).build();
    } catch (Exception e) {
      LOG.error("Unknown error occurred while getting dlp positive items", e);
      return Response.serverError().build();
    }
  }

  @DELETE
  @Path("/item/{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Delete a document by id", httpMethod = "DELETE", response = Response.class, notes = "This delete the document if the authenticated user is a super manager")
  @ApiResponses(value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
      @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
      @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error") })
  public Response deleteDlpDocumentById(@ApiParam(value = "Document id", required = true)
  @PathParam("id")
  Long id) {
    if (!DlpUtils.isDlpAdmin()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    dlpPositiveItemService.deleteDlpPositiveItem(id);
    return Response.ok().build();
  }

  @GET
  @Path("/keywords")
  @RolesAllowed("users")
  @ApiOperation(value = "Retrieves the list of dlp keywords", httpMethod = "GET", response = Response.class, produces = "application/json", notes = "Return list of dlp keywords in json format")
  @ApiResponses(value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
      @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
      @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error") })
  public Response getDlpKeywords() {
    if (!DlpUtils.isDlpAdmin()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    try {
      String keywords = dlpOperationProcessor.getKeywords();
      return Response.ok(keywords).build();
    } catch (Exception e) {
      LOG.error("Unknown error occurred while getting dlp keywords", e);
      return Response.serverError().build();
    }
  }

  @POST
  @Path("/keywords")
  @RolesAllowed("users")
  @ApiOperation(value = "set dlp keywords", httpMethod = "POST", response = Response.class, produces = "application/json", notes = "set the dlp keywords")
  @ApiResponses(value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
      @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
      @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error") })
  public Response setDlpKeywords(@ApiParam(value = "keywords", required = true)
  String keywords) {
    if (!DlpUtils.isDlpAdmin()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    try {
      dlpOperationProcessor.setKeywords(keywords);
      return Response.ok().entity("{\"result\":\"" + keywords + "\"}").build();
    } catch (Exception e) {
      LOG.error("Unknown error occurred while setting dlp keywords", e);
      return Response.serverError().build();
    }
  }

  @PUT
  @Path("item/restore/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Restore the dlp positive items", httpMethod = "PUT", response = Response.class, produces = "application/json", notes = "Return the restored positive item")
  @ApiResponses(value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
      @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
      @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error") })
  public Response restoreDlpPositiveItems(@ApiParam(value = "Document id", required = true)
  @PathParam("id")
  Long id) {
    if (!DlpUtils.isDlpAdmin()) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    try {
      dlpPositiveItemService.restoreDlpPositiveItem(id);
      return Response.ok().build();
    } catch (Exception e) {
      LOG.error("Unknown error occurred while restoring dlp positive items", e);
      return Response.serverError().build();
    }
  }
}
