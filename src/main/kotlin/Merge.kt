import java.sql.Connection
import com.google.gson.Gson
class Merge {
}

// 사용법
// MergebyId 에 (database, 내가 원하는 ID(int)를 넣는다.
// 그러면 Gson으로 Json화 된 Stirng을 반환한다.

fun MergebyId(connection: Connection, id : Int) : String{
    val gson = Gson()

    val merge_Proflie : Profile = Profile(id,
        getNameFromDatabase(connection, id),
        getFingerFromDatabase(connection, id),
        getAdressFromDatabase(connection, id),
        getPhoneNumberFromDatabase(connection, id),
        getMemoFromDatabase(connection, id))


    return gson.toJson(merge_Proflie)
}

fun getNameFromDatabase(connection: Connection, id: Int) : NameData {
    var merged_Profile_nameDB = NameData(-1, "", "")
    // 데이터베이스 연결 및 쿼리 실행
    connection.createStatement().use { statement ->
        val sql = "SELECT ID, Name, ProfileImg  FROM regname WHERE id = $id"
        statement.executeQuery(sql).use { resultSet ->
            // 결과 처리
            while (resultSet.next()) {
                val ID = resultSet.getInt("ID")
                val name = resultSet.getString("Name")
                val profileimg = resultSet.getString("ProfileImg")
                merged_Profile_nameDB = NameData(ID, name, profileimg)
            }
        }
    }
    return merged_Profile_nameDB
}

fun getFingerFromDatabase(connection: Connection, id: Int) : FingerData {
    var merged_Profile_fingerDB = FingerData(-1, "", "", "", "")
    // 데이터베이스 연결 및 쿼리 실행
    connection.createStatement().use { statement ->
        val sql = "SELECT ID, Finger1, Finger2, Finger3, Finger4 FROM regfinger WHERE id = $id"
        statement.executeQuery(sql).use { resultSet ->
            // 결과 처리
            while (resultSet.next()) {
                val ID = resultSet.getInt("ID")
                val finger1 = resultSet.getString("Finger1")
                val finger2 = resultSet.getString("Finger2")
                val finger3 = resultSet.getString("Finger3")
                val finger4 = resultSet.getString("Finger4")
                merged_Profile_fingerDB = FingerData(ID, finger1, finger2, finger3, finger4)
            }
        }
    }
    return merged_Profile_fingerDB
}

fun getAdressFromDatabase(connection: Connection, id: Int) : MutableList<AdressData>{
    val adressDataList = mutableListOf<AdressData>()

    // 데이터베이스 연결 및 쿼리 실행
    connection.createStatement().use { statement ->
        val sql = "SELECT adress, reg_order FROM regadress WHERE id = $id"
        statement.executeQuery(sql).use { resultSet ->
            // 결과 처리
            while (resultSet.next()) {
                val adress = resultSet.getString("adress")
                val regOrder = resultSet.getInt("reg_order")
                adressDataList.add(AdressData(adress, regOrder))
            }
        }
    }
    return adressDataList
}


fun getPhoneNumberFromDatabase(connection: Connection, id: Int) : MutableList<PhoneNumberData> {
    val phoneDataList = mutableListOf<PhoneNumberData>()

    // 데이터베이스 연결 및 쿼리 실행
    connection.createStatement().use { statement ->
        val sql = "SELECT PhoneNumber, reg_order FROM regphonenumber WHERE id = $id"
        statement.executeQuery(sql).use { resultSet ->
            // 결과 처리
            while (resultSet.next()) {
                val phonenumber = resultSet.getString("PhoneNumber")
                val regOrder = resultSet.getInt("reg_order")
                phoneDataList.add(PhoneNumberData(phonenumber, regOrder))
                println(phoneDataList)
            }
        }
    }
    return phoneDataList
}

fun getMemoFromDatabase(connection: Connection, id: Int) : MutableList<MemoData> {
    val momoDataList = mutableListOf<MemoData>()

    // 데이터베이스 연결 및 쿼리 실행
    connection.createStatement().use { statement ->
        val sql = "SELECT Memo, reg_order FROM regmemo WHERE id = $id"
        statement.executeQuery(sql).use { resultSet ->
            // 결과 처리
            while (resultSet.next()) {
                val memo = resultSet.getString("Memo")
                val regOrder = resultSet.getInt("reg_order")
                momoDataList.add(MemoData(memo, regOrder))
            }
        }
    }
    return momoDataList
}



