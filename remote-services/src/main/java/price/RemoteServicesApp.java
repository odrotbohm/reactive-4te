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
package price;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import reactor.core.publisher.Mono;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;

@SpringBootApplication
public class RemoteServicesApp implements WebFluxConfigurer {

	private static final UriBuilderFactory priceUrlBuilder =
			new DefaultUriBuilderFactory("/{seller}/product/{productId}/price");

	private static final Random priceSeed = new Random();


	public static void main(String[] args) {
		SpringApplication.run(RemoteServicesApp.class, args);
	}


	@Bean
	public RouterFunction<?> routes() {
		return RouterFunctions.route()
				.GET("/product/{productId}/sellers", request -> {
					Long productId = Long.parseLong(request.pathVariable("productId"));
					return ServerResponse.ok().syncBody(getSellerPriceData(productId));
				})
				.GET("/{seller}/product/{productId}/price", request -> {
					String seller = request.pathVariable("seller");
					Long productId = Long.parseLong(request.pathVariable("productId"));
					return ServerResponse.ok().syncBody(getPriceData(seller, productId));
				})
				.filter((request, next) -> {
					Duration seconds = Duration.ofSeconds(2);
					return Mono.delay(seconds).flatMap(aLong -> next.handle(request));
				})
				.build();
	}


	/**
	 * Return 5  seller URLs for the given product.
	 */
	private List<Map<String, String>> getSellerPriceData(Long productId) {
		return Stream.of("seller-A", "seller-B", "seller-C", "seller-E", "seller-D")
				.map(seller -> {
					URI uri = priceUrlBuilder.builder().build(seller, productId);
					Map<String, String> data = new LinkedHashMap<>(3);
					data.put("seller", seller);
					data.put("productId", productId.toString());
					data.put("priceUrl", uri.toString());
					return data;
				})
				.collect(Collectors.toList());
	}

	/**
	 * Generator for a random price between 5.00 and 100.00.
	 */
	private static Map<String, String> getPriceData(String seller, Long productId) {
		int units = priceSeed.nextInt(100) + 5;
		double change = priceSeed.nextDouble();
		BigDecimal price = new BigDecimal(units + change).setScale(2, RoundingMode.HALF_EVEN);
		Map<String, String> data = new LinkedHashMap<>(3);
		data.put("seller", seller);
		data.put("productId", productId.toString());
		data.put("price", price.toString());
		return data;
	}

}