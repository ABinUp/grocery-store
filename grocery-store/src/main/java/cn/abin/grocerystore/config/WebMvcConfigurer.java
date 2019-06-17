package cn.abin.grocerystore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import cn.abin.grocerystore.interceptor.LoginInterceptor;
import cn.abin.grocerystore.interceptor.OtherInterceptor;
 
@Configuration
class WebMvcConfigurer extends WebMvcConfigurerAdapter{
	@Bean
    public OtherInterceptor getOtherIntercepter() {
        return new OtherInterceptor();
    } 
    @Bean
    public LoginInterceptor getLoginIntercepter() {
        return new LoginInterceptor();
    }
     
    @Override
    public void addInterceptors(InterceptorRegistry registry){
    	// 配置拦截器，这里体现了顺序，类似于SSM中xml配置的先后，201906151259这个坑，
    	registry.addInterceptor(getOtherIntercepter())
        .addPathPatterns("/**"); 
        registry.addInterceptor(getLoginIntercepter())
        .addPathPatterns("/**");
    }
}