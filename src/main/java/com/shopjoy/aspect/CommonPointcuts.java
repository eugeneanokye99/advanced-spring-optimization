package com.shopjoy.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CommonPointcuts {

    @Pointcut("execution(* com.shopjoy.service..*.*(..))")
    public void serviceMethods() {
    }

    @Pointcut("execution(* com.shopjoy.repository..*.*(..))")
    public void repositoryMethods() {
    }

    @Pointcut("execution(* com.shopjoy.controller..*.*(..))")
    public void controllerMethods() {
    }

    @Pointcut("execution(* com.shopjoy.graphql.resolver..*.*(..))")
    public void graphqlResolverMethods() {
    }

    @Pointcut("execution(* com.shopjoy.service.*.create*(..))")
    public void createMethods() {
    }

    @Pointcut("execution(* com.shopjoy.service.*.update*(..))")
    public void updateMethods() {
    }

    @Pointcut("execution(* com.shopjoy.service.*.delete*(..))")
    public void deleteMethods() {
    }

    @Pointcut("serviceMethods() || repositoryMethods() || controllerMethods()")
    public void allApplicationMethods() {
    }

    @Pointcut("createMethods() || updateMethods() || deleteMethods()")
    public void dataModificationMethods() {
    }

    @Pointcut("execution(* com.shopjoy.controller.ProductController.getProductsWithFilters(..)) || " +
              "execution(* com.shopjoy.controller.ProductController.getProductById(..)) || " +
              "execution(* com.shopjoy.controller.ProductController.getAllProducts(..)) || " +
              "execution(* com.shopjoy.controller.ProductController.getActiveProducts(..)) || " +
              "execution(* com.shopjoy.controller.ProductController.getProductsByCategory(..)) || " +
              "execution(* com.shopjoy.controller.ProductController.searchProductsByName(..)) || " +
              "execution(* com.shopjoy.controller.ProductController.getProductsByPriceRange(..)) || " +
              "execution(* com.shopjoy.controller.ProductController.getTotalProductCount(..)) || " +
              "execution(* com.shopjoy.controller.ProductController.getProductCountByCategory(..)) || " +
              "execution(* com.shopjoy.controller.ProductController.getProductsPaginated(..)) || " +
              "execution(* com.shopjoy.controller.ProductController.searchProductsPaginated(..)) || " +
              "execution(* com.shopjoy.controller.ProductController.getNewArrivals(..))")
    public void productControllerReadMethods() {
    }

    @Pointcut("execution(* com.shopjoy.service.ProductService.getProductsWithFilters(..)) || " +
              "execution(* com.shopjoy.service.ProductService.getProductById(..)) || " +
              "execution(* com.shopjoy.service.ProductService.getProductsByIds(..)) || " +
              "execution(* com.shopjoy.service.ProductService.getAllProducts(..)) || " +
              "execution(* com.shopjoy.service.ProductService.getActiveProducts(..)) || " +
              "execution(* com.shopjoy.service.ProductService.getProductsByCategory(..)) || " +
              "execution(* com.shopjoy.service.ProductService.getProductsByCategories(..)) || " +
              "execution(* com.shopjoy.service.ProductService.searchProductsByName(..)) || " +
              "execution(* com.shopjoy.service.ProductService.getProductsByPriceRange(..)) || " +
              "execution(* com.shopjoy.service.ProductService.getTotalProductCount(..)) || " +
              "execution(* com.shopjoy.service.ProductService.getProductCountByCategory(..)) || " +
              "execution(* com.shopjoy.service.ProductService.getProductsPaginated(..)) || " +
              "execution(* com.shopjoy.service.ProductService.searchProductsPaginated(..)) || " +
              "execution(* com.shopjoy.service.ProductService.getRecentlyAddedProducts(..))")
    public void productServiceReadMethods() {
    }

    @Pointcut("serviceMethods() && !productServiceReadMethods()")
    public void nonProductReadServiceMethods() {
    }

    @Pointcut("controllerMethods() && !productControllerReadMethods()")
    public void nonProductReadControllerMethods() {
    }

    @Pointcut("execution(@(@org.springframework.web.bind.annotation.RequestMapping *) * *(..))")
    public void publicEndpoints() {
    }
}
