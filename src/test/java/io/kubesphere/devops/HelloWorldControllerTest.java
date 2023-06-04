package io.kubesphere.devops;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HelloWorldControllerTest {

    @Test
    public void testSayHello() {
        assertEquals("[v1.0] SpringBoot Deployed on Kubernetes with Jenkins DevOps.", new HelloWorldController().sayHello());
    }
}
