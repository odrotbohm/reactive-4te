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

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactory;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;

@SpringBootApplication
public class ReactiveApp {
	
	private static final Logger LOG = LoggerFactory.getLogger(ReactiveApp.class);

	public static void main(String[] args) {
		SpringApplication.run(ReactiveApp.class, args);
	}
	
	@Bean
	CommandLineRunner runner(DatabaseClient client) {
		
		return args -> {
			
			Stream.of("schema.sql", "data.sql")
					.peek(it -> LOG.info(String.format("Executing SQL from %s…", it)))
					.map(ClassPathResource::new)
					.flatMap(ReactiveApp::lines)
					.peek(it -> LOG.info(String.format("Executing %s.", it)))
					.forEach(line -> client.execute().sql(line).fetch().rowsUpdated().block());
		};
	}
	
	private static Stream<String> lines(Resource resource) {
		
		try {
			return Files.lines(resource.getFile().toPath());
		} catch (IOException o_O) {
			throw new RuntimeException(o_O);
		}
	}
	
	@Configuration
	static class R2dbcConfiguration {

		@Bean
		DiscountRepository customerRepository(R2dbcRepositoryFactory factory) {
			return factory.getRepository(DiscountRepository.class);
		}

		@Bean
		R2dbcRepositoryFactory repositoryFactory(DatabaseClient client) {

			RelationalMappingContext context = new RelationalMappingContext();
			context.afterPropertiesSet();

			return new R2dbcRepositoryFactory(client, context);
		}

		@Bean
		DatabaseClient databaseClient(ConnectionFactory factory) {

			return DatabaseClient.builder() //
					.connectionFactory(factory) //
					.build();
		}

		@Bean
		PostgresqlConnectionFactory connectionFactory() {

			PostgresqlConnectionConfiguration config = PostgresqlConnectionConfiguration.builder() //
					.host("localhost") //
					.port(5432) //
					.database("postgres") //
					.username("postgres") //
					.password("") //
					.build();

			return new PostgresqlConnectionFactory(config);
		}
	}
}
