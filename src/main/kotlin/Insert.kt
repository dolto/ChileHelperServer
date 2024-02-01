import java.sql.Connection
import java.sql.SQLException

class Insert {
}


// 사용법
// 1. Profile에 아이디를 잘 집어 넣어 준다.
// 2. insert_Data에 매개변수로 (database, Profile 형태 데이터) 넣고 돌린다.
// 3. 데이터 베이스에 안전하게 들어간다.

fun insert_Data(connection: Connection, profile: Profile){
    val ins_NameData: NameData = profile.nameDB
    insertNametData(connection, profile.id, ins_NameData)

    val ins_FingerData: FingerData = profile.fingerDB
    insertFingertData(connection, profile.id, ins_FingerData)

    val ins_AddressList: MutableList<AdressData> = profile.adressDB
    inserAdresstData(connection, profile.id, ins_AddressList)

    val ins_PhoneList: MutableList<PhoneNumberData> = profile.phoneNumberDB
    insertPhonetData(connection, profile.id, ins_PhoneList)

    val ins_MemoList: MutableList<MemoData> = profile.memoDB
    insertMemotData(connection, profile.id, ins_MemoList)
}

fun insertNametData(connection: Connection, id : Int, data_list : NameData) {
    try {
        // MutableList만큼 반복
        val insertQuery = "INSERT INTO regname (ID, Name, ProfileImg) VALUES (?, ?, ?)"

        // PreparedStatement를 사용하여 쿼리 실행
        connection.prepareStatement(insertQuery).use { preparedStatement ->
            preparedStatement.setInt(1, id)
            preparedStatement.setString(2, data_list.name)
            preparedStatement.setString(3, data_list.profile)
            // 쿼리 실행
            val rowsAffected = preparedStatement.executeUpdate()
            if (rowsAffected > 0) {
                print("성공!")
            } else {
                print("실패!")
            }
        }


    } catch (e: SQLException) {
        e.printStackTrace()
    }
    println("이름 데이터 삽입 성공!")
}
fun getRandomString(length: Int) : String {
    val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
    return (1..length)
        .map { charset.random() }
        .joinToString("")
}
fun insertFingertData(connection: Connection, id : Int, data_list : FingerData) {
    try {
        // MutableList만큼 반복
        val insertQuery = "INSERT INTO regfinger (ID, Finger1, Finger2, Finger3, Finger4) VALUES (?, ?, ?, ?, ?)"

        // PreparedStatement를 사용하여 쿼리 실행
        connection.prepareStatement(insertQuery).use { preparedStatement ->
            preparedStatement.setInt(1, id)
            preparedStatement.setString(2, getRandomString(5)+data_list.finger1)
            preparedStatement.setString(3, getRandomString(5)+data_list.finger2)
            preparedStatement.setString(4, getRandomString(5)+data_list.finger3)
            preparedStatement.setString(5, getRandomString(5)+data_list.finger4)
            // 쿼리 실행
            val rowsAffected = preparedStatement.executeUpdate()
            if (rowsAffected > 0) {
                println("성공!")
            } else {
                println("실패!")
            }
        }

    } catch (e: SQLException) {
        e.printStackTrace()
    }
    println("지문 데이터 삽입 성공!")
}


// 주소 데이터 삽입
fun inserAdresstData(connection: Connection, id : Int, data_list : MutableList<AdressData>) {
    try {
        // MutableList만큼 반복
        for(i in 0 .. (data_list.size -1)){
            // 삽입할 데이터 및 SQL 쿼리문 작성
            val insertQuery = "INSERT IGNORE INTO regadress (ID, Adress, reg_order) VALUES (?, ?, ?)"

            // PreparedStatement를 사용하여 쿼리 실행
            connection.prepareStatement(insertQuery).use { preparedStatement ->
                preparedStatement.setInt(1, id)
                preparedStatement.setString(2, data_list[i].adress)
                preparedStatement.setInt(3, data_list[i].reg_order)
                // 쿼리 실행
                val rowsAffected = preparedStatement.executeUpdate()
                if (rowsAffected > 0) {
                    println("성공!")
                } else {
                    println("실패!")
                }
            }
        }

    } catch (e: SQLException) {
        e.printStackTrace()
    }
    println("주소 데이터 삽입 성공!")
}

fun insertPhonetData(connection: Connection, id : Int, data_list : MutableList<PhoneNumberData>) {
    try {
        // MutableList만큼 반복
        for(i in 0 .. (data_list.size -1)){
            // 삽입할 데이터 및 SQL 쿼리문 작성
            val insertQuery = "INSERT IGNORE INTO regphonenumber (ID, PhoneNumber, reg_order) VALUES (?, ?, ?)"

            // PreparedStatement를 사용하여 쿼리 실행
            connection.prepareStatement(insertQuery).use { preparedStatement ->
                preparedStatement.setInt(1, id)
                preparedStatement.setString(2, data_list[i].phoneNumber)
                preparedStatement.setInt(3, data_list[i].reg_order)
                // 쿼리 실행
                val rowsAffected = preparedStatement.executeUpdate()
                if (rowsAffected > 0) {
                    println("성공!")
                } else {
                    println("실패!")
                }
            }
        }

    } catch (e: SQLException) {
        e.printStackTrace()
    }
    println("번호 데이터 삽입 성공!")
}

fun insertMemotData(connection: Connection, id : Int, data_list : MutableList<MemoData>) {
    try {
        // MutableList만큼 반복
        for(i in 0 .. (data_list.size -1)){
            // 삽입할 데이터 및 SQL 쿼리문 작성
            val insertQuery = "INSERT IGNORE INTO regmemo (ID, Memo, reg_order) VALUES (?, ?, ?)"

            // PreparedStatement를 사용하여 쿼리 실행
            connection.prepareStatement(insertQuery).use { preparedStatement ->
                preparedStatement.setInt(1, id)
                preparedStatement.setString(2, data_list[i].memo)
                preparedStatement.setInt(3, data_list[i].reg_order)
                // 쿼리 실행
                val rowsAffected = preparedStatement.executeUpdate()
                if (rowsAffected > 0) {
                    println("성공!")
                } else {
                    println("실패!")
                }
            }
        }

    } catch (e: SQLException) {
        e.printStackTrace()
    }
    println("메모 데이터 삽입 성공!")
}



