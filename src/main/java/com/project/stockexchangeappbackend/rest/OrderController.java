package com.project.stockexchangeappbackend.rest;


import com.project.stockexchangeappbackend.dto.OrderDto;
import com.project.stockexchangeappbackend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aoi/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ModelMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderDetails(@PathVariable Long id) {
        OrderDto orderDto = mapper.map(orderService.findOrderById(id), OrderDto.class);
        return new ResponseEntity<>(orderDto, HttpStatus.OK);
    }
}
