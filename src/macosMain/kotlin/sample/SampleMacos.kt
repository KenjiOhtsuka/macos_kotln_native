package sample

import platform.Foundation.*
import platform.darwin.*
import platform.posix.sleep
import kotlinx.cinterop.autoreleasepool
import platform.AppKit.*
import platform.Foundation.*
import platform.objc.*
import platform.osx.*
import kotlin.native.concurrent.MutableData
import kotlin.native.concurrent.freeze


fun main(args: Array<String>) {
    autoreleasepool {
        val app = NSApplication.sharedApplication()
        app.delegate = MyAppDelegate()
        app.setActivationPolicy(NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular)
        app.activateIgnoringOtherApps(true)
        app.run()
    }
}

object WebGateway {
    fun get(urlString: String): String {
        return HttpDelegate.get(urlString)
    }

    object HttpDelegate: NSObject(), NSURLSessionDataDelegateProtocol {
        private val queue = NSOperationQueue.mainQueue()
        private val receivedData = MutableData()

        init {
            freeze()
        }

        fun fetchUrl(url: String) {
            receivedData.reset()
            val session = NSURLSession.sessionWithConfiguration(
                NSURLSessionConfiguration.defaultSessionConfiguration(),
                this,
                delegateQueue = queue
            )
            session.dataTaskWithURL(NSURL(string = url)).resume()
        }

        fun get(urlString: String): String {
            receivedData.reset()
            val session = NSURLSession.sessionWithConfiguration(
                NSURLSessionConfiguration.defaultSessionConfiguration(),
                this,
                delegateQueue = NSOperationQueue.mainQueue()
            )
            val request = NSURLRequest.requestWithURL(NSURL(string = urlString))
            var resultString = ""
            session.dataTaskWithRequest(request) { nsData: NSData?, nsurlResponse: NSURLResponse?, nsError: NSError? ->
                resultString = nsurlResponse.toString()
            } .resume()
            queue.waitUntilAllOperationsAreFinished()
            return resultString
        }
    }
}

private class MyAppDelegate() : NSObject(), NSApplicationDelegateProtocol {
    init {
        println(WebGateway.get("https://www.google.com/"))
    }
}