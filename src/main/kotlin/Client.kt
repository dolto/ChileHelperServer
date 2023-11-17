import java.io.*
import java.nio.file.Paths
import javax.net.ssl.SSLSocketFactory

fun main() {

    //인증서 신뢰 구성
    val keystorePath = Paths.get("keycode.p12").toAbsolutePath().toString()
    val keystorePw = "chilehelper" //패스워드는 나중에 db에서 불러올 예정
    System.setProperty("javax.net.ssl.trustStore", keystorePath)
    System.setProperty("javax.net.ssl.trustStorePassword", keystorePw)

    val serverHost = "localhost"
    val serverPort = 55550

    val sslSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
    val clientSocket = sslSocketFactory.createSocket(serverHost, serverPort)
    val input = DataInputStream(clientSocket.getInputStream())
    val output = DataOutputStream(clientSocket.getOutputStream())

    // 클라이언트에서 서버에 메시지 전송
    val message = "Hello, server! 안녕!!"
    output.writeUTF(message)

    println("데이터 전송 완료")
    
    // 서버로부터 응답 받기
    val response = getSocketRead(input)
    println("Server response: $response")

    println("응답 받기 완료")
    input.close()
    output.close()
    clientSocket.close()
}

