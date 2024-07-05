package demoapplication.controller;

import com.flex.annotations.Component;
import com.flex.annotations.Autowired;
import com.flex.annotations.Qualifier;
import demoapplication.service.HelloWorldService;

@Component
public class HelloWorldControllerImpl implements HelloWorldController {

    private HelloWorldService helloWorldService;

    public HelloWorldControllerImpl() {
    }

    @Autowired
    public HelloWorldControllerImpl(@Qualifier(name = "implementation2") HelloWorldService helloWorldService) {
        this.helloWorldService = helloWorldService;
    }

    public void execute() {
        helloWorldService.printHelloWorld();
    }
}
