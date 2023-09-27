package org.jeecg;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.utils.ComputerInfo;
import org.jeecg.utils.SMS4;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

import org.springframework.util.ClassUtils;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@EnableSwagger2
@EnableScheduling
@SpringBootApplication(scanBasePackages = {"org.jeecg","com.ai"})
@EnableAsync
@EnableEncryptableProperties
public class JeecgApplication {

  public static void main(String[] args) throws UnknownHostException {
    ConfigurableApplicationContext application = SpringApplication.run(JeecgApplication.class, args);
    Environment env = application.getEnvironment();

/*
    //判断机器码
    String machineCode= SMS4.myCrypt(ComputerInfo.getMacAddress(), "jkjladfsfaaadfdd!13123", SMS4.ENCRYPT);
    log.info("machineCode："+machineCode);
    String registrationCode=SMS4.myCrypt(machineCode, "8718723qweqwe123ddd", SMS4.ENCRYPT);
    String txtPath = System.getProperty("user.dir")+"/config/logincode.txt";

    String configRegistrationCode = StringUtils.chomp(SMS4.readTxtFile(txtPath));
    if(!registrationCode.equalsIgnoreCase(configRegistrationCode)){
      log.info("机器码错误码："+machineCode);
      System.exit(-1);
    }
*/





    String ip = InetAddress.getLocalHost().getHostAddress();
    String port = env.getProperty("server.port");
    String path = env.getProperty("server.servlet.context-path");
    log.info("\n----------------------------------------------------------\n\t" +
        "Application Jeecg-Boot is running! Access URLs:\n\t" +
        "Local: \t\thttp://localhost:" + port + path + "/\n\t" +
        "External: \thttp://" + ip + ":" + port + path + "/\n\t" +
        "swagger-ui: \thttp://" + ip + ":" + port + path + "/swagger-ui.html\n\t" +
        "Doc: \t\thttp://" + ip + ":" + port + path + "/doc.html\n" +
        "----------------------------------------------------------");

  }

  @Value("${jeecg.path.upload}")
  private String UPLOAD_PATH;

  /**
   * 解决文件上传,临时文件夹被程序自动删除问题
   *
   * 文件上传时自定义临时路径
   * @return
   */
  @Bean
  MultipartConfigElement multipartConfigElement() {
    MultipartConfigFactory factory = new MultipartConfigFactory();
    //2.该处就是指定的路径(需要提前创建好目录，否则上传时会抛出异常)
    File uploadtmpFile = new File(UPLOAD_PATH + "_tmp");
    if(!uploadtmpFile.exists()){
      uploadtmpFile.mkdir();
    }
    factory.setLocation(uploadtmpFile.getAbsolutePath());
    return factory.createMultipartConfig();
  }
}
