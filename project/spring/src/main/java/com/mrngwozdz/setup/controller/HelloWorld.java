package com.mrngwozdz.setup.controller;

import com.mrngwozdz.setup.controller.api.HelloWorldApi;
import com.mrngwozdz.setup.controller.model.response.HelloWorldResponse;
import com.mrngwozdz.setup.platform.http.RestResults;
import com.mrngwozdz.setup.service.helloworld.business.HelloWorldBusiness;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/")
public class HelloWorld implements HelloWorldApi {

    private final HelloWorldBusiness business;

    @Override
    public ResponseEntity<HelloWorldResponse> sayHello() {
        return RestResults.toResponseEntity(business.sayHello(), HelloWorldResponse::new);
    }

}
