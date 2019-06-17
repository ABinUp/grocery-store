package cn.abin.grocerystore.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cn.abin.grocerystore.pojo.Order;
import cn.abin.grocerystore.pojo.User;

public interface OrderDAO extends JpaRepository<Order,Integer> {
	
	// 查看用户的订单集合，订单状态为未删除
	List<Order> findByUserAndStatusNotOrderByIdDesc(User user, String status);
}
