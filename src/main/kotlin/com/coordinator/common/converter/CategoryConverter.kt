package com.coordinator.common.converter

import com.coordinator.product.domain.Category
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class CategoryConverter : Converter<String, Category> {
    override fun convert(source: String) = Category.entries.find { it.korean == source }
        ?: throw IllegalArgumentException("존재하지 않는 카테고리: $source")
}
