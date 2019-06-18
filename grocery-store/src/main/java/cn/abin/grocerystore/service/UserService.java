package cn.abin.grocerystore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import cn.abin.grocerystore.dao.UserDAO;
import cn.abin.grocerystore.pojo.User;
import cn.abin.grocerystore.util.Page4Navigator;

@Service
public class UserService {
	@Autowired
	private UserDAO userDAO;
	
	/**
	 *  查询用户，启用分页
	 * @param start
	 * @param size
	 * @param navigatePages
	 * @return
	 */
	public Page4Navigator<User> list(int start,int size,int navigatePages){
		// 设置排序
		Sort sort = new Sort(Sort.Direction.DESC,"id");
		// 设置分页参数
		Pageable pageable = new PageRequest(start,size,sort);
		// 得到分页结果
		Page<User> page = userDAO.findAll(pageable);
		
		return new Page4Navigator<User>(page,navigatePages);
	}
	
	/**
	 *  查询用户名是否存在
	 * @param name
	 * @return
	 */
	public boolean isExist(String name) {
		User user = getByName(name);
		return user!=null;
	}
	/**
	 *  通过用户名查找用户
	 * @param name
	 * @return
	 */
	public User getByName(String name) {
		return userDAO.findByName(name);
	}
	/**
	 *  增加用户
	 * @param user
	 */
	public void add(User user) {
        userDAO.save(user);
    }
	
	/**
	 *  通过用户名和密码查询用户
	 * @param name
	 * @param password
	 * @return
	 */
	public User get(String name, String password) {
        return userDAO.getByNameAndPassword(name,password);
    }
}
