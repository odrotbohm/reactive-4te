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
package classic;

import classic.DiscountRepository.Discount;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@RestController
public class PriceController {

	private static final String BASE_URL = "http://localhost:8082";
	private static final ParameterizedTypeReference<List<ProductSellerInfo>> SELLER_LIST_TYPE =
			new ParameterizedTypeReference<List<ProductSellerInfo>>() {};

	private final RestTemplate template;
	private final DiscountRepository repository;

	public PriceController(RestTemplateBuilder builder, DiscountRepository repository) {
		
		this.template = builder.uriTemplateHandler(new DefaultUriBuilderFactory(BASE_URL)).build();
		this.repository = repository;
	}


	@GetMapping("/product/{productId}/offers")
	public List<ProductOffer> getPrice(@PathVariable Long productId) {

		String sellersUrl = "/product/{productId}/sellers";

		return template
				.exchange(sellersUrl, HttpMethod.GET, null, SELLER_LIST_TYPE, productId)
				.getBody()
				.stream()
				.map(sellerInfo -> 
					template.getForObject(sellerInfo.getUrl(), ProductPriceInfo.class))
				.map(priceInfo -> {
					String seller = priceInfo.getSeller();
					Discount discount = repository.getDiscount(seller, productId);
					return new ProductOffer(discount.value, priceInfo);
				})
				.collect(Collectors.toList());
	}

}