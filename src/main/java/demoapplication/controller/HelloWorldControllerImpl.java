package demoapplication.controller;

import com.flex.annotations.Component;
import com.flex.models.Autowired;
import demoapplication.service.HelloWorldService;

@Component
public class HelloWorldControllerImpl implements HelloWorldController {

    private HelloWorldService helloWorldService;

    public HelloWorldControllerImpl() {
    }

    @Autowired
    public HelloWorldControllerImpl(HelloWorldService helloWorldService) {
        this.helloWorldService = helloWorldService;
    }

    public void execute() {
        helloWorldService.printHelloWorld();
    }
}
