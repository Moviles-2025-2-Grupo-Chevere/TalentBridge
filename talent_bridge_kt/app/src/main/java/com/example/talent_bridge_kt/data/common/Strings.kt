package com.example.talent_bridge_kt.data.common

import java.text.Normalizer

fun String.normalizeAsciiLower(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        .lowercase()