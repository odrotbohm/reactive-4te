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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class ProductSellerInfo {

	private final String seller;

	private final Long productId;

	private final String priceUrl;


	@JsonCreator
	public ProductSellerInfo(@JsonProperty("seller") String seller,
			@JsonProperty("productId") Long productId, @JsonProperty("priceUrl") String priceUrl) {

		this.productId = productId;
		this.seller = seller;
		this.priceUrl = priceUrl;
	}

	public String getSeller() {
		return this.seller;
	}

	public Long getProductId() {
		return this.productId;
	}

	public String getUrl() {
		return this.priceUrl;
	}


	@Override
	public String toString() {
		return "Seller: '" + this.seller + "', priceURL: " + this.priceUrl;
	}
}
