package com.example.composepod.demo.data.repository

import com.example.composepod.demo.data.local.NoteDao
import com.example.composepod.demo.data.local.NoteEntity
import com.example.composepod.demo.domain.repository.NoteRepository
import com.example.composepod.demo.models.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepositoryImpl(
    private val dao: NoteDao
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> {
        return dao.getAllNotes().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getNoteById(id: String): Note? {
        return dao.getNoteById(id)?.toDomainModel()
    }

    override suspend fun insertNote(note: Note) {
        dao.insert(note.toEntity())
    }

    override suspend fun updateNote(note: Note) {
        dao.update(note.toEntity())
    }

    override suspend fun deleteNote(id: String) {
        dao.deleteById(id)
    }

    // Mappers
    private fun NoteEntity.toDomainModel(): Note {
        return Note(
            id = id,
            title = title,
            content = content
        )
    }

    private fun Note.toEntity(): NoteEntity {
        return NoteEntity(
            id = id,
            title = title,
            content = content
        )
    }
}
