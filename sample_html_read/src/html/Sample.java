package html;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Sample {
	public static void main(String[] args){
		try {
			Connection con = DBManager.getConnection();
			String table = "table";
			String sql = "select * from user_"+table+" where user_name=?";
			PreparedStatement smt = con.prepareStatement(sql);
			smt.setString(1, "taro");
			ResultSet result = smt.executeQuery();
			while(result.next()){
				System.out.println( result.getString("user_id") );
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
