import java.sql.Connection
import java.sql.SQLException

class Delete {
}

// 사용법

// EX) 전체 삭제
// 1, deleteAllById에 매개 변수로 (database, "삭제하고 싶은 아이디(Int형)")을 넣어준다.
// 2. 모든 테이블에 해당 아이디를 가지고 있는 모든 데이터가 삭제된다.

// EX) 테이블 선택 삭제
// 1. deleteRowById에 매개 변수로 (database, "삭제하고 싶은 아이디(Int형)", "삭제하고 싶은 테이블(String)")을 넣어준다.
// 2. 선택한 테이블의 해당 아이디를 가지고 있는 데이터가 삭제된다.

// 참고로 ID가 없어도 작동함. (키야 잘만들었다!

fun deleteAllById(connection: Connection, id: Int){
    deleteRowById(connection, id, "regname")
    deleteRowById(connection, id, "regfinger")
    deleteRowById(connection, id, "regadress")
    deleteRowById(connection, id, "regphonenumber")
    deleteRowById(connection, id, "regmemo")
}

fun deleteRowById(connection: Connection,id: Int, tableName: String) {
    try {
        // DELETE 쿼리 작성
        val deleteQuery = "DELETE FROM $tableName WHERE ID = ?"

        // PreparedStatement를 사용하여 쿼리 실행
        connection.prepareStatement(deleteQuery).use { preparedStatement ->
            preparedStatement.setInt(1, id)

            // 쿼리 실행
            val rowsAffected = preparedStatement.executeUpdate()

            if (rowsAffected > 0) {
                println("데이터 삭제 성공!")
            } else {
                println("데이터 삭제 실패: 해당 ID를 찾을 수 없음")
            }
        }
    } catch (e: SQLException) {
        e.printStackTrace()
    }
}
