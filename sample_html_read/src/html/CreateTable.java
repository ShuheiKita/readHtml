package html;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CreateTable {
	
	public static void main(String[] args){
		
		
		
		
		/**/
		try{
			Connection con = DBManager.getConnection();
			Statement smt = smt = con.createStatement();
			String sql;
			ResultSet result;
			
			/*カテゴリ・形テーブル作成*/
			sql = "create table category_table (" +
					"category varchar(128) not null, " +
					"primary key(category))";
			//smt.executeUpdate(sql);
			
			sql = "create table form_table (form varchar(128) not null, " +
					"primary key(form))";
			//smt.executeUpdate(sql);
			
			System.out.println("CATEGORY");
			sql = "select * from category_table";
			result = smt.executeQuery(sql);
			while(result.next()){
				System.out.print(" "+result.getString("category")+"  ");
			
			}
			System.out.println("FORM");
			sql = "select * from form_table";
			result = smt.executeQuery(sql);
			while(result.next()){
				System.out.print(" "+result.getString("form")+"  ");
			
			}
			/*サークル検索*/
			sql = "select circle_id from circle_table";
			result = smt.executeQuery(sql);
			List<Integer> list = new ArrayList<Integer>();
			/*
			while(result.next()){
				int id = result.getInt("circle_id");
				list.add(new Integer(id));
			}
			while(list.size()>0){
				int id = list.remove(0);
				//System.out.println(id);
				String sql_field = "select * from field_table_"+id+"";
				ResultSet fields = smt.executeQuery(sql_field);
				while(fields.next()){
					int type = fields.getInt("type");
					String value = fields.getString("value");
					if(type==4){
						String sql_c = "insert into category_table values (?)";
						PreparedStatement smt_c = con.prepareStatement(sql_c);
						smt_c.setString(1, value);
						try{
							smt_c.execute();
						}catch(SQLException e){}
					}else if(type==5){
						String sql_f = "insert into form_table values (?)";
						PreparedStatement smt_f = con.prepareStatement(sql_f);
						smt_f.setString(1, value);
						try{
							smt_f.execute();
						}catch(SQLException e){}
					}
				}
			}
			*/
			con.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
