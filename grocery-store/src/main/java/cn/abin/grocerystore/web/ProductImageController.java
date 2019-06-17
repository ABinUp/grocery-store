package cn.abin.grocerystore.web;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cn.abin.grocerystore.pojo.Product;
import cn.abin.grocerystore.pojo.ProductImage;
import cn.abin.grocerystore.service.ProductImageService;
import cn.abin.grocerystore.service.ProductService;
import cn.abin.grocerystore.util.ImageUtil;

@RestController
public class ProductImageController {
	
	@Autowired
	private ProductImageService productImageService;
	@Autowired
	private ProductService productService;
	// 查询图片
	@GetMapping("/products/{id}/productImages")
	public List<ProductImage> list(@PathVariable(name = "id") int id,
			@RequestParam(name = "type")String type){
		Product p = productService.get(id);
		// 根据type确定调用哪个服务层方法
		if(ProductImageService.type_single.equals(type)) {
			return productImageService.listSingleProductImages(p);
		}
		if(ProductImageService.type_detail.equals(type)) {
			return productImageService.listDetailProductImages(p);
		}
		return new ArrayList<>();
	}
	// 增加图片，根据参数type确实图片类型
	// single 只需添加一张，detail则添加三张
	@PostMapping("/productImages")
	public Object add(@RequestParam(name = "pid")int pid,
			@RequestParam(name = "type")String type,
			MultipartFile image,HttpServletRequest request) {
		// 创建一个商品图片对象，封装商品属性，封装类型
		 ProductImage bean = new ProductImage();
	        Product product = productService.get(pid);
	        bean.setProduct(product);
	        bean.setType(type);
	         // 数据库中添加
	        productImageService.add(bean);
	        // 根据商品图片的类型，确定图片的存放目录
	        String folder = "img/";
	        if(ProductImageService.type_single.equals(bean.getType())){
	            folder +="productSingle";
	        }
	        else{
	            folder +="productDetail";
	        }
	        // 
	        File  imageFolder= new File(request.getServletContext().getRealPath(folder));
	        File file = new File(imageFolder,bean.getId()+".jpg");
	        // 拉取文件名，若是详情图，则有用。
	        String fileName = file.getName();
	        // 保证目录创建
	        if(!file.getParentFile().exists())
	            file.getParentFile().mkdirs();
	        try {
	            image.transferTo(file);
	            BufferedImage img = ImageUtil.change2jpg(file);
	            ImageIO.write(img, "jpg", file);           
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	         // 如果是详情图，则继续将小图和中图创建完
	        if(ProductImageService.type_single.equals(bean.getType())){
	            String imageFolder_small= request.getServletContext().getRealPath("img/productSingle_small");
	            String imageFolder_middle= request.getServletContext().getRealPath("img/productSingle_middle");    
	            File f_small = new File(imageFolder_small, fileName);
	            File f_middle = new File(imageFolder_middle, fileName);
	            f_small.getParentFile().mkdirs();
	            f_middle.getParentFile().mkdirs();
	            ImageUtil.resizeImage(file, 56, 56, f_small);
	            ImageUtil.resizeImage(file, 217, 190, f_middle);
	        }      
	         // 返回商品图片对象
	        return bean;
	}
	// 删除商品图片
	// 根据type确定删除的是single还是detail
	@DeleteMapping("/productImages/{id}")
	public String delete(@PathVariable(name = "id")int id,HttpServletRequest request) {
		// 根据id得到商品图片类，从而获取图片的类型信息，再确定删除方案
		ProductImage pi = productImageService.get(id);
		// 数据库删除
		productImageService.delete(id);
		String type = pi.getType();
		// 巧妙删除一张
		String folder = "img/";
		if(ProductImageService.type_single.equals(type)) {
			folder += "productSingle";
		}else{
            folder +="productDetail";
        }
		File imageFolder = new File(folder);
		File image = new File(imageFolder,id+".jpg");
		String fileName = image.getName();
		image.delete();
		
		// 如果是详情图，还有两张要删
		if(ProductImageService.type_detail.equals(type)) {
			String imageFolder_small= request.getServletContext().getRealPath("img/productSingle_small");
            String imageFolder_middle= request.getServletContext().getRealPath("img/productSingle_middle");    
            File f_small = new File(imageFolder_small, fileName);
            File f_middle = new File(imageFolder_middle, fileName);
            f_small.delete();
            f_middle.delete();
		}
		return null;
	}
}
