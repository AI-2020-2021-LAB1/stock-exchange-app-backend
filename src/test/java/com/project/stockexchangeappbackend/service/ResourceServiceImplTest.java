package com.project.stockexchangeappbackend.service;

import com.project.stockexchangeappbackend.entity.Resource;
import com.project.stockexchangeappbackend.entity.Stock;
import com.project.stockexchangeappbackend.entity.User;
import com.project.stockexchangeappbackend.repository.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.project.stockexchangeappbackend.service.StockServiceImplTest.assertStock;
import static com.project.stockexchangeappbackend.service.StockServiceImplTest.createCustomStock;
import static com.project.stockexchangeappbackend.service.UserServiceImplTest.assertUser;
import static com.project.stockexchangeappbackend.service.UserServiceImplTest.createCustomUser;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceServiceImplTest {

    @InjectMocks
    ResourceServiceImpl resourceService;

    @Mock
    ResourceRepository resourceRepository;

    @Test
    void shouldPageAndFilterResources(@Mock SecurityContext securityContext, @Mock Authentication authentication) {
        User user = createCustomUser(1L, "test@test.pl", "John", "Kowal", BigDecimal.ONE);
        List<Resource> resources = Arrays.asList(
                createCustomResource(1L, createCustomStock(1L, "WIG30", "W30", 100, BigDecimal.TEN),
                user, 100));
        Pageable pageable = PageRequest.of(0,20);
        Specification<Resource> resourceSpecification =
                (Specification<Resource>) (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("name"), "WIG");
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user.getEmail());
        when(resourceRepository.findAll(Mockito.any(Specification.class), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(resources, pageable, resources.size()));
        Page<Resource> output = resourceService.getOwnedResources(pageable, resourceSpecification);
        assertEquals(resources.size(), output.getNumberOfElements());
        for (int i=0; i<resources.size(); i++) {
            assertResource(output.getContent().get(i), resources.get(i));
        }

    }

    public static void assertResource(Resource output, Resource expected) {
        assertAll(() -> assertEquals(expected.getId(), output.getId()),
                () -> assertEquals(expected.getAmount(), output.getAmount()),
                () -> assertUser(expected.getUser(),output.getUser()),
                () -> assertStock(expected.getStock(), output.getStock()));
    }

    public static Resource createCustomResource (Long id, Stock stock, User user, Integer amount) {
        return Resource.builder()
                .id(id).stock(stock).user(user).amount(amount).build();
    }

}