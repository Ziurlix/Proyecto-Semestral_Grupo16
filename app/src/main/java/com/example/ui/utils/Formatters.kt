package com.example.ui.utils

fun formatClp(amount: Int): String =
    "$" + amount.toString().reversed().chunked(3).joinToString(".").reversed() + " CLP"
