package com.shopjoy.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.jspecify.annotations.NonNull;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

/**
 * GraphQL exception handler for GraphQL error responses.
 */
@Component
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(@NonNull Throwable ex, @NonNull DataFetchingEnvironment env) {
        return switch (ex) {
            case ResourceNotFoundException _ -> GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.NOT_FOUND)
                    .message(ex.getMessage())
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build();
            case InvalidOrderStateException _ -> GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.BAD_REQUEST)
                    .message("This order cannot be updated because it's already in a different state. Please refresh the page to see the current status.")
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build();
            case DuplicateResourceException _, InvalidOperationException _, InsufficientStockException _,
                 IllegalArgumentException _, NullPointerException _ -> GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.BAD_REQUEST)
                    .message(ex.getMessage())
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build();
            case BusinessException businessEx -> GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.BAD_REQUEST)
                    .message("Operation failed: " + businessEx.getMessage())
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build();
            default -> {
                System.err.println("GraphQL Internal Error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
                ex.printStackTrace();
                yield GraphqlErrorBuilder.newError()
                        .errorType(ErrorType.INTERNAL_ERROR)
                        .message("Something went wrong on our end. Please try again in a few moments.")
                        .path(env.getExecutionStepInfo().getPath())
                        .location(env.getField().getSourceLocation())
                        .build();
            }
        };

    }
}
