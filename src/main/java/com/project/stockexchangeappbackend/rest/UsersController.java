package com.project.stockexchangeappbackend.rest;

import com.project.stockexchangeappbackend.dto.*;
import com.project.stockexchangeappbackend.repository.specification.AllOrdersSpecification;
import com.project.stockexchangeappbackend.repository.specification.ResourceSpecification;
import com.project.stockexchangeappbackend.repository.specification.TransactionSpecification;
import com.project.stockexchangeappbackend.service.OrderService;
import com.project.stockexchangeappbackend.service.ResourceService;
import com.project.stockexchangeappbackend.service.TransactionService;
import com.project.stockexchangeappbackend.service.UserService;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/api/user")
@CrossOrigin("*")
@AllArgsConstructor
@Api(value = "Users", description = "REST API for users' management", tags = "Users")
@ApiResponses({
        @ApiResponse(code = 400, message = "The request could not be understood or was missing required parameters.",
                response = ErrorResponse.class),
        @ApiResponse(code = 401, message = "Unauthorized."),
        @ApiResponse(code = 403, message = "Access Denied.")
})
public class UsersController {

    private final UserService userService;
    private final ResourceService resourceService;
    private final OrderService orderService;
    private final TransactionService transactionService;
    private final ModelMapper mapper;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation(value = "Retrieve user by id", response = StockDTO.class, notes = "Required role: ADMIN")
    @ApiResponses({@ApiResponse(code = 200, message = "User was successfully retrieved."),
            @ApiResponse(code = 404, message = "Given user not found.", response = ErrorResponse.class)})
    public UserDto getDetails(@ApiParam(value = "Id of desired user.", required = true) @PathVariable Long id) {
        return mapper.map(userService.findUserById(id), UserDto.class);
    }

