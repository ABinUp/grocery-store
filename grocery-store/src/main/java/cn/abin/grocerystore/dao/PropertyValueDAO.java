package cn.abin.grocerystore.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cn.abin.grocerystore.pojo.Product;
import cn.abin.grocerystore.pojo.Property;
import cn.abin.grocerystore.pojo.PropertyValue;

public interface PropertyValueDAO extends JpaRepository<PropertyValue,Integer> {
	
	List<PropertyValue> findByProductOrderByIdDesc(Product product);
	PropertyValue findByProductAndPropertyOrderByIdDesc(Product product,Property property);
}
