package cn.abin.grocerystore.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import cn.abin.grocerystore.pojo.Category;
import cn.abin.grocerystore.pojo.Order;
import cn.abin.grocerystore.pojo.OrderItem;
import cn.abin.grocerystore.pojo.Product;
import cn.abin.grocerystore.pojo.ProductImage;
import cn.abin.grocerystore.pojo.PropertyValue;
import cn.abin.grocerystore.pojo.Review;
import cn.abin.grocerystore.pojo.User;
import cn.abin.grocerystore.service.CategoryService;
import cn.abin.grocerystore.service.OrderItemService;
import cn.abin.grocerystore.service.OrderService;
import cn.abin.grocerystore.service.ProductImageService;
import cn.abin.grocerystore.service.ProductService;
import cn.abin.grocerystore.service.PropertyValueService;
import cn.abin.grocerystore.service.ReviewService;
import cn.abin.grocerystore.service.UserService;
import cn.abin.grocerystore.util.Result;

@RestController 
public class ForeRESTController {
	@Autowired
    CategoryService categoryService;
    @Autowired
    ProductService productService;
    @Autowired
    UserService userService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    PropertyValueService propertyValueService;
    @Autowired
    ReviewService reviewService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    OrderService orderService ;
    @GetMapping("/forehome")
    public List<Category> home() {
        List<Category> cs= categoryService.list();
        productService.fill(cs);
        productService.fillByRow(cs);
        categoryService.removeCategoryFromProduct(cs);
        return cs;
    }
    
    // 注册
    @PostMapping("/foreregister")
    public Object register(@RequestBody User user,HttpSession session) {
    	String name = user.getName();
    	// 转义，防止在页面显示用户名时，与html语法相同的字符导致页面异常<script>alert('papapa')</script>，这样的用户名
    	name = HtmlUtils.htmlEscape(name);
    	user.setName(name);
    	
    	Boolean result = userService.isExist(name);
    	if(result) {
    		String message ="用户名已经被使用,不能使用";
            return Result.fail(message); 
    	}else {
//    		userService.add(user);
//    		// 注册成功，让其自动登录，注意这里绕过了身份认证，不合理，之后再来改。201906142010
//    		session.setAttribute("user", user);
//    		return Result.success();
    		
    		// 使用shiro，201906161208
    		String salt = new SecureRandomNumberGenerator().nextBytes().toString();
    	    int times = 2;
    	    String algorithmName = "md5";
    	 
    	    String encodedPassword = new SimpleHash(algorithmName, user.getPassword(), salt, times).toString();
    	 
    	    user.setSalt(salt);
    	    user.setPassword(encodedPassword);
    	 
    	    userService.add(user);
    	    return Result.success();
    	}
    }
    
    // 登录
    @PostMapping("/forelogin")
    public Object login(@RequestBody User user,HttpSession session) {
    	// 特殊字符转义，保持与注册时转义到数据库中的一致
    	String name = user.getName();
    	name = HtmlUtils.htmlEscape(name);
    	
//    	User u = userService.get(name, user.getPassword());
//    	if(u==null) {
//    		String msg = "用户名或密码错误！";
//    		return Result.fail(msg);
//    	}
//    	// 使用转义后的用户名的user
//    	session.setAttribute("user", u);
//    	return Result.success();
    	
    	// 使用shiro
    	Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(name, user.getPassword());
        try {
            subject.login(token);
         // 验证成功
         // 使用转义后的用户名的user
            User u = userService.getByName(name);
//          subject.getSession().setAttribute("user", u);
            // 验证成功直接存到session
            session.setAttribute("user", u);
            return Result.success();
        } catch (AuthenticationException e) {
        	// 验证失败
            String message ="账号密码错误";
            return Result.fail(message);
        }
    }
    
