package cn.abin.grocerystore.service;

import java.util.ArrayList;
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

import cn.abin.grocerystore.dao.ProductDAO;
import cn.abin.grocerystore.pojo.Category;
import cn.abin.grocerystore.pojo.Product;
import cn.abin.grocerystore.util.Page4Navigator;
import cn.abin.grocerystore.util.SpringContextUtil;

@Service
@CacheConfig(cacheNames="products")
public class ProductService {
	@Autowired
	private ProductDAO productDAO;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private ProductImageService productImageService;
	@Autowired
	private PropertyValueService propertyValueService;
	@Autowired
	private OrderItemService orderItemService;
	@Autowired
	private ReviewService reviewService;
	
	/**
	 *  查询分类下的商品
	 * @param cid
	 * @param start
	 * @param size
	 * @param navigatePages
	 * @return
	 */
	@Cacheable(key="'products-cid-'+#p0+'-page-'+#p1 + '-' + #p2 ")
	public Page4Navigator<Product> list(int cid,int start,int size,int navigatePages){
		Category category = categoryService.get(cid);
		// 分页准备
		Sort sort = new Sort(Sort.Direction.DESC,"id");
		Pageable pageable = new PageRequest(start,size,sort);
		Page<Product> pageFromJPA = productDAO.findByCategory(category, pageable);
		// 设置首展图
		productImageService.setFirstImage(pageFromJPA.getContent());
		return new Page4Navigator<Product>(pageFromJPA,navigatePages); 
	}
	/**
	 *  根据分类查询商品
	 * @param category
	 * @return
	 */
	@Cacheable(key="'products-cid-'+ #p0.id")
	public List<Product> listByCategory(Category category){
		
		List<Product> products = productDAO.findByCategoryOrderById(category);
		productImageService.setFirstImage(products);
		return products;
	}
	/**
	 *  模糊查询
	 * @param start
	 * @param size
	 * @param name
	 * @return
	 */
	public List<Product> search(int start,int size,String name){
		Sort sort = new Sort(Sort.Direction.DESC,"id");
		Pageable pageable = new PageRequest(start,size,sort);
		// 注意模糊查询的条件设置"_"和"%"等
		List<Product> ps = productDAO.findByNameLike("%"+name+"%", pageable);
		productImageService.setFirstImage(ps);
		return ps;
	}
	
	/**
	 *  增加
	 * @param product
	 */
	@CacheEvict(allEntries=true)
	public void add(Product product) {
		productDAO.save(product);
		// 初始化属性值放在属性值查询时调用，避免有了商品，但之前没初始化，再获取属性值就出错。
		
	}
	/**
	 *  获取
	 * @param id
	 * @return
	 */
	@Cacheable(key="'products-one-'+ #p0")
	public Product get(int id) {
		Product p = productDAO.findOne(id);
		productImageService.setFirstImage(p);
		return p;
	}
	/**
	 *  更新
	 * @param bean
	 */
	@CacheEvict(allEntries=true)
	public void update(Product bean) {
		productDAO.save(bean);
		
	}
	/**
	 *  删除
	 * @param id
	 */
	@CacheEvict(allEntries=true)
	public void delete(int id) {
		productDAO.delete(id);
	}
	/**
	 *  设置分类下的所有商品集合，未做分页处理，可改进
	 * @param categorys
	 */
	public void fill(List<Category> categorys) {
        for (Category category : categorys) {
            fill(category);
        }
    }
	/**
	 *  通过绕一绕，诱发AOP，获取本服务类，在调用方法，才会走缓存
	 * @param category
	 */
    public void fill(Category category) {
    	ProductService productService = SpringContextUtil.getBean(ProductService.class);
    	// 通过服务层获取商品时，已设置好首展图
        List<Product> products = productService.listByCategory(category);
        
        category.setProducts(products);
    }
 
    public void fillByRow(List<Category> categorys) {
        int productNumberEachRow = 8;
        for (Category category : categorys) {
            List<Product> products =  category.getProducts();
            List<List<Product>> productsByRow =  new ArrayList<>();
            for (int i = 0; i < products.size(); i+=productNumberEachRow) {
                int size = i+productNumberEachRow;
                size= size>products.size()?products.size():size;
                List<Product> productsOfEachRow =products.subList(i, size);
                productsByRow.add(productsOfEachRow);
            }
            category.setProductsByRow(productsByRow);
        }
    }
    
    /**
     *  为产品设置销量和评价数
     * @param product
     */
    public void setSaleAndReviewNumber(Product product) {
    	int saleCount = orderItemService.getSaleCount(product);
    	product.setSaleCount(saleCount);
    	int reviewCount = reviewService.count(product);
    	product.setReviewCount(reviewCount);
    }
    public void setSaleAndReviewNumber(List<Product> products) {
    	for(Product product:products) {
    		setSaleAndReviewNumber(product);
    	}
    }
    
}
