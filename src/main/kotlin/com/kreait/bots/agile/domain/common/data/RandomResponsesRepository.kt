package com.kreait.bots.agile.domain.common.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.repository.MongoRepository

/**
 * mongo repository containing random messages
 */

interface RandomResponsesRepository : MongoRepository<RandomResponses, String>, RandomResponsesRepositoryCustom

interface RandomResponsesRepositoryCustom {
    fun update(id: String, update: Update)
}

/**
 * finds a random resonse for "hoot hoot"-messages
 */
class RandomResponsesRepositoryCustomImpl constructor(@Autowired private val template: MongoTemplate) : RandomResponsesRepositoryCustom {

    override fun update(id: String, update: Update) {
        template.updateFirst(
                Query.query(Criteria.where("_id").`is`(id)),
                update,
                RandomResponses::class.java)
    }

}

@Document(collection = "RandomResponses")
data class RandomResponses(@Id val id: String,
                           @Field(RESPONSES) val messages: List<String>,
                           @Field(DESCRIPTION) val description: String = "") {
    companion object {
        const val RESPONSES = "responses"
        const val DESCRIPTION = "description"
    }
}
