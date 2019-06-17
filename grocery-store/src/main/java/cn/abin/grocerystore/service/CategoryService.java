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

import cn.abin.grocerystore.dao.CategoryDAO;
import cn.abin.grocerystore.pojo.Category;
import cn.abin.grocerystore.pojo.Product;
import cn.abin.grocerystore.util.Page4Navigator;

@Service
@CacheConfig(cacheNames="categories")
public class CategoryService {
	@Autowired
	private CategoryDAO categoryDAO;
	
	@Cacheable(key="'categories-all'")
	public List<Category> list(){
		Sort sort = new Sort(Sort.Direction.DESC,"id");
		return categoryDAO.findAll(sort);
	}
	
	@Cacheable(key="'categories-page-'+#p0+ '-' + #p1")
	/**
	 *  使用分页
	 * @param start
	 * @param size
	 * @param navigatePages
	 * @return
	 */
	public Page4Navigator<Category> list(int start,int size,int navigatePages){
		Sort sort = new Sort(Sort.Direction.DESC,"id");
		Pageable pageable = new PageRequest(start,size,sort);
		Page pageFromJPA = categoryDAO.findAll(pageable);
		
		return new Page4Navigator<Category>(pageFromJPA,navigatePages);
	}
	
	@CacheEvict(allEntries=true)
//  @CacheEvict(key="'category-one-'+ #p0")
	/**
	 *  增加
	 * @param bean
	 */
	public void add(Category bean) {
		categoryDAO.save(bean);
	}
	
	@CacheEvict(allEntries=true)
//  @CacheEvict(key="'category-one-'+ #p0")
	/**
	 *  删除
	 * @param id
	 */
	public void delete(int id) {
		categoryDAO.delete(id);
	}
	
	@Cacheable(key="'categories-one-'+ #p0")
	/**
	 *  获取分类信息
	 * @param id
	 * @return
	 */
	public Category get(int id) {
		return categoryDAO.findOne(id);
	}
	
	@CacheEvict(allEntries=true)
//  @CachePut(key="'category-one-'+ #p0")
	/**
	 *  修改分类
	 * @param bean
	 */
	public void update(Category bean) {
		categoryDAO.save(bean);
	}
	
	/**
	 *  清空分类下的products中product的category，防止无限循环
	 * @param category
	 */
	public void removeCategoryFromProduct(Category category) {
        List<Product> products =category.getProducts();
        if(null!=products) {
            for (Product product : products) {
                product.setCategory(null);
            }
        }
 
        List<List<Product>> productsByRow =category.getProductsByRow();
        if(null!=productsByRow) {
            for (List<Product> ps : productsByRow) {
                for (Product p: ps) {
                    p.setCategory(null);
                }
            }
        }
    }
	public void removeCategoryFromProduct(List<Category> cs) {
        for (Category category : cs) {
            removeCategoryFromProduct(category);
        }
    }
}
