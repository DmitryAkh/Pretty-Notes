package com.dakh.prettynotes.data.db

import com.dakh.prettynotes.data.models.ContentItemDbModel
import com.dakh.prettynotes.data.models.ContentType
import com.dakh.prettynotes.data.models.NoteDBModel
import com.dakh.prettynotes.data.models.NoteWithContentDbModel
import com.dakh.prettynotes.domain.entity.ContentItem
import com.dakh.prettynotes.domain.entity.Note

fun Note.toDBModel(): NoteDBModel {
    return NoteDBModel(
        id = id,
        title = title,
        updatedAt = updatedAt,
        isPinned = isPinned
    )
}

fun List<ContentItem>.toContentItemDbModels(noteId: Int): List<ContentItemDbModel> {
    return mapIndexed { index, contentItem ->
        when (contentItem) {
            is ContentItem.Image -> {
                ContentItemDbModel(
                    noteId = noteId,
                    contentType = ContentType.IMAGE,
                    content = contentItem.url,
                    order = index
                )
            }

            is ContentItem.Text -> {
                ContentItemDbModel(
                    noteId = noteId,
                    contentType = ContentType.TEXT,
                    content = contentItem.content,
                    order = index
                )
            }
        }
    }
}

fun List<ContentItemDbModel>.toContentItems(): List<ContentItem> {
    return map { contentItemDbModel ->
        when (contentItemDbModel.contentType) {
            ContentType.TEXT -> {
                ContentItem.Text(content = contentItemDbModel.content)
            }

            ContentType.IMAGE -> {
                ContentItem.Image(url = contentItemDbModel.content)
            }
        }
    }
}


fun NoteWithContentDbModel.toNote(): Note {
    return Note(
        id = noteDBModel.id,
        title = noteDBModel.title,
        content = content.toContentItems(),
        updatedAt = noteDBModel.updatedAt,
        isPinned = noteDBModel.isPinned
    )
}

fun List<NoteWithContentDbModel>.toListNote(): List<Note> {
    return map {
        it.toNote()
    }
}