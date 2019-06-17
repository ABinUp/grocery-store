package cn.abin.grocerystore.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.abin.grocerystore.pojo.Property;
import cn.abin.grocerystore.service.PropertyService;
import cn.abin.grocerystore.util.Page4Navigator;

@RestController
public class PropertyController {

	@Autowired
	private PropertyService propertyService;
	
	// ajax访问的url为/categories/{cid}/properties？start=	这里size采用默认值
	@GetMapping("/categories/{cid}/properties")
	public Page4Navigator<Property> list(@PathVariable(name="cid") int cid,
			@RequestParam(value = "start", defaultValue = "0") int start,
			@RequestParam(value = "size", defaultValue = "5") int size) throws Exception {
		start = start<0?0:start;
		// 直接传导航页码数为5个，navigatePages
		Page4Navigator<Property> page = propertyService.list(cid, start, size, 5);
		
		return page;
	}
	
	// 增加,注意前端传参是json，所以requestBody
	@PostMapping("/properties")
	public Object add(@RequestBody Property bean) throws Exception {
		propertyService.add(bean);
		return  bean;
	}
	// 获取，在编辑时调用
	@GetMapping("/properties/{id}")
	public Object get(@PathVariable(name = "id") int id) throws Exception {
		
		return propertyService.get(id);
	}
	
	// 修改
	@PutMapping("/properties")
	public Object update(@RequestBody Property bean) {
		propertyService.update(bean);
		return bean;
	}
	
	// 删除
	@DeleteMapping("/properties/{id}")
	public String delete(@PathVariable(name = "id")int id) {
		propertyService.delete(id);
		return null;	// 自动转为空串""
	}
}
