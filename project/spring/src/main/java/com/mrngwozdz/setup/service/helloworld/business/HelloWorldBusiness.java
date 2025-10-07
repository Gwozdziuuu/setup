package com.mrngwozdz.setup.service.helloworld.business;

import com.mrngwozdz.setup.platform.result.Failure;
import io.vavr.control.Either;
import org.springframework.stereotype.Service;

@Service
public class HelloWorldBusiness {

    public Either<Failure, String> sayHello() {
        return Either.right("Hello World!");
    }

}
