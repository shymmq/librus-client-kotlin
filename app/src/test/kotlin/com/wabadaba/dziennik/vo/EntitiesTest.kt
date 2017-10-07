package com.wabadaba.dziennik.vo

import com.wabadaba.dziennik.BaseDBTest
import com.wabadaba.dziennik.api.Parser
import io.requery.Persistable
import org.amshove.kluent.shouldBeGreaterThan
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * Generic test, checking if all entities are properly parsed, saved in DB and retrieved by fragmentId.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class EntitiesTest(
        private val className: String,
        private val endpoint: String,
        @Suppress("unused")
        val name: String) : BaseDBTest() {

    companion object {
        private fun KClass<out Any>.findEndpoint() = this.findAnnotation<LibrusEntity>()?.endpoint
                ?: throw AssertionError("Class ${this.simpleName} not annotated with LibrusEntity annotation")

        @JvmStatic
        @Suppress("unused")
        @ParameterizedRobolectricTestRunner.Parameters(name = "{2}")
        fun data(): Collection<Array<out Any>> {
            return Models.DEFAULT
                    .types
                    .map { it.baseType.kotlin }
                    .map {
                        arrayOf(it.qualifiedName!!,
                                it.findEndpoint(),
                                it.simpleName!!)
                    }
        }
    }

    @Test
    fun checkEntityClass() {
        val inputClass = this::class.java.classLoader.loadClass(className).kotlin
        @Suppress("UNCHECKED_CAST")
        val clazz = inputClass as KClass<out Persistable>
        val file = readFile("/endpoints/$endpoint.json")
        val parsedList = Parser.parseEntityList(file, inputClass.java)
                .toList().blockingGet()
        parsedList.size shouldBeGreaterThan 0
        parsedList.forEach { original ->
            println(original)
            val inserted = dataStore.upsert(original)
            inserted shouldEqual original
            if (clazz.java.isAssignableFrom(Identifiable::class.java)) {
                val found = dataStore.findByKey(clazz, (original as Identifiable).id)
                found shouldEqual original
                found shouldNotBe original
            }
        }
    }
}