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

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.repository.Repository;

interface DiscountRepository extends Repository<DiscountRepository.Discount, Long> {

	@Query("SELECT * FROM discounts WHERE seller = $1 AND product = $2")
	Mono<Discount> getDiscount(String seller, Long productId);

	static class Discount {
		
		@Column("discount")
		public BigDecimal value;
	}
}
