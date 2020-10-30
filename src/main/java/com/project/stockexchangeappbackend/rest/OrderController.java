package com.project.stockexchangeappbackend.rest;

import com.project.stockexchangeappbackend.dto.ErrorResponse;
import com.project.stockexchangeappbackend.dto.OrderDTO;
import com.project.stockexchangeappbackend.service.OrderService;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@CrossOrigin("*")
@AllArgsConstructor
@Api(value = "Orders", description = "REST API for orders' management", tags = "Orders")
@ApiResponses({
        @ApiResponse(code = 400, message = "The request could not be understood or was missing required parameters.",
                response = ErrorResponse.class),
        @ApiResponse(code = 401, message = "Unauthorized."),
        @ApiResponse(code = 403, message = "Access Denied.")
})
public class OrderController {

    private final OrderService orderService;
    private final ModelMapper mapper;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @ApiOperation(value = "Retrieve order by id", response = OrderDTO.class)
    @ApiResponses({@ApiResponse(code = 200, message = "Order was successfully retrieved."),
            @ApiResponse(code = 404, message = "Given order not found.", response = ErrorResponse.class)})
    public OrderDTO getOrderDetails(@ApiParam(value = "Id of a order.", required = true)
                                        @PathVariable("id") Long id) {
        return mapper.map(orderService.findOrderById(id), OrderDTO.class);
    }

}
