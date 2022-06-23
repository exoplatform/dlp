package org.exoplatform.dlp.rest;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.dlp.utils.DlpUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/dlp")
@Api(value = "/dlp")
public class DlpRestServices implements ResourceContainer {

  public static final String DLP_FEATURE = "dlp";

  private static final Log   LOG         = ExoLogger.getLogger(DlpRestServices.class);

  @Path("/changeFeatureActivation/{isActive}")
  @PUT
  @Produces(MediaType.TEXT_PLAIN)
  @RolesAllowed("users")
  @ApiOperation(value = "Change a feature activation.", httpMethod = "GET", response = Response.class, produces = "text/plain")
  @ApiResponses(value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
      @ApiResponse(code = HTTPStatus.BAD_REQUEST, message = "Invalid query input"),
      @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
      @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), })
  public Response changeFeatureActivation(@ApiParam(value = "Is active feature", required = true)
  @PathParam("isActive")
  String isActive) {

    try {
      if (!DlpUtils.isDlpAdmin()) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }
      ExoFeatureService featureService = CommonsUtils.getService(ExoFeatureService.class);
      boolean isActiveBool = Boolean.parseBoolean(isActive);
      featureService.saveActiveFeature(DLP_FEATURE, isActiveBool);
      return Response.ok().type(MediaType.TEXT_PLAIN).build();
    } catch (Exception e) {
      LOG.warn("Error when changing feature activation with name '{}'", DLP_FEATURE, e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
}
