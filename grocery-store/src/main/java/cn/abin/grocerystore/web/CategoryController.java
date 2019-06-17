package cn.abin.grocerystore.web;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cn.abin.grocerystore.pojo.Category;
import cn.abin.grocerystore.service.CategoryService;
import cn.abin.grocerystore.util.ImageUtil;
import cn.abin.grocerystore.util.Page4Navigator;


@RestController		// 自动转为Json返回页面
public class CategoryController {
	@Autowired
	private CategoryService categoryService;
	
	@GetMapping("/categories")
	public Page4Navigator<Category> listCategory(@RequestParam (value = "start",defaultValue = "0") int start,
			@RequestParam (value = "size" ,defaultValue = "5") int size) throws Exception {
		// 边界
		start= start<0?0:start;
		Page4Navigator<Category> page = categoryService.list(start, size, 5); //5表示导航分页最多有5个，像 [1,2,3,4,5] 这样
		return page;
	}
	// 增加,这里bean是通过formdata过来的name自动封装的，不用@RequestBody
	@PostMapping("/categories")
	public Object addCategory(Category bean,MultipartFile image,HttpServletRequest request) throws Exception {
		// 页面已经做了非空不执行处理，所以这里的bean和image未非空
		// 调用服务增加分类，同时会自动主键生成并封装到bean
		categoryService.add(bean);
		// 封装一个存储图片的方法
		saveOrUpdateImageFile(bean,image,request);
		
		return bean;
	}
	private void saveOrUpdateImageFile(Category bean, MultipartFile image, HttpServletRequest request) throws IOException {
		// 创建图片目录对象，再创建图片对象
		File imageFolder = new File(request.getServletContext().getRealPath("img/category"));
		imageFolder.mkdirs();
		File img = new File(imageFolder,bean.getId()+".jpg");
		image.transferTo(img);
		// 调用工具类
		BufferedImage bImg = ImageUtil.change2jpg(img);
		// 调用图片处理类
        ImageIO.write(bImg, "jpg", img);
	}
	
	// 删除
	@DeleteMapping("/categories/{id}")
	public String deleteCategory(@PathVariable int id,HttpServletRequest request) {
		// 删除分类，删除分类对应的图片
		categoryService.delete(id);
		File imageFolder = new File(request.getServletContext().getRealPath("img/category"));
		 
		File img = new File(imageFolder,id+".jpg"); 
		img.delete();
		
		return null;	// null被RestController自动转为""空串，长度为0
	}
	
	// 获取
	@GetMapping("/categories/{id}")
	public Category getCategory(@PathVariable int id) {
		return categoryService.get(id);
	}
	// 更新
	// 201906131325这么理解吧，putMapping自动封装了url路径的id到bean，但是传参的name却没封装
	@PutMapping("/categories/{id}")
	public Object updateCategory(Category bean, MultipartFile image,HttpServletRequest request) throws Exception {
		// 获取name
		String name = request.getParameter("name");
		bean.setName(name);
		categoryService.update(bean);
		System.out.println(bean.getId());
		if(image!=null) {
			saveOrUpdateImageFile(bean, image, request);
		}
		return bean;
	}
	
	
}
