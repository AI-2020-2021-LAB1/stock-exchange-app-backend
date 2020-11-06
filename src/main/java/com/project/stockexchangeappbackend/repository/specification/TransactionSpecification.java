package com.project.stockexchangeappbackend.repository.specification;

import com.project.stockexchangeappbackend.entity.Transaction;
import net.kaczmarzyk.spring.data.jpa.domain.*;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;


@Join(path = "buyingOrder", alias = "o")
@Join(path = "o.stock", alias = "s")
@And({
        @Spec(path = "date", spec = GreaterThanOrEqual.class, params = {"dateCreation>"},
                config = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ"),
        @Spec(path = "date", spec = LessThanOrEqual.class, params = {"dateCreation<"},
                config = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ"),
        @Spec(path = "date", spec = Equal.class, params = {"dateCreation"},
                config = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ"),
        @Spec(path = "amount", spec = GreaterThanOrEqual.class, params = {"amount>"}),
        @Spec(path = "amount", spec = LessThanOrEqual.class, params = {"amount<"}),
        @Spec(path = "amount", spec = Equal.class, params = {"amount"}),
        @Spec(path = "unitPrice", spec = GreaterThanOrEqual.class, params = {"unitPrice>"}),
        @Spec(path = "unitPrice", spec = LessThanOrEqual.class, params = {"unitPrice<"}),
        @Spec(path = "unitPrice", spec = Equal.class, params = {"unitPrice"}),
        @Spec(path = "s.name", spec = LikeIgnoreCase.class, params = {"name"}),
        @Spec(path = "s.abbreviation", spec = LikeIgnoreCase.class, params = {"abbreviation"}),
})
public interface TransactionSpecification extends Specification<Transaction> {
}
