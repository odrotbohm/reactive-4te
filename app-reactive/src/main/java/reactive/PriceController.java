/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactive;

import reactor.core.publisher.Flux;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
public class PriceController {

	private final WebClient webClient;

	private final SellerRepository discountRepository;


	public PriceController(WebClient.Builder webClientBuilder, SellerRepository discountRepo) {
		this.webClient = webClientBuilder.baseUrl("http://localhost:8082").build();
		this.discountRepository = discountRepo;
	}


	@GetMapping("/product/{productId}/offers")
	public Flux<ProductOffer> getPrice(@PathVariable Long productId) {
		return this.webClient.get().uri("/product/{productId}/sellers", productId)
				.retrieve()
				.bodyToFlux(ProductSellerInfo.class)
				.flatMap(sellerInfo -> {
					String sellerUrl = sellerInfo.getUrl();
					return this.webClient.get().uri(sellerUrl)
							.retrieve()
							.bodyToMono(ProductPriceInfo.class)
							.flatMap(priceInfo ->
									this.discountRepository
											.getDiscount(priceInfo.getSeller(), productId)
											.map(discount -> new ProductOffer(priceInfo, discount)));
				});
	}

}
