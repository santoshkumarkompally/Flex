package demoapplication.service;

import com.flex.annotations.Component;

@Component(name = "implementation1")
public class HelloWorldServiceImpl implements HelloWorldService {
    @Override
    public void printHelloWorld() {
        System.out.println("Hello world from HelloWorldServiceImpl implementation");
    }
}
