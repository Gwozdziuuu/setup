package com.mrngwozdz.setup.integration.helloworld;

import com.mrngwozdz.setup.AbstractIntegrationTest;
import com.mrngwozdz.setup.controller.model.response.HelloWorldResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static com.mrngwozdz.setup.controller.HelloWorldControllerUtils.getHelloWorld;
import static org.assertj.core.api.Assertions.assertThat;

public class GetHelloWorldTest extends AbstractIntegrationTest {

    @Test
    void shouldReturnHelloWorld() {
        var response = getHelloWorld().statusCode(HttpStatus.OK.value()).extract().as(HelloWorldResponse.class);
        assertThat(response.message()).isEqualTo("Hello World!");
    }

}