    // 商品详情页
    @GetMapping("/foreproduct/{pid}")
    public Object product(@PathVariable(name = "pid") int pid) {
    	// 得到商品，同时已有首展图
    	Product product = productService.get(pid);
    	// 设置单张图和详情图
    	List<ProductImage> productSingleImages = productImageService.listSingleProductImages(product);
        List<ProductImage> productDetailImages = productImageService.listDetailProductImages(product);
        product.setProductSingleImages(productSingleImages);
        product.setProductDetailImages(productDetailImages);
        // 获取属性值
        List<PropertyValue> pvs = propertyValueService.list(product);
        // 获取评价，这里评价实体有user，product,product又有一系列的数据，会不会无限循环？201906142220
        List<Review> reviews = reviewService.list(product);
        // 设置销量和评价数
        productService.setSaleAndReviewNumber(product);
        // 首展图已经绑定
        
        // 设置为map型数据，便于前端交互
        Map<String,Object> map= new HashMap<>();
        map.put("product", product);
        map.put("pvs", pvs);
        map.put("reviews", reviews);
        
        /**前端vue
         *  vue.p=result.data.product;
            vue.pvs=result.data.pvs;
            vue.reviews=result.data.reviews;
         */
        return Result.success(map);
    }
    
    // 模态登录ajax交互判断登录状态
    @GetMapping("forecheckLogin")
    public Object checkLogin( HttpSession session) {
//        User user =(User)  session.getAttribute("user");
//        if(null!=user)
//            return Result.success();
//        return Result.fail("未登录");
    	
    	// 使用shiro
    	Subject subject = SecurityUtils.getSubject();
        if(subject.isAuthenticated())
            return Result.success();
        else
           return Result.fail("未登录");
    	
    }
    // 模态登录ajax交互验证，直接调用前面的登录方法即可
    
    // 根据分类展示商品页面
    @GetMapping("forecategory/{cid}")
    public Object category(@PathVariable int cid,String sort) {
        Category c = categoryService.get(cid);
        productService.fill(c);
        productService.setSaleAndReviewNumber(c.getProducts());
        categoryService.removeCategoryFromProduct(c);
        
        List<Product> list = c.getProducts();
        // switch中不能为空，先进行为空筛选
        if(null!=sort){
            switch(sort){
                case "review":
                    Collections.sort(list,new Comparator<Product>() {
    					public int compare(Product p1, Product p2) {
    						// 取反值，小的放后面
    						return p2.getReviewCount()-p1.getReviewCount();
    					}
    	            });
                    break;
                case "date" :
                    Collections.sort(list,new Comparator<Product>() {
    					public int compare(Product p1, Product p2) {
    						// 取反值，新品放前面
    						return p2.getCreateDate().compareTo(p1.getCreateDate());
    					}
    	            });
                    break;
     
                case "saleCount" :
                    Collections.sort(list,new Comparator<Product>() {
    					public int compare(Product p1, Product p2) {
    						// 取反值，销量高的放前面
    						return p2.getSaleCount()-p2.getSaleCount();
    					}
    	            });
                    break;
     
                case "price":
                    Collections.sort(list,new Comparator<Product>() {

    					@Override
    					public int compare(Product p1, Product p2) {
    						// 取正常值，价格低的放前面,这里价格是float型，直接转会丢失精度，导致忽略小数部分进行比较
    						// 处理
//    						float result = p1.getPromotePrice()-p2.getPromotePrice();
//    						if(result>0) {
//    							return 1;
//    						}else if(result<0) {
//    							return -1;
//    						}
//    						return 0;
    						// 或者仿照Date比较源码，使用三目运算
    						float r1=p1.getPromotePrice();
    						float r2 = p2.getPromotePrice();
    						return (r1<r2?-1:(r1==r2?0:1));
    					}
    	            });
                    break;
     
                case "all":
                    Collections.sort(list,new Comparator<Product>() {
    					public int compare(Product p1, Product p2) {
    						// 取反值，销量高的放前面
    						return p2.getReviewCount()*p2.getSaleCount()-p1.getReviewCount()*p1.getSaleCount();
    					}
    	            });
                    break;
            }
        }
     
        return c;
    }
 // 搜索商品
    @PostMapping("/foresearch")
    public Object search( String keyword){	// 形参自动匹配
        if(null==keyword)
            keyword = "";
        List<Product> ps= productService.search(0,20,keyword);
        // 首展图已经设置好，需要设置销量和评价数
        productService.setSaleAndReviewNumber(ps);
        return ps;
    }
    
    // 立即购买，生成订单项，对应的order为空，属于临时订单项，即购物车
    @GetMapping("forebuyone")
    public Object buyone(int pid, int num, HttpSession session) {
        return buyoneAndAddCart(pid,num,session);
    }

