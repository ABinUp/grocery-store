package cn.abin.grocerystore.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import cn.abin.grocerystore.dao.PropertyDAO;
import cn.abin.grocerystore.pojo.Category;
import cn.abin.grocerystore.pojo.Property;
import cn.abin.grocerystore.util.Page4Navigator;

@Service
@CacheConfig(cacheNames = "properties")
public class PropertyService {
	@Autowired
	private PropertyDAO propertyDAO;
	@Autowired
	private CategoryService categoryService;

	/**
	 * 根据cid 查询对应的属性
	 * 
	 * @param cid
	 * @param start
	 * @param size
	 * @param navigatePages
	 * @return
	 */
	@Cacheable(key = "'properties-cid-'+#p0+'-pages-'+#p1+'-'+#p2")
	public Page4Navigator<Property> list(int cid, int start, int size, int navigatePages) {

		Category category = categoryService.get(cid);
		// 分页准备
		Sort sort = new Sort(Sort.Direction.DESC, "id");
		Pageable pageable = new PageRequest(start, size, sort);
		// 得出分页数据
		Page<Property> pageFromJPA = propertyDAO.findByCategory(category, pageable);

		return new Page4Navigator<Property>(pageFromJPA, navigatePages);
	}

	/**
	 * 查询分类下所有属性
	 * 
	 * @param cid
	 * @return
	 */
	@Cacheable(key="'properties-cid'+#p0")
	public List<Property> list(int cid) {
		Category category = categoryService.get(cid);
		return propertyDAO.findByCategory(category);
	}

	/**
	 * 增加
	 * 
	 * @param bean
	 */
	@CacheEvict(allEntries=true)
	public void add(Property bean) {
		propertyDAO.save(bean);
	}

	/**
	 * 获取
	 * 
	 * @param id
	 * @return
	 */
	@Cacheable(key="'property-one'+#p0")
	public Property get(int id) {
		return propertyDAO.findOne(id);
	}
	@CacheEvict(allEntries=true)
	public void update(Property bean) {
		propertyDAO.save(bean);

	}
	@CacheEvict(allEntries=true)
	public void delete(int id) {
		propertyDAO.delete(id);

	}
}
