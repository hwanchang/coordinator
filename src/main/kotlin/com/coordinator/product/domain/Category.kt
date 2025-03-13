package com.coordinator.product.domain

import com.fasterxml.jackson.annotation.JsonValue

enum class Category(@JsonValue val korean: String) {
    TOP("상의"),

    OUTER("아우터"),

    TROUSERS("바지"),

    SNEAKERS("스니커즈"),

    BAG("가방"),

    HAT("모자"),

    SOCKS("양말"),

    ACCESSORY("액세서리"),
}
