package org.exoplatform.dlp.rest;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.dlp.utils.DlpUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;


@Path("/dlp")
@Tag(name = "/dlp", description = "Manage dlp activation")
public class DlpRestServices implements ResourceContainer {

  public static final String DLP_FEATURE = "dlp";

  private static final Log   LOG         = ExoLogger.getLogger(DlpRestServices.class);

  @Path("/changeFeatureActivation/{isActive}")
  @PUT
  @Produces(MediaType.TEXT_PLAIN)
  @RolesAllowed("users")
  @Operation(summary = "Change a feature activation.", method = "GET", description = "Change a feature activation")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
      @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response changeFeatureActivation(@Parameter(description = "Is active feature", required = true)
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
