package com.example.notenest.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes") data class Note( @PrimaryKey
                                              val id: String,
                                              val title: String,
                                              val content: String,
                                              val timestamp: Long )