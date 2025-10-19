package com.example.talent_bridge_kt.domain.model

data class ResumeUploadResult(
    val id: String,
    val url: String,
    val storagePath: String,
    val fileName: String,
    val language: ResumeLanguage,
    val bytes: Long
)