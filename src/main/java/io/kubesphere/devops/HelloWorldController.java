package io.kubesphere.devops;


import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 */
@RestController
@EnableAutoConfiguration
public class HelloWorldController {

    @RequestMapping("/")
    public String sayHello() {
        return "[v1.0] SpringBoot Deployed on Kubernetes with Jenkins DevOps.";
    }

    @RequestMapping("/ping")
    public String ping() {
        return "pong";
    }

    @RequestMapping("/hello")
    public String hello() {
        return "Hello World!";
    }
}
