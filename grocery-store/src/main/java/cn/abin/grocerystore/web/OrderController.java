package cn.abin.grocerystore.web;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.abin.grocerystore.pojo.Order;
import cn.abin.grocerystore.service.OrderItemService;
import cn.abin.grocerystore.service.OrderService;
import cn.abin.grocerystore.util.Page4Navigator;
import cn.abin.grocerystore.util.Result;

@RestController
public class OrderController {
	@Autowired
	private OrderService orderService;
	@Autowired
	private OrderItemService orderItemService;
	// 订单查询
	@GetMapping("/orders") 
	public Page4Navigator<Order> list(@RequestParam(name = "start",defaultValue="0")int start,
			@RequestParam(name = "size",defaultValue = "5")int size){
		// 边界
		start = start<0?0:start;
		// navigatePages 为5，显示5个导航页码
		Page4Navigator<Order> pageInfo = orderService.list(start, size, 5);
		// 订单的非数据库字段orderItems没有数据，需要手动绑定
		// 调用orderItemService的方法绑定订单的订单项，同时计算出其它非数据库字段值
		orderItemService.fill(pageInfo.getContent());
		// 调用orderItemService的方法清空，order下的orderItems中orderItem的order属性，避免无限循环
		orderItemService.removeOrder(pageInfo.getContent());
		
		return pageInfo;
	}
	// 发货
	@PutMapping("/deliveryOrder/{id}")
	public Object delivery(@PathVariable(name = "id")int id) {
		// 根据id查出订单，并改变订单状态为已发货，生成发货日期
		Order order = orderService.get(id);
		order.setDeliveryDate(new Date());
		order.setStatus(OrderService.waitConfirm);
		// 回写数据库
		orderService.update(order);
		
		return Result.success();
	}
}
