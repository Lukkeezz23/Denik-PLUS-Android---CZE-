// FILE: data/DetailModels.kt
package com.example.denikplus.data

import androidx.compose.runtime.Immutable

@Immutable
data class DetailCategory(
    val id: String,
    val title: String,
    val items: List<DetailItem>
)

@Immutable
data class DetailItem(
    val id: String,
    val title: String
)

@Immutable
data class DetailSelection(
    val categoryId: String,
    val itemId: String,
    val itemTitle: String,
    val note: String = ""
)
