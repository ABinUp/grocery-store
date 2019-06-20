package cn.abin.grocerystore.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import cn.abin.grocerystore.dao.ReviewDAO;
import cn.abin.grocerystore.pojo.Product;
import cn.abin.grocerystore.pojo.Review;

@Service
@CacheConfig(cacheNames="reviews")
public class ReviewService {
	@Autowired
	private ReviewDAO reviewDAO;
	
	/**
	 *  根据商品查出评价,评价实体中已绑定了user,product
	 * @param product
	 * @return
	 */
	@Cacheable(key="'reviews-pid-'+#p0.id")
	public List<Review> list(Product product){
		return reviewDAO.findByProductOrderByIdDesc(product);
	}
	/**
	 *  查出商品评价数
	 * @param product
	 * @return
	 */
	@Cacheable(key="'reviews-count-'+#p0.id")
	public int count(Product product) {
		return reviewDAO.countByProduct(product);
	}
	@CacheEvict(allEntries=true)
	public void add(Review review) {
		reviewDAO.save(review);
		
	}
}
