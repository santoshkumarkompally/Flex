package demoapplication;

import com.flex.ApplicationContext;
import demoapplication.controller.HelloWorldController;

public class TestHelloWorldApplication {

    public static void main(String[] args) {
        ApplicationContext applicationContext = new ApplicationContext(TestHelloWorldApplication.class);
        HelloWorldController controller = applicationContext.getBean(HelloWorldController.class);
        controller.execute();
    }
}
