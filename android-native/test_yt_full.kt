import java.net.URL
import java.net.HttpURLConnection
import java.io.OutputStreamWriter
import java.util.Scanner

fun main() {
    val domain = "https://www.youtube.com"
    val credentialUrl = "$domain/?gl=US&hl=en"
    val headers = mapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Accept-Language" to "en-US,en;q=0.9",
        "Origin" to "https://www.youtube.com",
        "Referer" to "https://www.youtube.com/"
    )

    println("Fetching credentials...")
    val conn = URL(credentialUrl).openConnection() as HttpURLConnection
    headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
    
    val html = conn.inputStream.bufferedReader().readText()
    
    val apiKey = Regex(""""(?:INNERTUBE_API_KEY|apiKey)":"(.+?)"""").find(html)?.groupValues?.get(1)
    val clientVersion = Regex(""""clientVersion":"([\d.]+)"""").find(html)?.groupValues?.get(1) ?: "2.20240101.01.00"
    val visitorData = Regex(""""visitorData":"(.+?)"""").find(html)?.groupValues?.get(1)
    
    println("ApiKey: $apiKey")
    println("ClientVersion: $clientVersion")
    println("VisitorData: $visitorData")
    
    if (apiKey == null) {
        println("FAILED to find API Key")
        return
    }
    
    val apiUrl = "$domain/youtubei/v1/browse?key=$apiKey"
    val body = """
    {
        "context": {
            "client": {
                "clientName": "WEB",
                "clientVersion": "$clientVersion",
                "hl": "en-US",
                "gl": "US",
                "visitorData": "${visitorData ?: ""}"
            }
        },
        "browseId": "FEwhat_to_watch"
    }
    """.trimIndent()
    
    println("Requesting browse...")
    val postConn = URL(apiUrl).openConnection() as HttpURLConnection
    postConn.requestMethod = "POST"
    postConn.doOutput = true
    headers.forEach { (k, v) -> postConn.setRequestProperty(k, v) }
    postConn.setRequestProperty("Content-Type", "application/json")
    
    OutputStreamWriter(postConn.outputStream).use { it.write(body) }
    
    val responseCode = postConn.responseCode
    println("Response Code: $responseCode")
    
    if (responseCode == 200) {
        val responseText = postConn.inputStream.bufferedReader().readText()
        println("Response Length: \${responseText.length}")
        println("Contains 'contents': \${responseText.contains("contents")}")
        println("Contains 'videoRenderer': \${responseText.contains("videoRenderer")}")
        println("Contains 'richItemRenderer': \${responseText.contains("richItemRenderer")}")
    } else {
        val errorText = postConn.errorStream?.bufferedReader()?.readText() ?: "No error body"
        println("Error Body: $errorText")
    }
}
