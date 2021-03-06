package com.shopme.admin.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;

import com.shopme.common.entity.Role;
import com.shopme.common.entity.User;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class UserRepositoryTests {
	@Autowired
	private UserRepository repo;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	public void testCreateNewUserWithOneRole() {
		Role roleAdmin = entityManager.find(Role.class, 1);
		User userNH = new User("giacbavanh@gmail.com", "2110", "Ngo", "Hoang");
		userNH.addRole(roleAdmin);
		User savedUser = repo.save(userNH);
		assertThat(savedUser.getId()).isGreaterThan(0);

	}

	@Test
	public void testCreateNewUserWithTwoRoles() {
		User userNH = new User("ravi@gmail.com", "ravi2020", "Ravi", "Kumar");
		Role roleEditor = new Role(3);
		Role roleAssistant = new Role(5);
		userNH.addRole(roleEditor);
		userNH.addRole(roleAssistant);
		User savedUser = repo.save(userNH);
		assertThat(savedUser.getId()).isGreaterThan(0);
	}

	@Test
	public void testListAllUsers() {
		Iterable<User> listUsers = repo.findAll();
		listUsers.forEach(user -> System.out.println(user));
	}

	@Test
	public void testGetUserById() {
		User userNh = repo.findById(1).get();
		System.out.println(userNh);
		assertThat(userNh).isNotNull();
	}

	@Test
	public void testUpdateUserDetails() {
		User userHoang = repo.findById(1).get();
		userHoang.setEnabled(true);
		userHoang.setEmail("hoanggg2110@gmail.com");
		repo.save(userHoang);
	}

	@Test
	public void testUpdateUserRoles() {
		User userHoang = repo.findById(2).get();
		Role roleSalesPerson = new Role(2);
		Role roleEditor = new Role(3);
		userHoang.getRoles().remove(roleEditor);
		userHoang.addRole(roleSalesPerson);
		repo.save(userHoang);
		System.out.print(userHoang);
	}

	@Test
	public void testDeleteUser() {
		Integer userId = 2;
		repo.deleteById(userId);
	}

	@Test
	public void testGetUserByEmail() {
		String email = "dangvanan@gmail.com";
		User user = repo.getUserByEmail(email);
		assertThat(user).isNotNull();
	}

	@Test
	public void testCountById() {
		Integer id = 1;
		Long countById = repo.countById(id);
		assertThat(countById).isNotNull().isGreaterThan(0);
	}

	@Test
	public void updateEnabledStatusEnable() {
		Integer id = 1;
		repo.updateEnabledStatus(id, true);
	}

	@Test
	public void updateEnabledStatusDisable() {
		Integer id = 1;
		repo.updateEnabledStatus(id, false);
	}

	@Test
	public void testListFirstPage() {
		Integer pageNumber=0;
		Integer pageSize=4;
		Pageable pageable=PageRequest.of(pageNumber,pageSize);
		Page<User> page=repo.findAll(pageable);
		List<User> listUsers=page.getContent();
		listUsers.forEach(user -> System.out.println(user));
		assertThat(listUsers.size()).isEqualTo(pageSize);

	}
	@Test
	public void testFilterFunction() {
		String keyword="bruce";
		Integer pageNumber=0;
		Integer pageSize=4;
		Pageable pageable=PageRequest.of(pageNumber,pageSize);
		Page<User> page=repo.findAll(keyword,pageable);
		List<User> listUsers=page.getContent();
		listUsers.forEach(user -> System.out.println(user));
		assertThat(listUsers.size()).isGreaterThan(0);
		System.out.println(listUsers);
	}
}
