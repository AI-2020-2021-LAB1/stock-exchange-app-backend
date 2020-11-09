package com.project.stockexchangeappbackend.util.timemeasuring;

import lombok.AllArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
@AllArgsConstructor
public class ExecutionTimeResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final ProcessingTime processingTime;
    private final static String DB_QUERY_EXECUTION_TIME_HEADER = "Execution-Time-DB-Query";
    private final static String BUSINESS_LOGIN_EXECUTION_TIME_HEADER = "Execution-Time-Business-Logic";

    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object obj, MethodParameter methodParameter, MediaType mediaType, Class aClass,
                                  ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        serverHttpResponse.getHeaders().add(DB_QUERY_EXECUTION_TIME_HEADER, processingTime.getDatabaseOperationExecutionTime().toString());
        serverHttpResponse.getHeaders().add(BUSINESS_LOGIN_EXECUTION_TIME_HEADER, processingTime.getBusinessLogicExecutionTime().toString());
        return obj;
    }

}
