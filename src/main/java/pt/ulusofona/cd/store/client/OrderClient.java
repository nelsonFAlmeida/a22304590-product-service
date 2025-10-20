package pt.ulusofona.cd.store.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "order-service", url = "http://order-service:8083")
public interface OrderClient {

    @GetMapping("/product/{productId}/has-pending")
    Boolean productHasPendingOrder(@PathVariable("productId") UUID productId);
}