	private Object buyoneAndAddCart(int pid, int num, HttpSession session) {
		// 根据pid的查询商品
		 
		Product product = productService.get(pid);
	    int oiid = 0;
	    
	    // 找到当前登录用户
	    User user =(User)  session.getAttribute("user");
	    // 设置标记
	    boolean found = false;
	    // 查出当前用户的临时订单项集合
	    List<OrderItem> ois = orderItemService.list(user);
	    // 遍历订单项集合，如果查到了，则增加数量，并更新该临时订单项，若无，则直接新增临时订单项
	    for (OrderItem oi : ois) {
	        if(oi.getProduct().getId()==product.getId()){
	            oi.setNumber(oi.getNumber()+num);
	            orderItemService.update(oi);
	            found = true;
	            oiid = oi.getId();
	            break;
	        }
	    }
	 
	    if(!found){
	    	// 创建订单项
	        OrderItem oi = new OrderItem();
	        oi.setUser(user);
	        oi.setProduct(product);
	        oi.setNumber(num);
	        orderItemService.add(oi);
	        oiid = oi.getId();
	    }
	    // 返回订单项id，页面vue继续渲染
	    return oiid;
	}
	
	// 结算页面	，201906151513在产生订单时的疑问在这里解答了，产生订单时，post形式传参到forebuy，oiid的数组，这里将已选择的订单项存放在session，然后生产订单时，从session中取
	@GetMapping("forebuy")
	 public Object buy(String[] oiid,HttpSession session){
	     List<OrderItem> orderItems = new ArrayList<>();
	     float total = 0;
	 
	     for (String strid : oiid) {
	         int id = Integer.parseInt(strid);
	         OrderItem oi= orderItemService.get(id);
	         
	         // 计算购物车总价
	         total +=oi.getProduct().getPromotePrice()*oi.getNumber();
	         orderItems.add(oi);
	     }
	     // 订单项的product的firstImage为非收据库字段，需要手动设置
	     // 这里写个单独的方法，因为后面也有业务需要调用，所以为了体现代码重用性
	     productImageService.setFirstProdutImagesOnOrderItems(orderItems);
	     
	     // 放在session域中，作为待生成订单的订单项集合，便于在之后访问createOrder()方法时直接从session域中取
	     session.setAttribute("ois", orderItems);
	 
	     Map<String,Object> map = new HashMap<>();
	     map.put("orderItems", orderItems);
	     map.put("total", total);
	     return Result.success(map);
	 }
	// 加入购物车
	@GetMapping("foreaddCart")
	public Object addCart(int pid, int num, HttpSession session) {
	    buyoneAndAddCart(pid,num,session);
	    return Result.success();
	}
	// 查看购物车
	@GetMapping("forecart")
	public Object cart(HttpSession session) {
	    User user =(User)  session.getAttribute("user");
	    // 查出未生成订单的订单项集合
	    List<OrderItem> ois = orderItemService.list(user);
	    // 为非数据库字段firstImage
	    productImageService.setFirstProdutImagesOnOrderItems(ois);
	    
	    return ois;
	}
	
	// 订单项更改，ajax交互
	@GetMapping("forechangeOrderItem")
	public Object changeOrderItem( HttpSession session, int pid, int num) {
	    User user =(User)  session.getAttribute("user");
	    if(null==user)
	        return Result.fail("未登录");
	 
	    List<OrderItem> ois = orderItemService.list(user);
	    // 遍历该用户的未生成订单的订单项
	    for (OrderItem oi : ois) {
	    	// 找到当前更改的订单项，条件是商品id相同
	        if(oi.getProduct().getId()==pid){
	            oi.setNumber(num);
	            // 调用服务层
	            orderItemService.update(oi);
	            break;
	        }
	    }
	    return Result.success();
	}
	
