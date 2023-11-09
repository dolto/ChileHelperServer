import cn.zenliu.utils.lzstring4k.LZ4K
import com.google.gson.Gson
import com.machinezoo.sourceafis.FingerprintImage
import com.machinezoo.sourceafis.FingerprintMatcher
import com.machinezoo.sourceafis.FingerprintTemplate
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Paths
import java.security.KeyStore
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import javax.net.ssl.*
import kotlin.concurrent.thread

const val fingerWidth = 596
const val fingerHeight = 596
fun main() {
    println("Hello World!")
    //println(Paths.get("keycode.jks").toAbsolutePath().toString());
    val gson = Gson() //직렬화를 위한 선언

//    val testProfile = Profile(
    //    0,
//        NameData(0,"홍길동","대충 이미지1"),
//        FingerData(0, "asdf","dsgae","12g3qa","1t1geas"),
//        mutableListOf(AdressData("전주시 완산구 안행똥")),
//        mutableListOf(PhoneNumberData("01011112224")),
//        mutableListOf(MemoData("잘 키워주세요~"))
//    )
//    println(testProfile.toString())
//    val jsonTestProfile = gson.toJson(testProfile)
//    println(jsonTestProfile)
//
//    val testProfileClone = gson.fromJson(jsonTestProfile, Profile::class.java)
//    println(testProfileClone.toString())
    // 직렬화와 역직렬화 테스트 성공

    println("Processing Server Loading...")

    val database = connectToDatabase() ?: return; //mySQL과 연결
    try {
        // SSLServerSocketFactory를 사용하여 SSLServerSocket을 생성

        ///인증서 연결부분
        val keystorePath = Paths.get("keycode.jks").toAbsolutePath().toString()
        val keystorePw = "chilehelper" //패스워드는 나중에 db에서 불러올 예정

        val keystore = KeyStore.getInstance("JKS")
        val keystoreFile = File(keystorePath)
        keystore.load(keystoreFile.inputStream(), keystorePw.toCharArray())

        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        keyManagerFactory.init(keystore, keystorePw.toCharArray())
        val keyManagers = keyManagerFactory.keyManagers

        val sslContext = javax.net.ssl.SSLContext.getInstance("TLS")
        sslContext.init(keyManagerFactory.keyManagers, null, null)
        ///인증서 열기 끝

        val sslServerSocketFactory = sslContext.serverSocketFactory
        val serverSocket = sslServerSocketFactory.createServerSocket(55550) as SSLServerSocket
        val encoder = Base64.getEncoder()
        val decoder = Base64.getDecoder()
        val sslParams = SSLParameters()
        sslParams.protocols = arrayOf("TLSv1.2")
        sslParams.cipherSuites = arrayOf("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256")
        serverSocket.sslParameters = sslParams

        val socketList = CopyOnWriteArrayList<SocketAndIO>()
        while (true){
            println("Processing Server Wait...")
            val socket = serverSocket.accept() as SSLSocket
            val socketAndIo = SocketAndIO(socket, DataInputStream(socket.inputStream), DataOutputStream(socket.outputStream))
            socketList.add(socketAndIo)

            val serverAcceptData = thread(true){
                println("Server Accept Now Wait for Data")
                when(val dataString = getSocketRead(socketAndIo.input)){
                    "Input_Profile" -> {
                        socketAndIo.output.writeUTF("result_ok")
                        val profile64 = getSocketRead(socketAndIo.input).toByteArray(charset("UTF-8"))
                        // 암호화 해제

                        val profile = gson.fromJson(encoder.encodeToString(profile64), Profile::class.java)

                        val fingers = getFingerData(database)
                        val proFinger1 = FingerprintTemplate(base64ToFingerprintImage(profile.fingerDB.finger1))
                        val proFinger2 = FingerprintTemplate(base64ToFingerprintImage(profile.fingerDB.finger2))
                        val proFinger3 = FingerprintTemplate(base64ToFingerprintImage(profile.fingerDB.finger3))
                        val proFinger4 = FingerprintTemplate(base64ToFingerprintImage(profile.fingerDB.finger4))

                        val result = fingerMatch(proFinger1,proFinger2,proFinger3,proFinger4,fingers)
                        if (result.first){
                            profile.id = result.second
                            updateProfile(database, profile)

                            socketAndIo.output.writeBoolean(true)
                        }else{
                            val tempid = getIndexID(database) + 1
                            profile.id = tempid
                            insertProfile(database, profile)

                            socketAndIo.output.writeBoolean(false)
                        }
                    }
                    "Try_Login" -> {
                        socketAndIo.output.writeUTF("result_ok")
                        val finger64 = getSocketRead(socketAndIo.input).toByteArray(charset("UTF-8"))
                        //암호화 해제
                        val finger = gson.fromJson(encoder.encodeToString(finger64), FingerData::class.java)

                        val fingers = getFingerData(database)
                        val proFinger1 = FingerprintTemplate(base64ToFingerprintImage(finger.finger1))
                        val proFinger2 = FingerprintTemplate(base64ToFingerprintImage(finger.finger2))
                        val proFinger3 = FingerprintTemplate(base64ToFingerprintImage(finger.finger3))
                        val proFinger4 = FingerprintTemplate(base64ToFingerprintImage(finger.finger4))

                        val result = fingerMatch(proFinger1,proFinger2,proFinger3,proFinger4,fingers)

                        if (result.first){
                            val result = getProfile(database, result.second)
                            val netMessage = decoder.decode(gson.toJson(result)).toString(Charset.forName("UTF-8"))
                            socketAndIo.output.writeUTF(netMessage)
                        }
                        else{
                            socketAndIo.output.writeUTF("로그인 실패!")
                        }
                    }
                    else -> {
                        println(dataString)
                        println("${dataString}에코 수행")
                        socketAndIo.output.writeUTF(dataString)
                    }
                }
                println("서버에서 처리를 완료함 (비연결성 지향을 위해)")
                socketAndIo.input.close()
                socketAndIo.output.close()
                socketAndIo.socket.close()
                socketList.remove(socketAndIo)
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

class SocketAndIO(
    val socket: SSLSocket,
    val input: DataInputStream,
    val output: DataOutputStream
)

fun getSocketRead(input: DataInputStream): String {
    return input.readUTF()
}

fun connectToDatabase(): Connection? {
    val url = "jdbc:mysql://localhost:3306/kotlindb"
    val user = "kotlinuser"
    val password = "password"

    return try {
        DriverManager.getConnection(url, user, password)
    } catch (e: SQLException) {
        null
    }
}

fun LZ4KToFingerprintImage(string: String): FingerprintImage? {
    return try {
        val data = LZ4K.decompressFromBase64(string)?.toByteArray(charset("UTF-8"))
        return FingerprintImage(fingerWidth, fingerHeight, data)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

private fun base64ToFingerprintImage(string: String): FingerprintImage? {
    return try{
        val base64decoder = Base64.getDecoder()
        val data = base64decoder.decode(string)
        return FingerprintImage(fingerWidth, fingerHeight, data)
    }catch (e: Exception){
        e.printStackTrace()
        null
    }
}

fun fingerMatch(
    proFinger1: FingerprintTemplate,
    proFinger2: FingerprintTemplate,
    proFinger3: FingerprintTemplate,
    proFinger4: FingerprintTemplate,
    fingers: MutableList<FingerData>
):Pair<Boolean, Int>{
    for (finger in fingers){
        //이제 여기서 profile.finger와 proFinger를 비교
        val finger1 = FingerprintTemplate(base64ToFingerprintImage(finger.finger1))
        val finger2 = FingerprintTemplate(base64ToFingerprintImage(finger.finger2))
        val finger3 = FingerprintTemplate(base64ToFingerprintImage(finger.finger3))
        val finger4 = FingerprintTemplate(base64ToFingerprintImage(finger.finger4))

        val finger1Matcher = FingerprintMatcher(finger1)
        val finger2Matcher = FingerprintMatcher(finger2)
        val finger3Matcher = FingerprintMatcher(finger3)
        val finger4Matcher = FingerprintMatcher(finger4)

        val similer = (finger1Matcher.match(proFinger1)+
                finger2Matcher.match(proFinger2)+
                finger3Matcher.match(proFinger3)+
                finger4Matcher.match(proFinger4)) / 4

        if (similer > 40){ //4개의 지문의 유사도의 평균이 40 이상이면 같은사람이라고 취급
            println("이미 만들어진 프로필 이라고 판단됩니다.")
            return Pair(true, finger.id)
        }
    }
    return Pair(false, -1)
}
