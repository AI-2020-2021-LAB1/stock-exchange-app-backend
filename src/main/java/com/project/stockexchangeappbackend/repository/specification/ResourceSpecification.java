package com.project.stockexchangeappbackend.repository.specification;

import com.project.stockexchangeappbackend.entity.Resource;
import net.kaczmarzyk.spring.data.jpa.domain.*;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;

@Join(path = "stock", alias = "s")
@And({
        @Spec(path = "s.name", spec = LikeIgnoreCase.class, params = {"name"}),
        @Spec(path = "s.abbreviation", spec = LikeIgnoreCase.class, params = {"abbreviation"}),
        @Spec(path = "s.currentPrice", spec = GreaterThan.class, params={"currentPriceGreaterThan"}),
        @Spec(path = "s.currentPrice", spec = GreaterThanOrEqual.class, params={"currentPriceGreaterThanOrEqual"}),
        @Spec(path = "s.currentPrice", spec = LessThan.class, params={"currentPriceLessThan"}),
        @Spec(path = "s.currentPrice", spec = LessThanOrEqual.class, params={"currentPriceLessThanOrEqual"}),
        @Spec(path = "s.currentPrice", spec = Equal.class, params={"currentPrice"}),
        @Spec(path = "amount", spec = GreaterThan.class, params={"amountGreaterThan"}),
        @Spec(path = "amount", spec = GreaterThanOrEqual.class, params={"amountGreaterThanOrEqual"}),
        @Spec(path = "amount", spec = LessThan.class, params={"amountLessThan"}),
        @Spec(path = "amount", spec = LessThanOrEqual.class, params={"amountLessThanOrEqual"}),
        @Spec(path = "amount", spec = Equal.class, params={"amount"})
})
public interface ResourceSpecification extends Specification<Resource> {
}
