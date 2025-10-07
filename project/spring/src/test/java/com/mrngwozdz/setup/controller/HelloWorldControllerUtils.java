package com.mrngwozdz.setup.controller;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;

public class HelloWorldControllerUtils {

    public static ValidatableResponse getHelloWorld() {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .get("/hello")
                .then();
    }

}
