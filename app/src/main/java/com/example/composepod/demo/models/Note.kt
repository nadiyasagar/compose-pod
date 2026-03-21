package com.example.composepod.demo.models

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
