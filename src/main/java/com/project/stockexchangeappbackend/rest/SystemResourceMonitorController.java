package com.project.stockexchangeappbackend.rest;

import com.project.stockexchangeappbackend.dto.ErrorResponse;
import com.project.stockexchangeappbackend.dto.SystemResourcesMonitorDTO;
import com.project.stockexchangeappbackend.repository.specification.SystemResourceMonitorSpecification;
import com.project.stockexchangeappbackend.service.SystemResourcesMonitorService;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;


@RestController
@RequestMapping("/api/system")
@Validated
@CrossOrigin("*")
@AllArgsConstructor
@Api(value = "System", description = "REST API for system resources' management", tags = "System")
public class SystemResourceMonitorController {

    private final SystemResourcesMonitorService systemResourcesMonitorService;
    private final ModelMapper modelMapper;

    @GetMapping("/resources")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ApiOperation(value = "Page and filter system resource info.", notes = "Required role: ADMIN")
    @ApiResponses({@ApiResponse(code = 200, message = "System resources' info was successfully retrieved."),
            @ApiResponse(code = 400, message = "The request could not be understood or was missing required parameters.",
                    response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized."),
            @ApiResponse(code = 403, message = "Access Denied")})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "Results page you want to retrieve (0..N).", defaultValue = "0"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "Number of records per page.", defaultValue = "20"),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "Sorting criteria in the format: property(,asc|desc). " +
                            "Default sort order is ascending. Multiple sort criteria are supported."),
            @ApiImplicitParam(name = "datetime>", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `timestamp` (omitted if null)."),
            @ApiImplicitParam(name = "datetime<", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `timestamp` (omitted if null).")
    })
    public Page<SystemResourcesMonitorDTO> getInfo(SystemResourceMonitorSpecification specification,
                                                   @ApiIgnore Pageable pageable) {
        return systemResourcesMonitorService.getInfo(pageable, specification)
                .map(info -> modelMapper.map(info, SystemResourcesMonitorDTO.class));
    }

}
