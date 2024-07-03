package demoapplication.service;

import com.flex.annotations.Component;

@Component
public class HelloWorldServiceImpl implements HelloWorldService {
    @Override
    public void printHelloWorld() {
        System.out.println("Hello world from HelloWorldService");
    }
}
