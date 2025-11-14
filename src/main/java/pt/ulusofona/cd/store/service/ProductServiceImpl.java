package pt.ulusofona.cd.store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;
import pt.ulusofona.cd.store.ProductServiceGrpc;
import pt.ulusofona.cd.store.CountDiscontinuedProductsRequest;
import pt.ulusofona.cd.store.ProductCountResponse;
import io.grpc.stub.StreamObserver;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ProductServiceGrpc.ProductServiceImplBase {

    private final ProductService productService;

    @Override
    public void countDiscontinuedProductsBySupplier(
            CountDiscontinuedProductsRequest request,
            StreamObserver<ProductCountResponse> responseObserver) {

        long count;

        try {
            System.out.println("countDiscontinuedProductsBySupplier requested");
            String supplierIdString = request.getSupplierId();
            UUID supplierId = UUID.fromString(supplierIdString);

            boolean isDiscontinued = request.getIsDiscontinued();

            count = productService.countProductsBySupplierAndDiscontinued(supplierId, isDiscontinued);

            ProductCountResponse response = ProductCountResponse.newBuilder()
                    .setCount((int) count)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Invalid supplier_id format. Must be a valid UUID.")
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("An unexpected error occurred.")
                    .asRuntimeException());
        }
    }
}