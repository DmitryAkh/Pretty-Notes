package com.dakh.prettynotes.data

import com.dakh.prettynotes.domain.ContentItem
import com.dakh.prettynotes.domain.Note
import kotlinx.serialization.json.Json

fun Note.toDBModel(): NoteDBModel {
    val contentAsString = Json.encodeToString( content.toContentItemDbModels())
    return NoteDBModel(
        id = id,
        title = title,
        content = contentAsString,
        updatedAt = updatedAt,
        isPinned = isPinned
    )
}

fun List<ContentItem>.toContentItemDbModels(): List<ContentItemDbModel> {
    return map { contentItem ->
        when(contentItem) {
            is ContentItem.Image -> {
                ContentItemDbModel.Image(contentItem.url)
            }
            is ContentItem.Text -> {
                ContentItemDbModel.Text(contentItem.content)
            }
        }
    }
}

fun List<ContentItemDbModel>.toContentItems(): List<ContentItem> {
    return map { contentItemDbModel ->
        when(contentItemDbModel) {
            is ContentItemDbModel.Image -> {
                ContentItem.Image(contentItemDbModel.url)
            }
            is ContentItemDbModel.Text -> {
                ContentItem.Text(contentItemDbModel.content)
            }
        }
    }
}

fun NoteDBModel.toNote(): Note {
    val contentAsList = Json.decodeFromString<List<ContentItemDbModel>>(content).toContentItems()
    return Note(
        id = id,
        title = title,
        content = contentAsList,
        updatedAt = updatedAt,
        isPinned = isPinned
    )
}

fun List<NoteDBModel>.toListNote(): List<Note> {
   return map {
        it.toNote()
    }
}