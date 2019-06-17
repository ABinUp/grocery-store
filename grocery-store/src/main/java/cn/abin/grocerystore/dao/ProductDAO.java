package cn.abin.grocerystore.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import cn.abin.grocerystore.pojo.Category;
import cn.abin.grocerystore.pojo.Product;

public interface ProductDAO extends JpaRepository<Product,Integer>{
	// 根据分类查询产品的分页信息
	Page<Product> findByCategory(Category category,Pageable pageable);
	
	List<Product> findByCategoryOrderById(Category category);
	
	// 模糊查询出指定分页的商品
	List<Product> findByNameLike(String name,Pageable pageable);
}
