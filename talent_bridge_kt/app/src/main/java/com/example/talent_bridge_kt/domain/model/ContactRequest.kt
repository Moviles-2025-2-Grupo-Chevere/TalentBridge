package com.example.talent_bridge_kt.domain.model

data class ContactRequest(
    val id: String,
    val fromUid: String,
    val fromName: String?,
    val fromEmail: String?,
    val toUid: String,
    val toName: String?,
    val toEmail: String?,
    val reviewed: Boolean,
    val contactRequestTime: Long,
    val reviewTime: Long?
)


