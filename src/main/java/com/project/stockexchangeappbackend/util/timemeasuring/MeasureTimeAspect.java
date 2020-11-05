package com.project.stockexchangeappbackend.util.timemeasuring;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
@AllArgsConstructor
public class MeasureTimeAspect {

    private final ProcessingTime processingTime;

    @Around("@annotation(LogicBusinessMeasureTime)")
    public Object measureLogicBusinessExecutionTime (ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        Object proceed = null;
        try {
            proceed = joinPoint.proceed();
            long executionTime = System.nanoTime() - start;
            processingTime.setBusinessLogicExecutionTime(executionTime);
            return proceed;
        } catch (BeanCreationException exc) {
            return proceed;
        } catch (Throwable t) {
            long executionTime = System.nanoTime() - start;
            processingTime.setBusinessLogicExecutionTime(executionTime);
            throw t;
        }

    }

    @Around("@annotation(DBQueryMeasureTime)")
    public Object measureDBQueryExecutionTime (ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        Object proceed = null;
        try {
            proceed = joinPoint.proceed();
            long executionTime = System.nanoTime() - start;
            processingTime.setDatabaseOperationExecutionTime(
                    processingTime.getDatabaseOperationExecutionTime() + executionTime);
            return proceed;
        } catch (BeanCreationException exc) {
            return proceed;
        } catch (Throwable t) {
            long executionTime = System.nanoTime() - start;
            processingTime.setDatabaseOperationExecutionTime(
                    processingTime.getDatabaseOperationExecutionTime() + executionTime);
            throw t;
        }
    }

}
