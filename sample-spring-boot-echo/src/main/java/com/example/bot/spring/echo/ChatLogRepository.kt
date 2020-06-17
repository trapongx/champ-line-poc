package com.example.bot.spring.echo

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface ChatLogRepository : MongoRepository<ChatLog, ObjectId>
