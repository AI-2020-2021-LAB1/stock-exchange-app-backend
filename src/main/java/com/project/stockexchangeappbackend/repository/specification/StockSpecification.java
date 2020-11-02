package com.project.stockexchangeappbackend.repository.specification;

import com.project.stockexchangeappbackend.entity.Stock;
import net.kaczmarzyk.spring.data.jpa.domain.*;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;

@And({
        @Spec(path = "name", spec = LikeIgnoreCase.class),
        @Spec(path = "abbreviation", spec = LikeIgnoreCase.class),
        @Spec(path = "currentPrice", spec = GreaterThan.class, params={"currentPriceGreaterThan"}),
        @Spec(path = "currentPrice", spec = GreaterThanOrEqual.class, params={"currentPriceGreaterThanOrEqual"}),
        @Spec(path = "currentPrice", spec = LessThan.class, params={"currentPriceLessThan"}),
        @Spec(path = "currentPrice", spec = LessThanOrEqual.class, params={"currentPriceLessThanOrEqual"}),
        @Spec(path = "currentPrice", spec = Equal.class, params={"currentPrice"}),
        @Spec(path = "amount", spec = GreaterThan.class, params={"amountGreaterThan"}),
        @Spec(path = "amount", spec = GreaterThanOrEqual.class, params={"amountGreaterThanOrEqual"}),
        @Spec(path = "amount", spec = LessThan.class, params={"amountLessThan"}),
        @Spec(path = "amount", spec = LessThanOrEqual.class, params={"amountLessThanOrEqual"}),
        @Spec(path = "amount", spec = Equal.class, params={"amount"})
})
public interface StockSpecification extends Specification<Stock> {
}
