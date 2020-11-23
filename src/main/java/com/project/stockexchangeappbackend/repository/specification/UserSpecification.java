package com.project.stockexchangeappbackend.repository.specification;

import com.project.stockexchangeappbackend.entity.User;
import net.kaczmarzyk.spring.data.jpa.domain.*;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;

@Join(path = "tag", alias = "t")
@And({
        @Spec(path = "email", spec = LikeIgnoreCase.class),
        @Spec(path = "firstName", spec = LikeIgnoreCase.class),
        @Spec(path = "lastName", spec = LikeIgnoreCase.class),
        @Spec(path = "role", spec = EqualIgnoreCase.class),
        @Spec(path = "money", spec = GreaterThanOrEqual.class, params={"money>"}),
        @Spec(path = "money", spec = LessThanOrEqual.class, params={"money<"}),
        @Spec(path = "money", spec = Equal.class, params={"money"}),
        @Spec(path = "t.name", spec = EqualIgnoreCase.class, params = {"tag"})
})
public interface UserSpecification extends Specification<User> {
}