    @PutMapping
    @ApiIgnore
    public String changeDetails() {
        return "change_details";
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ApiIgnore
    public String getUsers() {
        return "users_list";
    }

    @GetMapping("/stock/owned")
    @PreAuthorize("hasRole('USER')")
    @ApiOperation(value = "Page and filter logged user's stocks.", response = StockDTO.class,
            notes = "Required role: USER")
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully paged and filtered logged user's stocks."))
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
            @ApiImplicitParam(name = "amount>", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `amount`. (omitted if null)"),
            @ApiImplicitParam(name = "amount<", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `amount`. (omitted if null)"),
            @ApiImplicitParam(name = "amount", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `amount`. Param is exact value. (omitted if null)"),
            @ApiImplicitParam(name = "currentPrice>", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `currentPrice`. (omitted if null)"),
            @ApiImplicitParam(name = "currentPrice<", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `currentPrice`. (omitted if null)"),
            @ApiImplicitParam(name = "currentPrice", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `currentPrice`. Param is exact value. (omitted if null)")
    })
    public Page<ResourceDTO> getOwnedStocks(@ApiIgnore Pageable pageable, ResourceSpecification specification) {
        return resourceService.getOwnedResources(pageable, specification);
    }

    @GetMapping("/order/owned")
    @PreAuthorize("hasRole('USER')")
    @ApiOperation(value = "Page and filter logged user's orders.", response = OrderDTO.class,
            notes = "Required role of: USER \n" +
            "Given date must be in one format of: \n - yyyy-MM-ddThh:mm:ss.SSSZ (Z means Greenwich zone), " +
            "\n - yyyy-MM-ddThh:mm:ss.SSS-hh:mm \n - yyyy-MM-ddThh:mm:ss.SSS%2Bhh:mm (%2B means +)")
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully paged and filtered logged user's orders."))
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
            @ApiImplicitParam(name = "price>", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `price`. (omitted if null)"),
            @ApiImplicitParam(name = "price<", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `price`. (omitted if null)"),
            @ApiImplicitParam(name = "price", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `price`. Param is exact value. (omitted if null)"),
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
            @ApiImplicitParam(name = "creationDate>", dataType = "date", paramType = "query",
                    value = "Filtering criteria for field `creationDate`. (omitted if null)"),
            @ApiImplicitParam(name = "creationDate<", dataType = "date", paramType = "query",
                    value = "Filtering criteria for field `creationDate`. (omitted if null)"),
            @ApiImplicitParam(name = "dateExpiration>", dataType = "date", paramType = "query",
                    value = "Filtering criteria for field `dateExpiration`. (omitted if null)"),
            @ApiImplicitParam(name = "dateExpiration<", dataType = "date", paramType = "query",
                    value = "Filtering criteria for field `dateExpiration`. (omitted if null)"),
            @ApiImplicitParam(name = "dateClosing>", dataType = "date", paramType = "query",
                    value = "Filtering criteria for field `creationClosing`. (omitted if null)"),
            @ApiImplicitParam(name = "dateClosing<", dataType = "date", paramType = "query",
                    value = "Filtering criteria for field `creationClosing`. (omitted if null)"),
            @ApiImplicitParam(name = "active", dataType = "boolean", paramType = "query",
                    value = "Filtering criteria for state of order. Param is exact value. (omitted if null)")
    })
    public Page<OrderDTO> getOwnedOrders(@ApiIgnore Pageable pageable, AllOrdersSpecification specification) {
        return orderService.getOwnedOrders(pageable, specification)
                .map(order -> mapper.map(order, OrderDTO.class));
    }

    @GetMapping("/transaction/owned")
    @PreAuthorize("hasRole('USER')")
    @ApiOperation(value = "Page and filter user's owned transactions.", response = TransactionDTO.class,
            notes = "Required role of: USER \n" +
                    "Given date must be in one format of: \n - yyyy-MM-ddThh:mm:ss.SSSZ (Z means Greenwich zone), " +
                    "\n - yyyy-MM-ddThh:mm:ss.SSS-hh:mm \n - yyyy-MM-ddThh:mm:ss.SSS%2Bhh:mm (%2B means +)")
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully paged and filtered user's owned transactions."))
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "Results page you want to retrieve (0..N).", defaultValue = "0"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "Number of records per page.", defaultValue = "20"),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "Sorting criteria in the format: property(,asc|desc). " +
                            "Default sort order is ascending. Multiple sort criteria are supported."),
            @ApiImplicitParam(name = "date>", dataType = "date", paramType = "query",
                    value = "Filtering criteria for field `date`. (omitted if null)"),
            @ApiImplicitParam(name = "date<", dataType = "date", paramType = "query",
                    value = "Filtering criteria for field `date`. (omitted if null)"),
            @ApiImplicitParam(name = "amount>", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `amount`. (omitted if null)"),
            @ApiImplicitParam(name = "amount<", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `amount`. (omitted if null)"),
            @ApiImplicitParam(name = "amount", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `amount`. Param is exact value. (omitted if null)"),
            @ApiImplicitParam(name = "unitPrice>", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `unitPrice`. (omitted if null)"),
            @ApiImplicitParam(name = "unitPrice<", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `unitPrice`. (omitted if null)"),
            @ApiImplicitParam(name = "unitPrice", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `unitPrice`. Param is exact value. (omitted if null)"),
            @ApiImplicitParam(name = "name", dataType = "string", paramType = "query",
                    value = "Filtering criteria for field `name` (omitted if null)"),
            @ApiImplicitParam(name = "abbreviation", dataType = "string", paramType = "query",
                    value = "Filtering criteria for field `abbreviation`. (omitted if null)"),
            @ApiImplicitParam(name = "isSeller", dataType = "boolean", paramType = "query",
                    value = "Filtering criteria for field sellingOrder. (true if null)"),
            @ApiImplicitParam(name = "isBuyer", dataType = "boolean", paramType = "query",
                    value = "Filtering criteria for field buyingOrder. (true if null)"),
    })
    public Page<TransactionDTO> getOwnedTransactions(@ApiIgnore Pageable pageable, TransactionSpecification specification,
                                                     @RequestParam(required = false, defaultValue = "true") boolean isSeller,
                                                     @RequestParam(required = false, defaultValue = "true") boolean isBuyer) {
        return transactionService.getOwnedTransactions(pageable, specification, isSeller, isBuyer)
                .map(transaction -> mapper.map(transaction, TransactionDTO.class));
    }
}
