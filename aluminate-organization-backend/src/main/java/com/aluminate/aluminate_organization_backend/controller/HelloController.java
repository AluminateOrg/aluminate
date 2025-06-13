package com.aluminate.aluminate_organization_backend.controller;

    import com.aluminate.aluminate_organization_backend.service.HelloService;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;

    /**
     * Controller class for handling API requests related to greetings.
     * This class defines endpoints under the `/api` path.
     */
    @RestController
    @RequestMapping("/api")
    public class HelloController {

        private final HelloService helloService;

        /**
         * Constructor for HelloController.
         *
         * @param helloService the service used to handle greeting logic
         */
        public HelloController(HelloService helloService) {
            this.helloService = helloService;
        }

        /**
         * Endpoint to return a greeting message.
         *
         * @return a greeting message as a String
         */
        @GetMapping("/hello")
        public String sayHello() {
            return helloService.sayHello();
        }
    }