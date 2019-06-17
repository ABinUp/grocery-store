package cn.abin.grocerystore.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cn.abin.grocerystore.pojo.Order;
import cn.abin.grocerystore.pojo.OrderItem;
import cn.abin.grocerystore.pojo.Product;
import cn.abin.grocerystore.pojo.User;

public interface OrderItemDAO extends JpaRepository<OrderItem,Integer>{
	List<OrderItem> findByOrderOrderByIdDesc(Order order);
	// 根据商品查出订单项
	List<OrderItem> findByProduct(Product product);
	// 查询某用户未生成订单的订单项
	List<OrderItem> findByUserAndOrderIsNull(User user);
}
