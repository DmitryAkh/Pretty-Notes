package com.dakh.prettynotes.domain.entity

data class Note(
    val id: Int,
    val title: String,
    val content: List<ContentItem>,
    val updatedAt: Long,
    val isPinned: Boolean
)
