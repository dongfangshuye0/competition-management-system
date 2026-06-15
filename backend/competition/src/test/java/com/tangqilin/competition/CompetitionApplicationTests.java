package com.tangqilin.competition;

import com.tangqilin.competition.common.exception.BusinessException;
import com.tangqilin.competition.common.security.AuthContext;
import com.tangqilin.competition.entity.Competition;
import com.tangqilin.competition.entity.Registration;
import com.tangqilin.competition.entity.User;
import com.tangqilin.competition.entity.dto.ProfileUpdateDTO;
import com.tangqilin.competition.entity.dto.WorkSubmitDTO;
import com.tangqilin.competition.service.PlatformService;
import com.tangqilin.competition.service.UserService;
import com.tangqilin.competition.storage.DatabaseStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class CompetitionApplicationTests {

	@Autowired
	private PlatformService platformService;

	@Autowired
	private UserService userService;

	@Autowired
	private DatabaseStore databaseStore;

	@AfterEach
	void tearDown() {
		AuthContext.clear();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void studentCannotSubmitWorkBeforeSubmissionWindow() {
		User student = databaseStore.findUserByUsername("20240001").orElseThrow();
		Competition competition = databaseStore.listCompetitions().stream()
				.filter(item -> "全国大学生软件设计大赛".equals(item.getName()))
				.findFirst()
				.orElseThrow();
		AuthContext.setUser(student);
		Registration registration = databaseStore.listRegistrations().stream()
				.filter(item -> student.getId().equals(item.getUserId())
						&& competition.getId().equals(item.getCompetitionId())
						&& "approved".equals(item.getStatus()))
				.findFirst()
				.orElseThrow();

		WorkSubmitDTO workSubmitDTO = new WorkSubmitDTO();
		workSubmitDTO.setRegistrationId(registration.getId());
		workSubmitDTO.setTitle("新的参赛作品");

		BusinessException exception = assertThrows(BusinessException.class,
				() -> platformService.submitWork(workSubmitDTO));
		assertEquals(400, exception.getCode());
		assertTrue(exception.getMessage().contains("尚未开放"));
	}

	@Test
	void adminCannotDeleteUserWithBusinessRecords() {
		User admin = databaseStore.findUserByUsername("admin").orElseThrow();
		User student = databaseStore.findUserByUsername("20240001").orElseThrow();
		AuthContext.setUser(admin);

		BusinessException exception = assertThrows(BusinessException.class,
				() -> userService.deleteUser(student.getId()));
		assertEquals(400, exception.getCode());
		assertTrue(exception.getMessage().contains("关联"));
	}

	@Test
	void updateProfileRejectsShortPassword() {
		User student = databaseStore.findUserByUsername("20240002").orElseThrow();
		AuthContext.setUser(student);
		ProfileUpdateDTO profileUpdateDTO = new ProfileUpdateDTO();
		profileUpdateDTO.setPassword("123");

		BusinessException exception = assertThrows(BusinessException.class,
				() -> userService.updateProfile(profileUpdateDTO));
		assertEquals(400, exception.getCode());
		assertTrue(exception.getMessage().contains("密码长度"));
	}
}
