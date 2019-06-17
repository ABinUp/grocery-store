package cn.abin.grocerystore.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cn.abin.grocerystore.pojo.Product;
import cn.abin.grocerystore.pojo.ProductImage;

public interface ProductImageDAO extends JpaRepository<ProductImage,Integer>{
	// 根据图片的类型和商品id查询
	List<ProductImage> findByProductAndTypeOrderByIdDesc(Product product,String type);
}
