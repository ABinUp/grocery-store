package cn.abin.grocerystore.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import cn.abin.grocerystore.pojo.Product;
import cn.abin.grocerystore.pojo.PropertyValue;
import cn.abin.grocerystore.service.ProductService;
import cn.abin.grocerystore.service.PropertyValueService;

@RestController
public class PropertyValueController {
	@Autowired
	private PropertyValueService propertyValueService;
	@Autowired
	private ProductService productService;
	// 查询商品的属性值
	@GetMapping("/products/{pid}/propertyValues")
	public List<PropertyValue> list(@PathVariable(name = "pid")int pid){
		Product p = productService.get(pid);
		 
		return propertyValueService.list(p);
	}
	// 更新商品的属性值
	@PutMapping("/propertyValues")
	public Object update(@RequestBody PropertyValue bean) {
		propertyValueService.update(bean);
		return bean;
	}
}
