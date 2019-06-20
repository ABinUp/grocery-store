package cn.abin.grocerystore.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import cn.abin.grocerystore.dao.PropertyValueDAO;
import cn.abin.grocerystore.pojo.Category;
import cn.abin.grocerystore.pojo.Product;
import cn.abin.grocerystore.pojo.Property;
import cn.abin.grocerystore.pojo.PropertyValue;
import cn.abin.grocerystore.util.SpringContextUtil;

@Service
@CacheConfig(cacheNames="propertyValues")
public class PropertyValueService {
	@Autowired
	private PropertyValueDAO propertyValueDAO;
	@Autowired
	private PropertyService propertyService;
	/**
	 *  得到某商品的所有属性值集合，
	 * @param p
	 * @return
	 */
	@Cacheable(key="'propertyValues-'+#p0.id")
	public List<PropertyValue> list(Product p){
		// 查询时才检查初始化
		init(p);
		List<PropertyValue> pvs = propertyValueDAO.findByProductOrderByIdDesc(p);
		// 设置属性，便于调用，获取属性名，其实自动获取了，因为实体中绑定了Property属性
//		setProperty(pvs);
		return pvs;
	}


	/**
	 *  根据商品和商品的属性得到唯一的商品的属性值
	 * @param product
	 * @param property
	 * @return
	 */
	@Cacheable(key="'propertyValues-pid-'+#p0.id+'-propertyid-'+#p1.id")
	public PropertyValue get(Product product,Property property){
		PropertyValue pv = propertyValueDAO.findByProductAndPropertyOrderByIdDesc(product, property);
		// 设置属性，便于调用，获取属性名
//		setProperty(pv);
		return pv;
	}
	/**
	 * 2 首先根据产品获取分类，然后获取这个分类下的所有属性集合
	 * 3 然后用属性id和产品id去查询，看看这个属性和这个产品，是否已经存在属性值了。
	 *4 如果不存在，那么就创建一个属性值，并设置其属性和产品，接着插入到数据库中。
	 *这样就完成了属性值的初始化。
	 */
	
	public void init(Product product) {
		PropertyValueService bean = SpringContextUtil.getBean(PropertyValueService.class);
		Category category = product.getCategory();
		List<Property> propertyList = propertyService.list(category.getId());
		// foreach 集合为空不会影响，直接不执行而已
		for(Property property:propertyList) {
			// 查询唯一的属性值
			PropertyValue pv = bean.get(product,property);
			// 如果数据库中没有，则绑定商品和属性，再保存到数据库
			if(pv==null) {
				pv = new PropertyValue();
				pv.setProduct(product);
				pv.setProperty(property);
				// 保存到数据库
				bean.add(pv);
			}
		}
	}
	
	/**
	 *  增加属性值字段
	 * @param pv
	 */
	@CacheEvict(allEntries = true)
	public void add(PropertyValue pv) {
		propertyValueDAO.save(pv);
	}
	/**
	 *  更改属性值
	 * @param pv
	 */
	@CacheEvict(allEntries = true)
	public void update(PropertyValue pv) {
		propertyValueDAO.save(pv);
	}
	
	/**
	 *  设置属性，可以不用，在实体中声明了对应字段的映射，自动装配
	 */
//	private void setProperty(PropertyValue pv) {
//		Property property = propertyService.get(pv.getProperty().getId());
//		pv.setProperty(property);
//	}
//	private void setProperty(List<PropertyValue> pvs) {
//		for(PropertyValue pv:pvs) {
//			setProperty(pv);
//		}
//	}
}
