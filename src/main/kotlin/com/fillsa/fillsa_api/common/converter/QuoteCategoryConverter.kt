package com.fillsa.fillsa_api.common.converter

import com.fillsa.fillsa_api.domain.quote.entity.Quote
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class QuoteCategoryConverter : AttributeConverter<Quote.QuoteCategory, String> {
    // TODO. converter 적용 확인하기
    override fun convertToDatabaseColumn(attribute: Quote.QuoteCategory?): String? {
        return attribute?.value
    }

    override fun convertToEntityAttribute(value: String?): Quote.QuoteCategory? {
        return Quote.QuoteCategory.fromValue(value)
    }
}