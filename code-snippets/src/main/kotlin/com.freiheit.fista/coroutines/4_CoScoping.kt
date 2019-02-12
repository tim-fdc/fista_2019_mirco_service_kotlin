package com.freiheit.fista.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

data class ArticleId(val id: String)
data class Article(val id: ArticleId)
data class Price(val netPrice: Double)

val art = Article(ArticleId("1"))
val pr = Price(1.0)

/**
 * Coroutine builders [launch] and [async] can only be called within a [CoroutineScope].
 * It is possible to switch into the scope by calling [coroutineScope].
 */
suspend fun writeArticle(article: Article) = coroutineScope {
    launch {
        delay(200)
        println("writing article ${article.id}")
    }
    //NOTICE: This linewill be immediately executed. If you want to wait for the launch
    //to finish, use .join()
    println("writing article scheduled")
}

/**
 * It is also possible to pass the responsibility to the callee by
 * defining an extension on [CoroutineScope].
 */
fun CoroutineScope.writePriceAsync(price: Price): Job {
    return launch {
        delay(500)
        println("writing price ${price.netPrice}")
    }
}

/**
 * Furthermore, it is possible to implement the interface [CoroutineScope].
 */
object WriteBatchWorker : CoroutineScope {
    override val coroutineContext: CoroutineContext = Job()

    fun work(prices: List<Price>) {
        prices.map {
            launch {
                println(it.netPrice)
            }
        }
    }
}


suspend fun main() {
    writeArticle(art)

    runBlocking {
        writePriceAsync(pr).join()
    }

    WriteBatchWorker.work(listOf(pr, pr, pr))
}