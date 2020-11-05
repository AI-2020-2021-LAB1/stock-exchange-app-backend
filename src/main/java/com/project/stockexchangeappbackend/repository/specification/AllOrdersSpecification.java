package com.project.stockexchangeappbackend.repository.specification;

import com.project.stockexchangeappbackend.entity.AllOrders;
import net.kaczmarzyk.spring.data.jpa.domain.*;
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
        @Spec(path = "priceType", spec = EqualIgnoreCase.class, params = {"priceType"}),
        @Spec(path = "orderType", spec = EqualIgnoreCase.class, params = {"orderType"}),
        @Spec(path = "dateCreation", spec = GreaterThanOrEqual.class, params = {"dateCreation>"}, config = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ"),
        @Spec(path = "dateCreation", spec = LessThanOrEqual.class, params = {"dateCreation<"}, config = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ"),
        @Spec(path = "dateCreation", spec = Equal.class, params = {"dateCreation"}, config = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ"),
        @Spec(path = "dateExpiration", spec = GreaterThanOrEqual.class, params = {"dateExpiration>"}, config = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ"),
        @Spec(path = "dateExpiration", spec = LessThanOrEqual.class, params = {"dateExpiration<"}, config = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ"),
        @Spec(path = "dateExpiration", spec = Equal.class, params = {"dateExpiration"}, config = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ"),
        @Spec(path = "dateClosing", spec = GreaterThanOrEqual.class, params = {"dateClosing>"}, config = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ"),
        @Spec(path = "dateClosing", spec = LessThanOrEqual.class, params = {"dateClosing<"}, config = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ"),
        @Spec(path = "dateClosing", spec = Equal.class, params = {"dateClosing"}, config = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ"),
        @Spec(path = "s.name", spec = LikeIgnoreCase.class, params = {"name"}),
        @Spec(path = "s.abbreviation", spec = LikeIgnoreCase.class, params = {"abbreviation"}),
})

public interface AllOrdersSpecification extends Specification<AllOrders> {

}
