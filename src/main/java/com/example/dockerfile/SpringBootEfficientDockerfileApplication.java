package com.example.dockerfile;

import java.util.Optional;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class SpringBootEfficientDockerfileApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringBootEfficientDockerfileApplication.class, args);
  }

  @RestController
  static class HelloController {

    @GetMapping(value = "/hello")
    String sayHello(@RequestParam Optional<String> name) {
      return name.map(visitor -> "Hello " + visitor + "!").orElse("Hello stranger!");
    }
  }
}
