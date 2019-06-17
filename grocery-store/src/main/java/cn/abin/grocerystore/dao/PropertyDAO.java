package cn.abin.grocerystore.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import cn.abin.grocerystore.pojo.Category;
import cn.abin.grocerystore.pojo.Property;

public interface PropertyDAO extends JpaRepository<Property,Integer> {
	/**
	 *  通过cid查出已经分页的属性信息
	 * @param category
	 * @param pageable
	 * @return
	 */
	Page<Property> findByCategory(Category category,Pageable pageable);
	/**
	 *  通过cid查询所有属性
	 * @param category
	 * @return
	 */
	List<Property> findByCategory(Category category);
}
