import java.net.URL
import java.net.HttpURLConnection

fun main() {
    val connection = URL("https://www.youtube.com/?gl=US&hl=en").openConnection() as HttpURLConnection
    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
    val html = connection.inputStream.bufferedReader().readText()
    
    val apiKey = Regex(""""(?:INNERTUBE_API_KEY|apiKey)":"(.+?)"""").find(html)?.groupValues?.get(1)
    val clientVersion = Regex(""""clientVersion":"([\d.]+)"""").find(html)?.groupValues?.get(1)
    val visitorData = Regex(""""visitorData":"(.+?)"""").find(html)?.groupValues?.get(1)
    
    println("ApiKey: $apiKey")
    println("ClientVersion: $clientVersion")
    println("VisitorData: $visitorData")
}
