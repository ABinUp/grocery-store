package cn.abin.grocerystore.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.abin.grocerystore.pojo.User;
import cn.abin.grocerystore.service.UserService;
import cn.abin.grocerystore.util.Page4Navigator;

@RestController
public class UserController {
	@Autowired
	private UserService userService;
	
	// 查询user
	@GetMapping("/users")
	public Page4Navigator<User> list(@RequestParam(name = "start",defaultValue="0")int start,
			@RequestParam(name = "size",defaultValue = "5")int size){
		// 边界
		start = start<0?0:start;
		// navigatePages 为5，显示5个导航页码
		return userService.list(start, size, 5);
	}
}
