package com.project.stockexchangeappbackend.rest;

import com.project.stockexchangeappbackend.dto.ErrorResponse;
import com.project.stockexchangeappbackend.dto.OrderDTO;
import com.project.stockexchangeappbackend.repository.specification.AllOrdersSpecification;
import com.project.stockexchangeappbackend.service.OrderService;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

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

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @ApiOperation(value = "Create new order")
    @ApiResponses(@ApiResponse(code = 200, message = "Order was successfully created."))
    public void createOrder(@ApiParam(value = "Order object to create.", required = true)
                            @RequestBody @Valid OrderDTO orderDTO) {
        orderService.createOrder(orderDTO);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @ApiOperation(value = "Page and filter orders.", response = OrderDTO.class,
            notes = "Required one role of: ADMIN, USER")
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully paged and filtered orders."))
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "Results page you want to retrieve (0..N).", defaultValue = "0"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "Number of records per page.", defaultValue = "20"),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "Sorting criteria in the format: property(,asc|desc). " +
                            "Default sort order is ascending. Multiple sort criteria are supported."),
            @ApiImplicitParam(name = "name", dataType = "string", paramType = "query",
                    value = "Filtering criteria for field `name` (omitted if null)"),
            @ApiImplicitParam(name = "abbreviation", dataType = "string", paramType = "query",
                    value = "Filtering criteria for field `abbreviation`. (omitted if null)"),
            @ApiImplicitParam(name = "currentPrice>", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `currentPrice`. (omitted if null)"),
            @ApiImplicitParam(name = "currentPrice<", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `currentPrice`. (omitted if null)"),
            @ApiImplicitParam(name = "currentPrice", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `currentPrice`. Param is exact value. (omitted if null)"),
            @ApiImplicitParam(name = "amount>", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `amount`. (omitted if null)"),
            @ApiImplicitParam(name = "amount<", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `amount`. (omitted if null)"),
            @ApiImplicitParam(name = "amount", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `amount`. Param is exact value. (omitted if null)"),
            @ApiImplicitParam(name = "priceType", dataType = "string", paramType = "query",
                    value = "Filtering criteria for field `priceType`. Param is exact value. (omitted if null)"),
            @ApiImplicitParam(name = "orderType", dataType = "string", paramType = "query",
                    value = "Filtering criteria for field `orderType`. Param is exact value. (omitted if null)"),
            @ApiImplicitParam(name = "creationDate", dataType = "date", paramType = "query",
                    value = "Filtering criteria for field `creationDate`. Param is exact value. (omitted if null)"),
            @ApiImplicitParam(name = "creationDate>", dataType = "date", paramType = "query",
                    value = "Filtering criteria for field `creationDate`. (omitted if null)"),
            @ApiImplicitParam(name = "creationDate<", dataType = "date", paramType = "query",
                    value = "Filtering criteria for field `creationDate`. (omitted if null)"),
            @ApiImplicitParam(name = "dateExpiration", dataType = "date", paramType = "query",
                    value = "Filtering criteria for field `dateExpiration`. Param is exact value. (omitted if null)"),
            @ApiImplicitParam(name = "dateExpiration>", dataType = "date", paramType = "query",
                    value = "Filtering criteria for field `dateExpiration`. (omitted if null)"),
            @ApiImplicitParam(name = "dateExpiration<", dataType = "date", paramType = "query",
                    value = "Filtering criteria for field `dateExpiration`. (omitted if null)"),
            @ApiImplicitParam(name = "dateClosing", dataType = "date", paramType = "query",
                    value = "Filtering criteria for field `creationClosing`. Param is exact value. (omitted if null)"),
            @ApiImplicitParam(name = "dateClosing>", dataType = "date", paramType = "query",
                    value = "Filtering criteria for field `creationClosing`. (omitted if null)"),
            @ApiImplicitParam(name = "dateClosing<", dataType = "date", paramType = "query",
                    value = "Filtering criteria for field `creationClosing`. (omitted if null)"),
    })
    public Page<OrderDTO> getOrders(@ApiIgnore Pageable pageable, AllOrdersSpecification allOrdersSpecification) {
        return orderService.findAllOrders(pageable, allOrdersSpecification)
                .map(order -> mapper.map(order, OrderDTO.class));
    }

}
