package com.rdev.rrepodocs.domain.model

enum class ShareExpiryOption(
    val label: String,
    val days: Int?,
) {
    SevenDays("7 days", 7),
    ThirtyDays("30 days", 30),
    NinetyDays("90 days", 90),
    Never("Never", null),
}
