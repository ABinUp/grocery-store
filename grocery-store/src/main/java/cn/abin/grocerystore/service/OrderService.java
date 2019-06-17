package cn.abin.grocerystore.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cn.abin.grocerystore.dao.OrderDAO;
import cn.abin.grocerystore.pojo.Order;
import cn.abin.grocerystore.pojo.OrderItem;
import cn.abin.grocerystore.pojo.Review;
import cn.abin.grocerystore.pojo.User;
import cn.abin.grocerystore.util.Page4Navigator;

@Service
public class OrderService {
	public static final String waitPay = "waitPay";
    public static final String waitDelivery = "waitDelivery";
    public static final String waitConfirm = "waitConfirm";
    public static final String waitReview = "waitReview";
    public static final String finish = "finish";
    public static final String delete = "delete";  
	@Autowired
	private OrderDAO orderDAO;
	@Autowired
	private OrderItemService orderItemService;
	@Autowired
	private ReviewService reviewService;
	
	
	/**
	 *  查出所有订单，分页
	 * @param start
	 * @param size
	 * @param navigatePages
	 * @return
	 */
	public Page4Navigator<Order> list(int start,int size,int navigatePages){
		Sort sort = new Sort(Sort.Direction.DESC,"id");
		Pageable pageable = new PageRequest(start,size,sort);
		Page<Order> page = orderDAO.findAll(pageable);
		
		return new Page4Navigator<Order>(page,navigatePages);
	}
	
	/**
	 *  根据id查出订单
	 * @param id
	 * @return
	 */
	public Order get(int id) {
		return orderDAO.getOne(id);
	}
	/**
	 *  修改订单
	 * @param order
	 */
	public void update(Order order) {
		orderDAO.save(order);
	}
	/**
	 *  增加订单
	 * @param order
	 */
	public void add(Order order) {
		orderDAO.save(order);
	}
	/**
	 *  删除订单
	 * @param id
	 */
	public void delete(int id) {
		orderDAO.delete(id);
	}
	
	/**
	 *  临时订单项生成订单时，开启事务配置，只有当所有订单项都完成数据库订单id的绑定后，订单才真正生成 201906151311
	 * @param order
	 * @param ois
	 * @return
	 */
	@Transactional(propagation= Propagation.REQUIRED,rollbackForClassName="Exception")
    public float add(Order order, List<OrderItem> ois) {
        float total = 0;
        add(order);
 
//        if(false)
//            throw new RuntimeException();
 
        for (OrderItem oi: ois) {
        	// 设置订单
            oi.setOrder(order);
            // 数据库更新
            orderItemService.update(oi);
            // 计算总价
            total+=oi.getProduct().getPromotePrice()*oi.getNumber();
        }
        return total;
    }
	
	/**
	 *  查出用户的订单集合，状态为未删除
	 * @param user
	 * @return
	 */
	public List<Order> listByUserWithoutDelete(User user) {
        List<Order> orders = listByUserAndNotDeleted(user);
        // 填充订单项，保证非数据库字段完整
        orderItemService.fill(orders);
        return orders;
    }
 
    public List<Order> listByUserAndNotDeleted(User user) {
        return orderDAO.findByUserAndStatusNotOrderByIdDesc(user, OrderService.delete);
    }
    
    // 在评论中学到了，要操作多张表时，需要开启事务，保证操作的完整性，一致性
    /**
     *  更新订单状态为完成，增加评价
     * @param o
     * @param review
     */
    @Transactional(propagation= Propagation.REQUIRED,rollbackForClassName="Exception")
	public void updateOrderAndAddReview(Order o, Review review) {
    	reviewService.add(review);
    	
    	update(o);
	}
}
