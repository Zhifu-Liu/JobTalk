package com.zhifu.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {

	@PostConstruct
	public void init(){
		//解决netty启动冲突问题
		//see Netty4Utils.setAvailableProcessors()
		System.setProperty("es.set.netty.runtime.available.processors", "false");

		/*如果项目里面同时使用到了redis 和 elasticsearch ，就会出现
		* nested exception is java.lang.IllegalStateException: availableProcessors is already set to [4], rejecting [4]1。
		* 主要的原因就是redis 也使用到了Netty，这影响在实例化传输客户端之前初始化处理器的数量。
		* 实例化传输客户端时，我们尝试初始化处理器的数量。
		* 由于在其他地方使用Netty，因此已经初始化并且Netty会对此进行防范，因此首次实例化会因看到的非法状态异常而失败。
		 */
	}

	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

}
