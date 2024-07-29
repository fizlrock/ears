package dev.fizlrock.ears;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import dev.fizlrock.ears.domain.entities.AudioRecordInfo;
import dev.fizlrock.ears.domain.entities.User;
import dev.fizlrock.ears.repository.AudioRecordInfoRepository;
import dev.fizlrock.ears.repository.UserRepository;

@DataJpaTest(includeFilters = @ComponentScan.Filter(classes = AudioRecordInfoRepository.class, type = FilterType.ASSIGNABLE_TYPE))
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Disabled("Слишком долго")
class AudioRecordInfoRepositoryTests {

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
			.withInitScript("init_db_script.sql");

	@Autowired
	AudioRecordInfoRepository audioRepo;
	@Autowired
	UserRepository userRepo;

	@Test
	void createEntityWithUUID() {
		var user = User.builder()
				.username("randomusername")
				.passwordHash("randompasswordhash")
				.build();
		assertNull(user.getId());
		userRepo.save(user);
		assertNotNull(user.getId());
	}

	@Test
	void testFindByUUID() {
		var user = User.builder()
				.username("randomusername")
				.passwordHash("randompasswordhash")
				.build();
		userRepo.save(user);

		var audio = AudioRecordInfo.builder()
				.owner(user)
				.build();
		audioRepo.save(audio);

		assertNotNull(audio.getId());

		var finded = audioRepo.findById(UUID.fromString(audio.getId().toString()));

		assertTrue(finded.isPresent());

	}

}
