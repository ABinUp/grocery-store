package cn.abin.grocerystore.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import cn.abin.grocerystore.pojo.Category;

public interface CategoryDAO extends JpaRepository<Category,Integer>{
	
}
