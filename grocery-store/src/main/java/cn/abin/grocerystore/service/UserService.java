package cn.abin.grocerystore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import cn.abin.grocerystore.dao.UserDAO;
import cn.abin.grocerystore.pojo.User;
import cn.abin.grocerystore.util.Page4Navigator;
import cn.abin.grocerystore.util.SpringContextUtil;

@Service
@CacheConfig(cacheNames="users")
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
	@Cacheable(key="'users-pages-'+#p0+'-'+#p1")
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
		UserService bean = SpringContextUtil.getBean(UserService.class);
		User user = bean.getByName(name);
		return user!=null;
	}
	/**
	 *  通过用户名查找用户
	 * @param name
	 * @return
	 */
	@Cacheable(key="'users-one-name-'+#p0")
	public User getByName(String name) {
		return userDAO.findByName(name);
	}
	/**
	 *  增加用户
	 * @param user
	 */
	@CacheEvict(allEntries=true)
	public void add(User user) {
        userDAO.save(user);
    }
	
	/**
	 *  通过用户名和密码查询用户
	 * @param name
	 * @param password
	 * @return
	 */
	@Cacheable(key="'users-one-name-'+#p0+'-password-'+#p1")
	public User get(String name, String password) {
        return userDAO.getByNameAndPassword(name,password);
    }
}
