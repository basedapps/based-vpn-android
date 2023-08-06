package co.uk.basedapps.domain.extension

import java.util.*

fun String?.isNotNullOrEmpty() = !this.isNullOrEmpty()

fun String.Companion.empty() = ""
