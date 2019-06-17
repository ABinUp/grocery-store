package cn.abin.grocerystore.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import cn.abin.grocerystore.pojo.User;

public interface UserDAO extends JpaRepository<User,Integer>{
	User findByName(String name);

	User getByNameAndPassword(String name, String password);
}
