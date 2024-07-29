package dev.fizlrock.ears;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import dev.fizlrock.ears.domain.entities.AudioRecordInfo;
import dev.fizlrock.ears.domain.entities.User;
import dev.fizlrock.ears.repository.AudioRecordInfoRepository;
import dev.fizlrock.ears.repository.UserRepository;

@SpringBootTest
class EarsApplicationTests {

	@Test
	void contextLoads() {
	}

	@Autowired
	AudioRecordInfoRepository audioRepo;
	@Autowired
	UserRepository userRepo;

	@Test
	void testFindByUUID() {
		var user = User.builder()
				.username("fuck you")
				.passwordHash("fuck it")
				.build();
		userRepo.save(user);
		var audio = AudioRecordInfo.builder()
				.id(UUID.randomUUID())
				.owner(user)
				.build();
		audioRepo.save(audio);

		var finded = audioRepo.findById(UUID.fromString(audio.getId().toString()));

		assertTrue(finded.isPresent());

	}

}
