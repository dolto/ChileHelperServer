import cn.zenliu.utils.lzstring4k.LZ4K
import com.google.gson.Gson
import com.machinezoo.sourceafis.FingerprintImage
import com.machinezoo.sourceafis.FingerprintMatcher
import com.machinezoo.sourceafis.FingerprintTemplate
import java.awt.image.BufferedImage
import java.io.*
import java.lang.Integer.min
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyStore
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import javax.imageio.ImageIO
import javax.net.ssl.*
import kotlin.concurrent.thread

//const val fingerWidth = 100
//const val fingerHeight = 100
fun main() {
    println("Hello World!")
    //println(Paths.get("keycode.jks").toAbsolutePath().toString());
    val gson = Gson() //직렬화를 위한 선언

//    val testProfile = Profile(
//        0,
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
        val keystorePath = Paths.get("keycode.p12").toAbsolutePath().toString()
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

        // 테스트 공간

        var profile_test = "{\"adressDB\":[{\"adress\":\"test2a1\",\"reg_order\":0},{\"adress\":\"test2m2\",\"reg_order\":5}],\"fingerDB\":{\"finger1\":\"\",\"finger2\":\"\",\"finger3\":\"\",\"finger4\":\"\",\"id\":-1},\"id\":-1,\"memoDB\":[{\"memo\":\"test2m1\",\"reg_order\":2},{\"memo\":\"test2a2\",\"reg_order\":3}],\"nameDB\":{\"id\":-1,\"name\":\"test2\",\"profile\":\"Test\"},\"phoneNumberDB\":[{\"phoneNumber\":\"test2p1\",\"reg_order\":1},{\"phoneNumber\":\"test2p2\",\"reg_order\":4}]}"

        // 테스트 공간

        while (true){
            println("Processing Server Wait...")
            val socket = serverSocket.accept() as SSLSocket
            val socketAndIo = SocketAndIO(socket, DataInputStream(socket.inputStream), DataOutputStream(socket.outputStream))
            socketList.add(socketAndIo)

            val serverAcceptData = thread(true){
                println("Server Accept Now Wait for Data")
                when(val dataString = socketAndIo.input.readUTF()){
                    "Input_Profile" -> {
                        //socketAndIo.output.writeUTF("result_ok")
                        val profile64 =String(getSocketRead(socketAndIo.input), Charsets.UTF_8)

                        println("연결 되었음" + profile64)

                        // 암호화 해제
                        val profile = gson.fromJson(profile64, Profile::class.java)
                        //val profile = Json.decodeFromString<Profile>(profile64)

                        println("연결 되었음" + profile)

                        println("Josn을 List로 변환 완료")

                        val fingers = getFingerData(database)

                        val image1 = base64ToBufferedImage(profile.fingerDB.finger1) as BufferedImage
                        saveBufferedImageToFile(image1, "test1.png")
                        val image2 = base64ToBufferedImage(profile.fingerDB.finger2) as BufferedImage
                        saveBufferedImageToFile(image2, "test2.png")
                        val image3 = base64ToBufferedImage(profile.fingerDB.finger3) as BufferedImage
                        saveBufferedImageToFile(image3, "test3.png")
                        val image4 = base64ToBufferedImage(profile.fingerDB.finger4) as BufferedImage
                        saveBufferedImageToFile(image4, "test4.png")

                        val file_finger1 = Files.readAllBytes(Paths.get("test1.png"));
                        val file_finger2 = Files.readAllBytes(Paths.get("test2.png"));
                        val file_finger3 = Files.readAllBytes(Paths.get("test3.png"));
                        val file_finger4 = Files.readAllBytes(Paths.get("test4.png"));

                        val proFinger1 = FingerprintTemplate(FingerprintImage(file_finger1))
                        val proFinger2 = FingerprintTemplate(FingerprintImage(file_finger2))
                        val proFinger3 = FingerprintTemplate(FingerprintImage(file_finger3))
                        val proFinger4 = FingerprintTemplate(FingerprintImage(file_finger4))




                        val result = fingerMatch(proFinger1,proFinger2,proFinger3,proFinger4,fingers)


                        if (result.first){
                            println("있음")
                            profile.id = result.second

                            deleteAllById(database, profile.id);
                            insert_Data(database, profile)

                            getSocketWrite(socketAndIo.output, "Ok".toByteArray(Charsets.UTF_8))//이미 있는 유저를 갱신한다는 뜻
                        }else{
                            println("ID 없음, Proflie = " + profile)
                            val tempid = getIndexID(database) + 1
                            profile.id = tempid
                            println("ID 생성" + profile.id)

                            deleteAllById(database, profile.id);
                            insert_Data(database, profile)

                            getSocketWrite(socketAndIo.output, "Err".toByteArray(Charsets.UTF_8))//이미 있는 유저가 없다는 뜻
                        }
                    }
                    "Try_Login" -> {
                        val finger64 = String(getSocketRead(socketAndIo.input), Charsets.UTF_8)
                        //암호화 해제
                        val finger = gson.fromJson(finger64, FingerData::class.java)

                        val fingers = getFingerData(database)
                        val image1 = base64ToBufferedImage(finger.finger1) as BufferedImage
                        saveBufferedImageToFile(image1, "test1.png")
                        val image2 = base64ToBufferedImage(finger.finger2) as BufferedImage
                        saveBufferedImageToFile(image2, "test2.png")
                        val image3 = base64ToBufferedImage(finger.finger3) as BufferedImage
                        saveBufferedImageToFile(image3, "test3.png")
                        val image4 = base64ToBufferedImage(finger.finger4) as BufferedImage
                        saveBufferedImageToFile(image4, "test4.png")

                        val file_finger1 = Files.readAllBytes(Paths.get("test1.png"));
                        val file_finger2 = Files.readAllBytes(Paths.get("test2.png"));
                        val file_finger3 = Files.readAllBytes(Paths.get("test3.png"));
                        val file_finger4 = Files.readAllBytes(Paths.get("test4.png"));

                        val proFinger1 = FingerprintTemplate(FingerprintImage(file_finger1))
                        val proFinger2 = FingerprintTemplate(FingerprintImage(file_finger2))
                        val proFinger3 = FingerprintTemplate(FingerprintImage(file_finger3))
                        val proFinger4 = FingerprintTemplate(FingerprintImage(file_finger4))

                        val result = fingerMatch(proFinger1,proFinger2,proFinger3,proFinger4,fingers)

                        if (result.first){
                            val return_profile = MergebyId(database, result.second)
                            val netMessage = return_profile.toByteArray(Charsets.UTF_8)
                            getSocketWrite(socketAndIo.output, netMessage)
                        }
                        else{
                            getSocketWrite(socketAndIo.output, "Err".toByteArray(Charsets.UTF_8))//로그인 실패라는 뜻
                        }
                    }
//                    "Input_Image" -> {
//                        println("이미지를 테스트 수행")
//                        val data_size = socketAndIo.input.readInt()
//                        var count = 0
//                        val data = ByteArray(data_size)
//                        while(data_size > count){
//                            socketAndIo.input.read(data,count,min(data_size - count , 1024))
//                            count += 1024
//                        }
//
//                        val data_test = String(data, Charsets.UTF_8)
//                        println("이미지를 파일로 임시 저장 크기 ${data_size}")
//                        //println("이미지 문자열: ${data_test}")
//                        val image = base64ToBufferedImage(data_test) as BufferedImage
//                        saveBufferedImageToFile(image, "test1.png")
//                        count = 0
//                        while(data_size > count){
//                            socketAndIo.output.write(data,count,min(data_size - count , 1024))
//                            count += 1024
//                        }
//
//                    }
//                    "Input_Image1" -> {
//                        println("이미지를 테스트 수행")
//                        val data_size = socketAndIo.input.readInt()
//                        var count = 0
//                        val data = ByteArray(data_size)
//                        while(data_size > count){
//                            socketAndIo.input.read(data,count,min(data_size - count , 1024))
//                            count += 1024
//                        }
//                        val data_test = String(data, Charsets.UTF_8)
//                        println("이미지를 파일로 임시 저장 크기 ${data_size}")
//                        //println("이미지 문자열: ${data_test}")
//                        val image = base64ToBufferedImage(data_test) as BufferedImage
//                        saveBufferedImageToFile(image, "test2.png")
//                        count = 0
//                        while(data_size > count){
//                            socketAndIo.output.write(data,count,min(data_size - count , 1024))
//                            count += 1024
//                        }
//                    }
//                    "Input_Image2" -> {
//                        println("이미지를 테스트 수행")
//                        val data_size = socketAndIo.input.readInt()
//                        var count = 0
//                        val data = ByteArray(data_size)
//                        while(data_size > count){
//                            socketAndIo.input.read(data,count,min(data_size - count , 1024))
//                            count += 1024
//                        }
//                        val data_test = String(data, Charsets.UTF_8)
//                        println("이미지를 파일로 임시 저장 크기 ${data_size}")
//                        //println("이미지 문자열: ${data_test}")
//                        val image = base64ToBufferedImage(data_test) as BufferedImage
//                        saveBufferedImageToFile(image, "test3.png")
//                        count = 0
//                        while(data_size > count){
//                            socketAndIo.output.write(data,count,min(data_size - count , 1024))
//                            count += 1024
//                        }
//                    }
//                    "Input_Image3" -> {
//                        println("이미지를 테스트 수행")
//                        val data_size = socketAndIo.input.readInt()
//                        var count = 0
//                        val data = ByteArray(data_size)
//                        while(data_size > count){
//                            socketAndIo.input.read(data,count,min(data_size - count , 1024))
//                            count += 1024
//                        }
//                        val data_test = String(data, Charsets.UTF_8)
//                        println("이미지를 파일로 임시 저장 크기 ${data_size}")
//                        //println("이미지 문자열: ${data_test}")
//                        val image = base64ToBufferedImage(data_test) as BufferedImage
//                        saveBufferedImageToFile(image, "test4.png")
//                        count = 0
//                        while(data_size > count){
//                            socketAndIo.output.write(data,count,min(data_size - count , 1024))
//                            count += 1024
//                        }
//
//                        val file_finger1 = Files.readAllBytes(Paths.get("test1.png"));
//                        val file_finger2 = Files.readAllBytes(Paths.get("test2.png"));
//                        val file_finger3 = Files.readAllBytes(Paths.get("test3.png"));
//                        val file_finger4 = Files.readAllBytes(Paths.get("test4.png"));
//
//                        val proFinger1 = FingerprintTemplate(FingerprintImage(file_finger1))
//                        val promatch = FingerprintMatcher(proFinger1)
//                        val proFinger2 = FingerprintTemplate(FingerprintImage(file_finger2))
//                        val proFinger3 = FingerprintTemplate(FingerprintImage(file_finger3))
//                        val proFinger4 = FingerprintTemplate(FingerprintImage(file_finger4))
//
//                        val similer0 = promatch.match(proFinger1)
//                        val similer1 = promatch.match(proFinger2)
//                        val similer2 = promatch.match(proFinger3)
//                        val similer3 = promatch.match(proFinger4)
//
//                        println("test0: ${similer0}, test1: ${similer1}, test2: ${similer2}, test3: ${similer3}")
//
//                    }
                    else -> {
                        val data = getSocketRead(socketAndIo.input)
                        getSocketWrite(socketAndIo.output, data)
                        println("${dataString}에코 수행")
                        println("${String(data, Charsets.UTF_8)}에코 수행")
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

fun getSocketRead(input: DataInputStream): ByteArray {
    var count = 0
    val size = input.readInt()
    val resultArray: ByteArray = ByteArray(size)
    count = 0
    while (count < size){
        input.read(resultArray, count, min(1024, size - count))
        count += 1024
    }
    return resultArray
}
fun getSocketWrite(output: DataOutputStream, data: ByteArray){
    var count = 0
    val size = data.size
    output.writeInt(size)
    count = 0
    while (count < size){
        output.write(data, count, min(1024, size - count))
        count += 1024
    }
}

fun connectToDatabase(): Connection? {
    val url = "jdbc:mysql://localhost:413/child_helper"
    val user = "User5"
    val password = "0000"

    return try {
        DriverManager.getConnection(url, user, password)
    } catch (e: SQLException) {
        null
    }
}

fun LZ4KToFingerprintImage(string: String): FingerprintImage? {
    return try {
        val data = LZ4K.decompressFromBase64(string)?.toByteArray(charset("UTF-8"))
        return FingerprintImage(data)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

private fun base64ToFingerprintImage(string: String): FingerprintImage? {
    return try{
        val base64decoder = Base64.getDecoder()
        val data = base64decoder.decode(string)
        return FingerprintImage(data)
    }catch (e: Exception){
        e.printStackTrace()
        null
    }
}

fun base64ToBufferedImage(base64String: String): BufferedImage? {
    return try {
        val base64decoder = Base64.getDecoder()
        val data = base64decoder.decode(base64String)
        val inputStream = ByteArrayInputStream(data)
        return ImageIO.read(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun base64ToByteArray(base64String: String): ByteArray?{
    return try{
        val base64Decoder = Base64.getDecoder()
        val data = base64Decoder.decode(base64String)
//        val inputStream = ByteArrayInputStream(data)
//        val result = inputStream.readBytes()
//        inputStream.close()
        return data
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun saveBufferedImageToFile(bufferedImage: BufferedImage, filePath: String) {
    try {
        val outputFile = File(filePath)
        ImageIO.write(bufferedImage, "png", outputFile)
        println("Image saved successfully to: $filePath")
    } catch (e: Exception) {
        e.printStackTrace()
        println("Error saving image to file")
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

        val image1 = base64ToBufferedImage(finger.finger1) as BufferedImage
        saveBufferedImageToFile(image1, "other_t1.png")
        val image2 = base64ToBufferedImage(finger.finger2) as BufferedImage
        saveBufferedImageToFile(image2, "other_t2.png")
        val image3 = base64ToBufferedImage(finger.finger3) as BufferedImage
        saveBufferedImageToFile(image3, "other_t3.png")
        val image4 = base64ToBufferedImage(finger.finger4) as BufferedImage
        saveBufferedImageToFile(image4, "other_t4.png")

        val file_finger1 = Files.readAllBytes(Paths.get("other_t1.png"));
        val file_finger2 = Files.readAllBytes(Paths.get("other_t2.png"));
        val file_finger3 = Files.readAllBytes(Paths.get("other_t3.png"));
        val file_finger4 = Files.readAllBytes(Paths.get("other_t4.png"));

        val finger1 = FingerprintTemplate(FingerprintImage(file_finger1))
        val finger2 = FingerprintTemplate(FingerprintImage(file_finger2))
        val finger3 = FingerprintTemplate(FingerprintImage(file_finger3))
        val finger4 = FingerprintTemplate(FingerprintImage(file_finger4))

        val finger1Matcher = FingerprintMatcher(finger1)
        val finger2Matcher = FingerprintMatcher(finger2)
        val finger3Matcher = FingerprintMatcher(finger3)
        val finger4Matcher = FingerprintMatcher(finger4)

        val similer = (finger1Matcher.match(proFinger1)+
                finger2Matcher.match(proFinger2)+
                finger3Matcher.match(proFinger3)+
                finger4Matcher.match(proFinger4)) / 4
        println("similer1: ${finger1Matcher.match(proFinger1)}")
        println("similer2: ${finger2Matcher.match(proFinger2)}")
        println("similer3: ${finger3Matcher.match(proFinger3)}")
        println("similer4: ${finger4Matcher.match(proFinger4)}")
        if (similer > 40){ //4개의 지문의 유사도의 평균이 40 이상이면 같은사람이라고 취급
            println("이미 만들어진 프로필 이라고 판단됩니다. similer : " + similer)
            return Pair(true, finger.id)
        }
    }
    return Pair(false, -1)
}