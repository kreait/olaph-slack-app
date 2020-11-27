package com.kreait.bots.agile.domain.v2.repository

import com.kreait.bots.agile.domain.v2.data.Questionnaire
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository

/**
 * Custom repository for the questionnaire answer subtype
 */
@Repository
class QuestionnaireAnswerRepository @Autowired constructor(private val template: MongoTemplate) {

    companion object {
        const val ITEM_ID = "${Questionnaire.ITEMS}.${Questionnaire.Item.ID}"
        /**
         * This key selects the item in the list of items that was found in criteria
         * *Example*
         * Say you have the following items and you want to update only item two:
         * [{id: 1, ...}, {id: 2, ...}, {id: 3, ...}]
         * build criteria that finds item two and then in the update object use [ITEM_ANSWER_VALUE] and it will
         * automatically address item two as well
         */
        const val ITEM_ANSWER_VALUE = "${Questionnaire.ITEMS}.$.${Questionnaire.Item.ANSWER}.${Questionnaire.Item.Answer.VALUE}"
    }

    fun saveAnswer(itemId: String, answerText: String): Questionnaire {
        val criteria = Criteria.where(ITEM_ID).`is`(itemId)

        val update = Update().set(ITEM_ANSWER_VALUE, answerText)

        return template.findAndModify(Query.query(criteria), update, FindAndModifyOptions.options().returnNew(true), Any::class.java, Questionnaire.COLLECTION_NAME)
                as Questionnaire
    }

}


