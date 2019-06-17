package cn.abin.grocerystore.web;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.abin.grocerystore.pojo.Product;
import cn.abin.grocerystore.service.ProductService;
import cn.abin.grocerystore.util.Page4Navigator;

// restful 风格，且自动转为json传输到页面，但页面传过来的json仍需requestBody
@RestController
public class ProductController {
	@Autowired
	private ProductService productService;
	
	@GetMapping("/categories/{cid}/products")
	public Page4Navigator<Product> list(@PathVariable(name = "cid")int cid,
			@RequestParam(name = "start",defaultValue="0")int start,
			@RequestParam(name = "size",defaultValue="5")int size) {
		// 边界
		start = start<0?0:start;
		
		return productService.list(cid, start, size, 5);
	}
	
	// 增加，注意与页面传参对应bean
	@PostMapping("/products")
	public Object add(@RequestBody Product bean) {
		// 商品上架日期
		bean.setCreateDate(new Date());
		productService.add(bean);
		return bean;
	}
	
	// 获取
	@GetMapping("/products/{id}")
	public Product get(@PathVariable(name = "id") int id) {
		return productService.get(id);
	}
	// 修改
	@PutMapping("/products")
	public Object update(@RequestBody Product bean) {
		productService.update(bean);
		return bean;
	}
	// 删除
	@DeleteMapping("/products/{id}")
	public String delete(@PathVariable(name = "id")int id) {
		productService.delete(id);
		return null;	// 自动转为空串
	}
}
