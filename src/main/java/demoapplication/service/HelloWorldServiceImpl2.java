package demoapplication.service;

import com.flex.annotations.Component;

@Component(name = "implementation2")
public class HelloWorldServiceImpl2 implements HelloWorldService{

    @Override
    public void printHelloWorld() {
        System.out.print("Hello world from HelloWorldServiceImpl2 implementation");
    }
}
