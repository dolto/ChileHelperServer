import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class Login {

}

fun Sign_in(connection: Connection, data_list : LoginData) :String {
    try {
        // SQL문 제작
        val sql = "SELECT * FROM Users WHERE UserID = ? AND Password = ?"
        //SqL문 ? 안에 들어갈 단어 제작
        val preparedStatement = connection.prepareStatement(sql)
        preparedStatement.setString(1, data_list.username)
        preparedStatement.setString(2, data_list.password)

        // 전송
        val resultSet = preparedStatement.executeQuery()
        val result = resultSet.next().toString()
        println("결과" + result)
        return result;

    }
    catch (e: Exception) {
        e.printStackTrace()
    }
    return "로그인 실패";
}

fun Sign_up(connection: Connection, data_list : LoginData) {
    try {
        // MutableList만큼 반복
        print(data_list.password+" 비밀 \n")
        val insertQuery = "INSERT INTO users (UserID, Password) VALUES (?, ?)"

        // PreparedStatement를 사용하여 쿼리 실행
        connection.prepareStatement(insertQuery).use { preparedStatement ->
            preparedStatement.setString(1, data_list.username)
            preparedStatement.setString(2, data_list.password)
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
    println("회원 가입 성공!")
}

fun getList_toUserId(connection:Connection, userId: String): List<NameData> {
    val userList = mutableListOf<NameData>()

    try {

        // 쿼리를 준비합니다.
        val sql = "SELECT ID, Name FROM regname WHERE UserID = ?"

        // PreparedStatement를 사용하여 쿼리 실행
        connection.prepareStatement(sql).use { preparedStatement ->
            preparedStatement.setString(1, userId)
            // 쿼리 실행
            val rowsAffected = preparedStatement.executeQuery()
            // 실행 이후 결과 처리
            while (rowsAffected.next()) {
                val id = rowsAffected.getInt("ID")
                val name = rowsAffected.getString("Name")
                userList.add(NameData(id, name, userId,""))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    println("UserList : " + userList)
    return userList
}
