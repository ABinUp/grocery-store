package cn.abin.grocerystore.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cn.abin.grocerystore.pojo.Product;
import cn.abin.grocerystore.pojo.Review;

public interface ReviewDAO extends JpaRepository<Review,Integer>{
	// 根据商品查询出评价
	List<Review> findByProductOrderByIdDesc(Product product);
	// 根据商品查出评价数，类似于group by
	int countByProduct(Product product);
}
