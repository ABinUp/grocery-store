package cn.abin.grocerystore.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.abin.grocerystore.dao.ReviewDAO;
import cn.abin.grocerystore.pojo.Product;
import cn.abin.grocerystore.pojo.Review;

@Service
public class ReviewService {
	@Autowired
	private ReviewDAO reviewDAO;
	
	// 根据商品查出评价,评价实体中已绑定了user,product
	public List<Review> list(Product product){
		return reviewDAO.findByProductOrderByIdDesc(product);
	}
	// 查出商品评价数
	public int count(Product product) {
		return reviewDAO.countByProduct(product);
	}
	public void add(Review review) {
		reviewDAO.save(review);
		
	}
}
