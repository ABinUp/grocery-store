package cn.abin.grocerystore.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.abin.grocerystore.dao.ProductImageDAO;
import cn.abin.grocerystore.pojo.OrderItem;
import cn.abin.grocerystore.pojo.Product;
import cn.abin.grocerystore.pojo.ProductImage;

@Service
public class ProductImageService {
	public static final String type_single = "single";
    public static final String type_detail = "detail";
	
	@Autowired
	private ProductService prodectService;
	@Autowired
	private ProductImageDAO productImageDAO;
	
	// 查询单张图
	public List<ProductImage> listSingleProductImages(Product product){
		return productImageDAO.findByProductAndTypeOrderByIdDesc(product, type_single);
	}
	// 查询详情图
	public List<ProductImage> listDetailProductImages(Product product){
		return productImageDAO.findByProductAndTypeOrderByIdDesc(product, type_detail);
	}
	// 增加图片，图片中封装了类型type
	public void add(ProductImage pi) {
		productImageDAO.save(pi);
	}
	// 获取图片
	public ProductImage get(int id) {
		return productImageDAO.findOne(id);
	}
	
	// 删除图片
	public void delete(int id) {
		productImageDAO.delete(id);
	}
	
	// 设置首展图,让商品服务层在获取商品时就调用，再返回给表现层
	public void setFirstImage(Product product) {
		// 拿到该商品的单张图,这么干，前端页面取首展图时会报错，导致一个商品也不会显示，应该考虑没有图片的情况
		// 问题出在集合可能为空，空调用get(0)，服务器出错。所以应该判断集合是否为空
		//		ProductImage pi = listSingleProductImages(product).get(0);
//		if(pi!=null) {
//			product.setFirstProductImage(pi);
//		}
		 List<ProductImage> singleImages = listSingleProductImages(product);
	        if(!singleImages.isEmpty())
	            product.setFirstProductImage(singleImages.get(0));
	        else
	            product.setFirstProductImage(new ProductImage()); //这样做是考虑到产品还没有来得及设置图片，但是在订单后台管理里查看订单项的对应产品图片。
	        	// 这里最好不要设置为null，因为前台Thymeleaf在取值时用到了firstProductImage.id，如果为null，调用方法出错，导致页面大部分受影响。
	}
	
	public void setFirstImage(List<Product> ps) {
		for(Product p:ps) {
			setFirstImage(p);
		}
	}
	public void setFirstProdutImagesOnOrderItems(List<OrderItem> orderItems) {
		for(OrderItem oi:orderItems) {
			setFirstImage(oi.getProduct());
		}
		
	}
}
