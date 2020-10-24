package com.project.stockexchangeappbackend.rest;


import com.project.stockexchangeappbackend.dto.TransactionDto;
import com.project.stockexchangeappbackend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final ModelMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransactionDetails(@PathVariable Long id) {
        TransactionDto transactionDto = mapper.map(transactionService.findTransactionById(id), TransactionDto.class);
        return new ResponseEntity<>(transactionDto, HttpStatus.OK);
    }
}
