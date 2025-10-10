package com.mrngwozdz.setup.controller;

import com.mrngwozdz.setup.controller.model.request.CreateOrderRequest;
import com.mrngwozdz.setup.controller.model.request.UpdateOrderRequest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;

public class OrderControllerUtils {

    public static ValidatableResponse createOrder(CreateOrderRequest request) {
        return given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/orders")
                .then();
    }

    public static ValidatableResponse createOrder(String jsonRequest) {
        return given()
                .contentType(ContentType.JSON)
                .body(jsonRequest)
                .when()
                .post("/orders")
                .then();
    }

    public static ValidatableResponse getOrderById(String orderId) {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .get("/orders/{orderId}", orderId)
                .then();
    }

    public static ValidatableResponse getAllOrders() {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .get("/orders")
                .then();
    }

    public static ValidatableResponse updateOrder(String orderId, UpdateOrderRequest request) {
        return given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put("/orders/{orderId}", orderId)
                .then();
    }

    public static ValidatableResponse patchOrder(String orderId, UpdateOrderRequest request) {
        return given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .patch("/orders/{orderId}", orderId)
                .then();
    }

    public static ValidatableResponse deleteOrder(String orderId) {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/orders/{orderId}", orderId)
                .then();
    }
}