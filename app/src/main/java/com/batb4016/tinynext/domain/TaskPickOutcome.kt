package com.batb4016.tinynext.domain

enum class TaskPickOutcome(val storageValue: String) {
    PICKED("PICKED"),
    DONE("DONE"),
    SKIPPED("SKIPPED"),
    SNOOZED("SNOOZED"),
    ANOTHER("ANOTHER"),
}
