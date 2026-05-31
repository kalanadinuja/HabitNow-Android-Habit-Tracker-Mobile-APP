
package com.example.madlab3.models

//Stores registered user details for login and profile management.
data class User(
    val email: String,
    val username: String,
    val password: String,
    val fullName: String,
    val createdAt: Long? = null
)