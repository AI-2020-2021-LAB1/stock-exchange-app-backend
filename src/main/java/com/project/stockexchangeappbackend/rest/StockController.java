package com.project.stockexchangeappbackend.rest;

import com.project.stockexchangeappbackend.dto.CreateStockDTO;
import com.project.stockexchangeappbackend.dto.ErrorResponse;
import com.project.stockexchangeappbackend.dto.StockDTO;
import com.project.stockexchangeappbackend.dto.StockIndexValueDTO;
import com.project.stockexchangeappbackend.repository.specification.StockIndexValueSpecification;
import com.project.stockexchangeappbackend.repository.specification.StockSpecification;
import com.project.stockexchangeappbackend.service.StockIndexValueService;
import com.project.stockexchangeappbackend.service.StockService;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/stock")
@CrossOrigin("*")
@AllArgsConstructor
@Api(value = "Stocks", description = "REST API for stocks' management", tags = "Stocks")
@ApiResponses({
        @ApiResponse(code = 400, message = "The request could not be understood or was missing required parameters.",
                response = ErrorResponse.class),
        @ApiResponse(code = 401, message = "Unauthorized."),
        @ApiResponse(code = 403, message = "Access Denied.")
})
public class StockController {

    private final StockService stockService;
    private final StockIndexValueService stockIndexValueService;
    private final ModelMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @ApiOperation(value = "Page and filter stocks.", response = StockDTO.class,
            notes = "Required one role of: ADMIN, USER")
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully paged and filtered stocks."))
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
            @ApiImplicitParam(name = "priceChangeRatio>", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `priceChangeRatio`. (omitted if null)"),
            @ApiImplicitParam(name = "priceChangeRatio<", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `priceChangeRatio`. (omitted if null)")
    })
    public Page<StockDTO> getStocks(@ApiIgnore Pageable pageable, StockSpecification stockSpecification) {
        return stockService.getStocks(pageable, stockSpecification)
                .map(stock -> mapper.map(stock, StockDTO.class));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @ApiOperation(value = "Retrieve stock by id", response = StockDTO.class, notes = "Required one role of: ADMIN, USER")
    @ApiResponses({@ApiResponse(code = 200, message = "Stock was successfully retrieved."),
            @ApiResponse(code = 404, message = "Given stock not found.", response = ErrorResponse.class)})
    public StockDTO getStock(@ApiParam(value = "Abbreviation or id of desired stock", required = true)
                                           @PathVariable String id) {
        return mapper.map(stockService.getStockByIdOrAbbreviation(id), StockDTO.class);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation(value = "Update stock")
    @ApiResponses(@ApiResponse(code = 200, message = "Stock was successfully updated."))
    public void updateStock(
            @ApiParam(value = "Stock object to update.", required = true) @Valid @RequestBody StockDTO stockDTO,
            @ApiParam(value = "Abbreviation or id of desired stock", required = true)
            @PathVariable String id) {
        stockService.updateStock(stockDTO, id);
    }

    @GetMapping("/{id}/index")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @ApiOperation(value = "Retrieve stock's indexes history by stock id", response = StockIndexValueDTO.class,
            notes = "Required one role of: ADMIN, USER")
    @ApiResponses({@ApiResponse(code = 200, message = "Stock's indexes was successfully retrieved."),
            @ApiResponse(code = 404, message = "Given stock not found.", response = ErrorResponse.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "datetime>", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `timestamp` (omitted if null)."),
            @ApiImplicitParam(name = "dateime<", dataType = "integer", paramType = "query",
                    value = "Filtering criteria for field `timestamp` (omitted if null).")
    })
    public List<StockIndexValueDTO> getIndexes(StockIndexValueSpecification specification,
                               @ApiParam(value = "The stock's id.", required = true) @PathVariable("id") Long stockId,
                               @ApiParam(value = "Interval", defaultValue = "1")
                               @RequestParam(value = "interval", defaultValue = "1") Integer interval) {
        return stockIndexValueService.getStockIndexValues(stockId, specification, interval);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation(value = "Create new stock", notes = "Required role ADMIN")
    @ApiResponses({@ApiResponse(code = 200, message = "Stock's indexes was successfully created."),
            @ApiResponse(code = 409, message = "Given stock already exist.", response = ErrorResponse.class)})
    public void create(@RequestBody @Valid CreateStockDTO stockDTO) {
        stockService.createStock(stockDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation(value = "Delete existing stock", notes = "Required role ADMIN")
    @ApiResponses({@ApiResponse(code = 200, message = "Stock's indexes was successfully deleted."),
            @ApiResponse(code = 404, message = "Given stock not found.", response = ErrorResponse.class)})
    public void delete(@ApiParam("The id of stock to delete.") @PathVariable Long id) {
        stockService.deleteStock(id);
    }

}
