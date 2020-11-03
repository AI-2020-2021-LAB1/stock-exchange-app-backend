package com.project.stockexchangeappbackend.repository.specification;

import com.project.stockexchangeappbackend.entity.Order;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.domain.LessThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.domain.LikeIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;

@Join(path = "stock", alias = "s")
@And({
        @Spec(path = "remainingAmount", spec = GreaterThanOrEqual.class, params = {"remainingAmount>"}),
        @Spec(path = "remainingAmount", spec = LessThanOrEqual.class, params = {"remainingAmount<"}),
        @Spec(path = "remainingAmount", spec = Equal.class, params = {"remainingAmount"}),
        @Spec(path = "amount", spec = GreaterThanOrEqual.class, params = {"amount>"}),
        @Spec(path = "amount", spec = LessThanOrEqual.class, params = {"amount<"}),
        @Spec(path = "amount", spec = Equal.class, params = {"amount"}),
        @Spec(path = "price", spec = GreaterThanOrEqual.class, params = {"price>"}),
        @Spec(path = "price", spec = LessThanOrEqual.class, params = {"price<"}),
        @Spec(path = "price", spec = Equal.class, params = {"price"}),
        @Spec(path = "s.name", spec = LikeIgnoreCase.class, params = {"name"}),
        @Spec(path = "s.abbreviation", spec = LikeIgnoreCase.class, params = {"abbreviation"}),
})

public interface OrderSpecification extends Specification<Order> {
}
