package sample

import platform.Foundation.*
import platform.darwin.*
import platform.posix.sleep

fun main(args: Array<String>) {
    println(WebGateway.get("https://www.google.com/"))
}

object WebGateway {
    fun get(urlString: String): String {
        val semaphore = dispatch_semaphore_create(0)

        val components = NSURLComponents(urlString)
        //compnents?.queryItems = queryItems
        val url = components.URL
        var result: String = ""
        val config = NSURLSessionConfiguration.defaultSessionConfiguration()
        config.waitsForConnectivity = true
        config.timeoutIntervalForResource = 300.0

        val request = NSURLRequest.requestWithURL(url!!)
        val session = NSURLSession.sessionWithConfiguration(
            config, null, NSOperationQueue.mainQueue()
        )

        //val session = NSURLSession.sharedSession
        println(urlString)
        val task =
            session.dataTaskWithRequest(request) { nsData: NSData?, nsurlResponse: NSURLResponse?, nsError: NSError? ->
                nsData?.run { result = toString() }
                println(nsData)
                dispatch_semaphore_signal(semaphore);
            }

        task.resume()
        session.finishTasksAndInvalidate()
        NSOperationQueue.mainQueue().waitUntilAllOperationsAreFinished()
        dispatch_semaphore_wait(semaphore, DISPATCH_TIME_FOREVER)
        //session.finishTasksAndInvalidate()
        return result
    }

    fun post(url: String): String {
        TODO("not implemented")
    }
}