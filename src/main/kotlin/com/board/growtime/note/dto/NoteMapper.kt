package com.board.growtime.note.dto

import com.board.growtime.note.Note

fun Note.toNoteInfo(): NoteInfo {
    return NoteInfo(
        id = this.id,
        title = this.title,
        content = this.content,
        developType = this.developType,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun List<Note>.toNoteInfoList(): List<NoteInfo> {
    return this.map { it.toNoteInfo() }
}
