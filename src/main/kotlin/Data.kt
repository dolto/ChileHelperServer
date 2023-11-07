import java.lang.StringBuilder
import java.sql.Connection

data class NameData(
    var id: Int, val name: String, val profile: String
)
fun getNameData(database: Connection, id:Int): NameData?{
    val sql = "SELECT * FROM RegName WHERE ID = $id"
    val statement = database.createStatement()
    val resultSet = statement.executeQuery(sql)
    var result: NameData? = null
    while (resultSet.next()){
        val _id = resultSet.getInt("ID")
        val name = resultSet.getNString("Name")
        val profileimg = resultSet.getNString("ProfileImg")
        result = NameData(_id, name, profileimg)
        break
    }
    return result
}
fun updateNameData(database: Connection, data:NameData){
    val sql = "UPDATE RegName SET Name=?, ProfileImg=? WHERE ID=${data.id};"
    val statement = database.prepareStatement(sql)
    statement.setString(0,data.name)
    statement.setString(1,data.profile)
    statement.executeUpdate()
}
fun insertNameData(database: Connection, data:NameData){
    val sql = "INSERT INTO RegName (Name, ProfileImg) values (?, ?)"
    val statement = database.prepareStatement(sql)
    statement.setString(0,data.name)
    statement.setString(1,data.profile)
    statement.executeUpdate()
}
fun getNameData(database: Connection): MutableList<NameData>{
    val sql = "SELECT * FROM RegName"
    val statement = database.createStatement()
    val resultSet = statement.executeQuery(sql)
    val result: MutableList<NameData> = mutableListOf()
    while (resultSet.next()){
        val _id = resultSet.getInt("ID")
        val name = resultSet.getNString("Name")
        val profileimg = resultSet.getNString("ProfileImg")
        result.add(NameData(_id, name, profileimg))
    }
    return result
}
data class FingerData(
    var id: Int, val finger1: String, val finger2: String, val finger3: String, val finger4: String
)
fun updateFingerData(database: Connection, data:FingerData){
    val sql = "UPDATE RegFinger SET Finger1=?,Finger2=?,Finger3=?,Finger4=? WHERE ID=${data.id}"
    val statement = database.prepareStatement(sql)
    statement.setString(0, data.finger1)
    statement.setString(1, data.finger2)
    statement.setString(2, data.finger3)
    statement.setString(3, data.finger4)
    statement.executeUpdate()
}
fun insertFingerData(database: Connection, data:FingerData){
    val sql = "INSERT INTO RegFinger (Finger1,Finger2,Finger3,Finger4) values (?,?,?,?)"
    val statement = database.prepareStatement(sql)
    statement.setString(0, data.finger1)
    statement.setString(1, data.finger2)
    statement.setString(2, data.finger3)
    statement.setString(3, data.finger4)
    statement.executeUpdate()
}
fun getFingerData(database: Connection, id:Int): FingerData?{
    val sql = "SELECT * FROM RegFinger WHERE ID = $id"
    val statement = database.createStatement()
    val resultSet = statement.executeQuery(sql)
    var result: FingerData? = null
    while (resultSet.next()){
        val finger1 = resultSet.getNString("Finger1")
        val finger2 = resultSet.getNString("Finger2")
        val finger3 = resultSet.getNString("Finger3")
        val finger4 = resultSet.getNString("Finger4")
        result = FingerData(id, finger1, finger2, finger3, finger4)
        break
    }
    return result
}
fun getFingerData(database: Connection): MutableList<FingerData>{
    val sql = "SELECT * FROM RegFinger"
    val statement = database.createStatement()
    val resultSet = statement.executeQuery(sql)
    val result: MutableList<FingerData> = mutableListOf()
    while (resultSet.next()){
        val id = resultSet.getInt("ID");
        val finger1 = resultSet.getNString("Finger1")
        val finger2 = resultSet.getNString("Finger2")
        val finger3 = resultSet.getNString("Finger3")
        val finger4 = resultSet.getNString("Finger4")
        result.add(FingerData(id, finger1, finger2, finger3, finger4))
    }
    return result
}
data class AdressData(
    val adress: String
)
fun getAdressData(database: Connection, id:Int): MutableList<AdressData>{
    val sql = "SELECT * FROM RegAdress WHERE ID = $id"
    val statement = database.createStatement()
    val resultSet = statement.executeQuery(sql)
    val result: MutableList<AdressData> = mutableListOf()
    while (resultSet.next()){
        val adress = resultSet.getNString("Adress")
        result.add(AdressData(adress))
    }
    return result
}
fun updateAdressData(database: Connection, data:MutableList<AdressData>, id:Int){
    val sql = StringBuilder("DELETE FROM RegAdress WHERE ID = $id;")
    for (adress in data){
        sql.append("INSERT INTO RegAdress values ($id '${adress.adress}');")
    }
    val statement = database.prepareStatement(sql.toString())
    statement.executeUpdate()
}
data class PhoneNumberData(
    val phoneNumber: String
)
fun getPhoneNumberData(database: Connection, id:Int): MutableList<PhoneNumberData>{
    val sql = "SELECT * FROM RegPhoneNumber WHERE ID = $id"
    val statement = database.createStatement()
    val resultSet = statement.executeQuery(sql)
    val result: MutableList<PhoneNumberData> = mutableListOf()
    while (resultSet.next()){
        val phoneNumber = resultSet.getNString("PhoneNumber")
        result.add(PhoneNumberData(phoneNumber))
    }
    return result
}
fun updatePhoneNumberData(database: Connection, data:MutableList<PhoneNumberData>, id:Int){
    val sql = StringBuilder("DELETE FROM RegAdress WHERE ID = $id;")
    for (phone in data){
        sql.append("INSERT INTO RegAdress values ($id '${phone.phoneNumber}');")
    }
    val statement = database.prepareStatement(sql.toString())
    statement.executeUpdate()
}
data class MemoData(
    val memo: String
)
fun getMemoData(database: Connection, id:Int): MutableList<MemoData>{
    val sql = "SELECT * FROM RegMemo WHERE ID = $id"
    val statement = database.createStatement()
    val resultSet = statement.executeQuery(sql)
    val result: MutableList<MemoData> = mutableListOf()
    while (resultSet.next()){
        val memo = resultSet.getNString("Memo")
        result.add(MemoData(memo))
    }
    return result
}
fun updateMemoData(database: Connection, data:MutableList<MemoData>, id:Int){
    val sql = StringBuilder("DELETE FROM RegAdress WHERE ID = $id;")
    for (memo in data){
        sql.append("INSERT INTO RegAdress values ($id '${memo.memo}');")
    }
    val statement = database.prepareStatement(sql.toString())
    statement.executeUpdate()
}
data class Profile(
    var id: Int,
    val nameDB: NameData, val fingerDB: FingerData,
    val adressDB: MutableList<AdressData>,
    val phoneNumberDB: MutableList<PhoneNumberData>,
    val memoDB: MutableList<MemoData>
)
fun getProfile(database: Connection, id: Int):Profile{
    val finger = getFingerData(database, id)
    val name = getNameData(database,id)
    val adress = getAdressData(database,id)
    val phoneNumber = getPhoneNumberData(database, id)
    val memo = getMemoData(database, id)

    return Profile(id, name!!, finger!!, adress, phoneNumber, memo)
}
fun updateProfile(database: Connection, data:Profile){
    val id = data.id
    data.nameDB.id = id
    data.fingerDB.id = id
    updateNameData(database, data.nameDB)
    updateFingerData(database, data.fingerDB)
    updatePhoneNumberData(database, data.phoneNumberDB, id)
    updateAdressData(database, data.adressDB, id)
    updateMemoData(database, data.memoDB, id)
}
fun insertProfile(database: Connection, data:Profile){
    val id = data.id
    data.nameDB.id = id
    data.fingerDB.id = id
    insertNameData(database, data.nameDB)
    insertFingerData(database, data.fingerDB)
    updatePhoneNumberData(database, data.phoneNumberDB, id)
    updateAdressData(database, data.adressDB, id)
    updateMemoData(database, data.memoDB, id)
}
fun getIndexID(database: Connection):Int {
    val sql = "SELECT ID FROM RegName ORDER BY ID DESC LIMIT 1;"
    val statement = database.createStatement()
    val resultSet = statement.executeQuery(sql)
    return if (resultSet.next()) {
        resultSet.getInt(1) // 인덱스는 1부터 시작
    } else {
        -1
    }
}