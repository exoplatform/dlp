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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.exoplatform.dlp.dto.DlpPositiveItem;
import org.exoplatform.dlp.processor.DlpOperationProcessor;
import org.exoplatform.dlp.service.DlpPositiveItemService;
import org.exoplatform.dlp.utils.DlpUtils;
import org.exoplatform.portal.rest.CollectionEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

@Path("/dlp/items")
@Tag(name = "/dlp/items", description = "Manages Dlp positive items") // NOSONAR
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
  @Operation(summary = "Retrieves the list of dlp positive items", method = "GET",  description = "Return list of dlp positive items in json format")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "50", description = "Internal server error") })
  public Response getDlpPositiveItems(@Parameter(description = "Offset") @Schema(defaultValue = "0")
  @QueryParam("offset")
  int offset,
                                      @Parameter(description = "Limit") @Schema(defaultValue = "20")
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
  @Operation(summary = "Delete a document by id", method = "DELETE", description = "This delete the document if the authenticated user is a super manager")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
    public Response deleteDlpDocumentById(@Parameter(description = "Document id", required = true)
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
  @Operation(summary = "Retrieves the list of dlp keywords", method = "GET", description = "Return list of dlp keywords in json format")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
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
  @Operation(summary = "set dlp keywords", method = "POST", description = "set the dlp keywords")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response setDlpKeywords(@Parameter(description = "keywords", required = true)
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
  @Operation(summary = "Restore the dlp positive items", method = "PUT", description = "Return the restored positive item")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response restoreDlpPositiveItems(@Parameter(description = "Document id", required = true)
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
