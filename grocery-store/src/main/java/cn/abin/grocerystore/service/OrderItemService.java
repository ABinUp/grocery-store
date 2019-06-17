package cn.abin.grocerystore.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.abin.grocerystore.dao.OrderItemDAO;
import cn.abin.grocerystore.pojo.Order;
import cn.abin.grocerystore.pojo.OrderItem;
import cn.abin.grocerystore.pojo.Product;
import cn.abin.grocerystore.pojo.User;

@Service
public class OrderItemService {
	
	
	@Autowired
	private OrderItemDAO orderItemDAO;
	@Autowired
	private ProductImageService productImageService;
	
	// 查出某订单的所有订单项
	public List<OrderItem> list(Order order){
		List<OrderItem> ois = orderItemDAO.findByOrderOrderByIdDesc(order);
		
		return ois;
	}
	// 根据商品查出订单项
	public List<OrderItem> list(Product product){
		return orderItemDAO.findByProduct(product);
	}
	
	// 根据用户查出未生成订单的订单项
	public List<OrderItem> list(User user){
		return orderItemDAO.findByUserAndOrderIsNull(user);
	}
	
	// 增加订单项，没有生成订单
	public void add(OrderItem orderItem) {
		orderItemDAO.save(orderItem);
	}
	
	// 更新订单项
	public void update(OrderItem oi) {
		orderItemDAO.save(oi);
	}
	// 删除订单项
	public void delete(int oiid) {
		orderItemDAO.delete(oiid);
		
	}
	
	// 根据订单项id查出订单项
	public OrderItem get(int id) {
		return orderItemDAO.getOne(id);
	}
	
	// 得到销量
	public int getSaleCount(Product product) {
		List<OrderItem> ois = list(product);
		int saleCount=0;
		for(OrderItem oi:ois) {
			// 为空判断，排除掉未生成订单的临时订单项，以及为付款的订单项
			// 这个订单项是服务层调用DAO查出来，自动装配了Order，即有oid及order的其它数据库字段
			if(oi.getOrder()!=null&&null!=oi.getOrder().getPayDate())
				saleCount += oi.getNumber();
 		}
		return saleCount;
	}
	
	// 订单的订单项填充，同时计算出订单的其它值，// 注意商品首展图
	public void fill(Order order) {
		// 查出该订单对应的所有订单项集合
		List<OrderItem> ois = list(order);
		// 遍历集合，计算总价和总数
		float total = 0;
		int totalNumber = 0;
		for(OrderItem oi:ois) {
			// 得到订单项的商品，这里的商品没有经过服务层调用，所以未设置首展图，需要手动
			Product product = oi.getProduct();
			// 根据订单项的商品属性计算
			total += product.getPromotePrice();
			totalNumber += oi.getNumber();
			// 注意商品首展图为非数据库字段，所以填充时需要设置
			productImageService.setFirstImage(product);
			
		}
		
		// 将该订单的订单项集合属性设置
		order.setOrderItems(ois);
		// 设置其它
		order.setTotal(total);
		order.setTotalNumber(totalNumber);
		
	}
	public void fill(List<Order> os) {
		for(Order o:os) {
			fill(o);
		}
		
	}
	
	// 清空orderItems的orderItem的order
	public void removeOrder(Order o) {
		List<OrderItem> orderItems = o.getOrderItems();
		for(OrderItem oi:orderItems) {
			// 将order设置为null
			oi.setOrder(null);
		}
	}
	public void removeOrder(List<Order> os) {
		for(Order o:os) {
			removeOrder(o);
		}
	}
	
	
}
