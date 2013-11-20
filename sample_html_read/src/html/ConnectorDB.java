package html;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ConnectorDB {
	/*
	 * create table  circle_table (circle_id int auto_increment not null, circle_name varchar(128), circle_site varchar(256), primary key(circle_id));
	 */

	private static final int CATEGORY = 1;
	private static final int FORM = 2;

	/**
	 * サークルが登録済みか調べる.
	 * @param circle
	 * @return 登録済みの場合ID　　ない場合-1
	 */
	public static int isCircleId(Work work){
		int id = -1;
		try{
			Connection con = DBManager.getConnection();
			if(con==null) return -1;
			String sql = "select circle_id from circle_table where circle_name=?";
			PreparedStatement smt = con.prepareStatement(sql);
			smt.setString(1, work.getCircle().getName());
			ResultSet result = smt.executeQuery();
			if(result.next())
				id = result.getInt("circle_id");
			else{	//サークルが登録されていない場合
				if( insertCircle(work) ){
					id = isCircleId(work);
					work.getCircle().setId(id);
				}
			}
			con.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return id;
	}
	/**
	 * サークルサイト情報登録（更新時）.
	 * @param work
	 * @return
	 */
	public static boolean updateCircle(Work work){
		try{
			Connection con = DBManager.getConnection();
			if(con==null) return false;
			String sql = "update circle_table set circle_site=?, upload=NOW() where circle_id=?";
			PreparedStatement smt = con.prepareStatement(sql);
			smt.setString(1, work.getCircle().getSite_url());
			smt.setInt(2, work.getCircle().getId());
			int row = smt.executeUpdate();
			con.close();
			if(row==0) return false;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 作品が登録済みか調べる.
	 * @param work
	 * @return 登録済みの場合ID　　ない場合-1
	 */
	public static int isWorkId(Work work){
		int id = -1;
		try{
			Connection con = DBManager.getConnection();
			if(con==null) return -1;
			String sql = "select work_id from work_table where circle_id=? and work_name=?";
			PreparedStatement smt = con.prepareStatement(sql);
			smt.setInt(1, work.getCircle().getId());
			smt.setString(2, work.getName());
			ResultSet result = smt.executeQuery();
			if(result.next())
				id = result.getInt("work_id");
			con.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return id;
	}


	/**
	 * DLTable_YYYYがあるか調べる.
	 * @param work
	 * @return
	 */
	public static boolean isDlTable(Work work){
		try{
			Connection con = DBManager.getConnection();
			if(con==null) return false;
			String sql = "select count(*) from dl_table_"+work.getDateYM();
			Statement smt = con.createStatement();
			ResultSet result = smt.executeQuery(sql);
			con.close();
		}catch(Exception e){
			return createDlTable(work);
		}
		return true;
	}
	/**
	 * 
	 * @param work
	 * @return
	 */
	public static Dl[] getDl(Work work){
		List<Dl> list = new ArrayList<Dl>();
		try{
			Connection con = DBManager.getConnection();
			if(con==null) return null;
			String sql = "select *from dl_table_"+work.getDateYM()+" where work_id=? order by day desc";
			PreparedStatement smt = con.prepareStatement(sql);
			smt.setInt(1, work.getId());
			ResultSet result = smt.executeQuery();
			while(result.next()){
				Dl dl = new Dl();
				dl.setDate(result.getDate("day"));
				dl.setDl(result.getInt("dl"));
				list.add(dl);
			}
			con.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		if(list.size()==0) return null;
		return list.toArray(new Dl[0]);
	}

	/**
	 * ダウンロードテーブル月別　作成.
	 * @param work
	 * @return
	 */
	private static boolean createDlTable(Work work){
		try{
			Connection con = DBManager.getConnection();
			if(con==null) return false;
			String sql = "create table dl_table_"+work.getDateYM()+" (" +
					" work_id int  not null " +
					",dl int" +
					",day date" +
					",upload datetime" +
					",primary key(work_id, day)"
					+");";
			Statement smt = con.createStatement();
			smt.executeUpdate(sql);
			con.close();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	/**
	 * ダウンロード数更新.
	 * @param work
	 * @param cal
	 * @return
	 */
	public static boolean updateDl(Work work, Calendar cal){
		try{
			Connection con = DBManager.getConnection();
			if(con==null) return false;
			String sql = "update dl_table_"+work.getDateYM()+" set dl=? where work_id="+work.getId()+" and day='"+new Date(cal.getTimeInMillis()).toString()+"'";
			PreparedStatement smt = con.prepareStatement(sql);
			smt.setInt(1, work.getDl()[0].getDl());
			int row = smt.executeUpdate();
			con.close();
			if(row==0) return false;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	/**
	 * 作品テーブルのDL数更新.
	 * @param work
	 * @return
	 */
	public static boolean updateDl_work(Work work){
		try{
			Connection con = DBManager.getConnection();
			if(con==null) return false;
			String sql = "update work_table set dl=? where work_id="+work.getId();
			PreparedStatement smt = con.prepareStatement(sql);
			smt.setInt(1, work.getDl()[0].getDl());
			int row = smt.executeUpdate();
			con.close();
			if(row==0) return false;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * ダウンロードテーブルに登録済みか.
	 * @param work
	 * @param cal
	 * @return
	 */
	public static boolean isDlQuery(Work work, Calendar cal){
		boolean flag = false;
		try{
			Connection con = DBManager.getConnection();
			if(con==null) return false;
			String sql = "select * from dl_table_"+work.getDateYM()+" where work_id="+work.getId()+" and day='"+new Date(cal.getTimeInMillis()).toString()+"'";
			Statement smt = con.createStatement();
			ResultSet result = smt.executeQuery(sql);
			if(result.next()){
				flag = true;
			}
			con.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return flag;
	}


	/**
	 * 属性登録.
	 * @param work
	 * @return
	 */
	public static void insertFields(Work work){
		//カテゴリー情報登録
		String[] list  = work.getCategory();
		if(list != null){
			for(String str : list){
				//属性情報テーブルから　ID取得
				int id = getFieldId(str);
				//登録されていない場合 登録する
				if(id == -1){
					insertFieldInfo(str);
					id = getFieldId(str);
				}
				insertField(CATEGORY, id, work);
			}
		}
		//作品形式情報登録
		list = work.getForm();
		if(list != null){
			for(String str : list){
				//属性情報テーブルから　ID取得
				int id = getFieldId(str);
				//登録されていない場合 登録する
				if(id == -1){
					insertFieldInfo(str);
					id = getFieldId(str);
				}
				insertField(FORM, id, work);
			}
		}
	}
	/**
	 * 作品属性登録.
	 * @param type
	 * @param value
	 * @param work
	 * @return
	 */
	private static boolean insertField(int type, int field_id, Work work){
		try{
			Connection con = DBManager.getConnection();
			if(con==null) return false;
			StringBuilder sql = new StringBuilder("insert into field_table (");
			sql.append(" work_id");
			sql.append(",type");
			sql.append(",field_id");
			sql.append(",upload");
			sql.append(") values (");
			sql.append(" ?");
			sql.append(",?");
			sql.append(",?");
			sql.append(",NOW()");
			sql.append(");");
			
			PreparedStatement smt = con.prepareStatement(sql.toString());
			//作品ID取得
			smt.setInt(1, work.getId());
			//属性タイプ
			smt.setInt(2, type);
			//属性ID
			smt.setInt(3, field_id);
			int row = smt.executeUpdate();
			con.close();
			if(row==0) return false;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	/**
	 * 属性情報登録.
	 * @param field
	 * @param field_id
	 */
	public static void insertFieldInfo(String field){
		try{
			Connection con = DBManager.getConnection();
			StringBuilder sql = new StringBuilder("insert into field_info_table (");
			sql.append(" field_name");
			sql.append(",upload");
			sql.append(") values (");
			sql.append(" ?");
			sql.append(",NOW()");
			sql.append(");");
			
			PreparedStatement smt = con.prepareStatement(sql.toString());
			smt.setString(1, field);
			smt.executeUpdate();
			con.close();
		}catch(SQLException e){}
	}
	
	/**
	 * 属性ID取得.
	 * @param field_name
	 * @return
	 */
	private static int getFieldId(String field_name){
		int id = -1;
		try{
			Connection con = DBManager.getConnection();
			String sql = "select * from field_info_table where field_name=?";
			PreparedStatement smt = con.prepareStatement(sql);
			smt.setString(1, field_name);
			ResultSet result = smt.executeQuery();
			//登録されていた場合　属性ID取得
			if(result.next()){
				id = result.getInt("field_id");
			}
		}catch(SQLException e){
			
		}
		return id;
	}
	
	/**
	 * DL登録.
	 * @param work
	 * @param cal
	 * @return
	 */
	public static boolean insertDl(Work work, Calendar cal){
		try{
			Connection con = DBManager.getConnection();
			if(con==null) return false;
			StringBuilder sql = new StringBuilder("insert into dl_table_"+work.getDateYM()+" (");
			sql.append(" work_id");
			sql.append(",dl");
			sql.append(",day");
			sql.append(",upload");
			sql.append(") values (");
			sql.append("?");
			sql.append(",?");
			sql.append(",?");
			sql.append(",NOW()");
			sql.append(");");
			PreparedStatement smt = con.prepareStatement(sql.toString());
			
			//カラムカウンタ
			int i = 1;
			//作品ID
			smt.setInt(i++, work.getId());
			//ダウンロード数
			smt.setInt(i++, work.getDl()[0].getDl());
			//対象日
			smt.setDate(i++, new Date(cal.getTimeInMillis()));
			
			int row = smt.executeUpdate();
			con.close();
			if(row==0) return false;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * サークル登録.
	 * @param work
	 * @return
	 */
	public static boolean insertCircle(Work work){
		try{
			Connection con = DBManager.getConnection();
			if(con==null) return false;
			StringBuilder sql = new StringBuilder("insert into circle_table (");
			sql.append(" circle_dlsite_id");
			sql.append(",circle_name");
			sql.append(",circle_site");
			sql.append(",upload");
			sql.append(") values (");
			sql.append(" ?");
			sql.append(",?");
			sql.append(",?");
			sql.append(",CURRENT_DATE");
			sql.append(");");
			
			PreparedStatement smt = con.prepareStatement(sql.toString());
			//カラムカウンタ
			int i = 1;
			//サークルDLSiteID
			smt.setString(i++, work.getCircle().getDLSiteId());
			//サークル名
			smt.setString(i++, work.getCircle().getName());
			//サークルホームページ
			smt.setString(i++, work.getCircle().getSite_url());
			
			int row = smt.executeUpdate();
			con.close();
			if(row==0) return false;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 作品登録.
	 * @param work
	 * @return
	 */
	public static boolean insertWork(Work work){
		try{
			Connection con = DBManager.getConnection();
			if(con==null) return false;
			StringBuilder sql = new StringBuilder("insert into work_table (");
			sql.append(" work_dlsite_id");
			sql.append(",circle_id");
			sql.append(",work_name");
			sql.append(",price");
			sql.append(",dl");
			sql.append(",url");
			sql.append(",exp_long");
			sql.append(",exp_short");
			sql.append(",img_thumb");
			sql.append(",img_main");
			sql.append(",img_sample1");
			sql.append(",img_sample2");
			sql.append(",img_sample3");
			sql.append(",day");
			sql.append(",adult");
			sql.append(",upload");
			sql.append(") values (");
			sql.append(" ?");
			sql.append(",?");
			sql.append(",?");
			sql.append(",?");
			sql.append(",?");
			sql.append(",?");
			sql.append(",?");
			sql.append(",?");
			sql.append(",?");
			sql.append(",?");
			sql.append(",?");
			sql.append(",?");
			sql.append(",?");
			sql.append(",?");
			sql.append(",?");
			sql.append(",NOW()");
			sql.append(");");
			
			PreparedStatement smt = con.prepareStatement(sql.toString());
			
			//カラムカウンタ
			int i = 1;
			//DLSiteID
			smt.setString(i++, work.getDlSiteId());
			//サークルID
			smt.setInt(i++, work.getCircle().getId());
			//作品名
			smt.setString(i++, work.getName());
			//価格
			smt.setInt(i++, work.getPrice());
			//ダウンロード数
			smt.setInt(i++, work.getDl()[0].getDl());
			//作品URL
			smt.setString(i++, work.getUrl());
			//紹介文　長
			smt.setString(i++, work.getExp_long());
			//紹介文　短
			smt.setString(i++, work.getExp_short());
			//サムネイル画像
			smt.setString(i++, work.getImages().getThumb());
			//メイン画像
			smt.setString(i++, work.getImages().getMain());
			//サンプル画像
			for(int j=0; j<3; j++){
				//サンプル画像があれば挿入
				if(work.getImages()!=null && work.getImages().getSamples()!=null && work.getImages().getSamples().length > j){
					smt.setString(i++, work.getImages().getSamples()[j]);
				//なければnull挿入
				}else{
					smt.setString(i++, null);
				}
			}
			//販売開始日
			smt.setDate(i++, work.getDate());
			//アダルトフラグ
			smt.setBoolean(i++, work.getAdult());
			
			//クエリ実行
			int row = smt.executeUpdate();
			con.close();
			if(row==0) return false;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