	// 删除订单项
	@GetMapping("foredeleteOrderItem")
	public Object deleteOrderItem(HttpSession session,int oiid){
	    User user =(User)  session.getAttribute("user");
	    // 非空判断，避免session过期，仍然访问，导致出错
	    if(null==user)
	        return Result.fail("未登录");
	    orderItemService.delete(oiid);
	    return Result.success();
	}
	// 产生订单，这里从页面传参oiid的数组形式，直接转为JSON对象order接收
	@PostMapping("forecreateOrder")
	public Object createOrder(@RequestBody Order order,HttpSession session){
	    User user =(User)  session.getAttribute("user");
	 // 非空判断，避免session过期，仍然访问，导致出错
	    if(null==user)
	        return Result.fail("未登录");
	    String orderCode = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + RandomUtils.nextInt(10000);
	    order.setOrderCode(orderCode);
	    order.setCreateDate(new Date());
	    order.setUser(user);
	    order.setStatus(OrderService.waitPay);
	    
	    // 拿到session域中的所有订单项，即购物车，这里有问题，如果用户要选择一部分先付款呢？
	    // 201906151505 在页面cartPage.html 的js部分有处理，转为实际选择的oiid数组传参到forebuy，调用上面的buy()方法，将实际选择的订单项存放在session
	    List<OrderItem> ois= (List<OrderItem>)  session.getAttribute("ois");
	 
	    float total =orderService.add(order,ois);
	 
	    Map<String,Object> map = new HashMap<>();
	    map.put("oid", order.getId());
	    map.put("total", total);
	 
	    return Result.success(map);
	}
	// 支付页面 支付响应
	@GetMapping("forepayed")
	public Object payed(int oid) {
	    Order order = orderService.get(oid);
	    order.setStatus(OrderService.waitDelivery);
	    order.setPayDate(new Date());
	    orderService.update(order);
	    return order;
	}
	
	// 用户订单页面
	@GetMapping("forebought")
	public Object bought(HttpSession session) {
	    User user =(User)  session.getAttribute("user");
	    if(null==user)
	        return Result.fail("未登录");
	    List<Order> os= orderService.listByUserWithoutDelete(user);
	    // 清空orderItem下的order，避免无限循环
	    orderItemService.removeOrder(os);
	    return os;
	}
	
	// 从用户订单页面跳转到结算页面，vue渲染，
	// 从产生订单方法提交订单后的结算页面点击结算，vue加载后也要跳转到此
	@GetMapping("foreconfirmPay")
    public Object confirmPay(int oid) {
        Order o = orderService.get(oid);
        // 填充订单项
        orderItemService.fill(o);
        orderItemService.removeOrder(o);
        return o;
    }
	// 订单结算页面确认收货
	@GetMapping("foreorderConfirmed")
	public Object orderConfirmed( int oid) {
	    Order o = orderService.get(oid);
	    o.setStatus(OrderService.waitReview);
	    o.setConfirmDate(new Date());
	    orderService.update(o);
	    return Result.success();
	}
	
	// 在用户订单页面删除订单
	@PutMapping("foredeleteOrder")
	public Object deleteOrder(int oid){
	    Order o = orderService.get(oid);
	    o.setStatus(OrderService.delete);
	    orderService.update(o);
	    return Result.success();
	}
	
	// 商品评价
	@GetMapping("forereview")
	public Object review(int oid) {
	    Order o = orderService.get(oid);
	    orderItemService.fill(o);
	    orderItemService.removeOrder(o);
	    Product p = o.getOrderItems().get(0).getProduct();
	    List<Review> reviews = reviewService.list(p);
	    productService.setSaleAndReviewNumber(p);
	    Map<String,Object> map = new HashMap<>();
	    map.put("p", p);
	    map.put("o", o);
	    map.put("reviews", reviews);
	 
	    return Result.success(map);
	}
	
	// 提交评价
	// 有bug，地址栏更改oid 可以随意评价，不需要购买商品 201906151540
	@PostMapping("foredoreview")
	public Object doreview( HttpSession session,int oid,int pid,String content) {
	    Order o = orderService.get(oid);
	    o.setStatus(OrderService.finish);
	    // 这里用事务处理比较科学，和后面的评价提交一起
//	    orderService.update(o);
	 
	    Product p = productService.get(pid);
	    content = HtmlUtils.htmlEscape(content);
	 
	    User user =(User)  session.getAttribute("user");
	    Review review = new Review();
	    review.setContent(content);
	    review.setProduct(p);
	    review.setCreateDate(new Date());
	    review.setUser(user);
	    
	    // 在服务层中开启事务，操作两张表
	    orderService.updateOrderAndAddReview(o,review);
	    
	    return Result.success();
	}
}
