package ado;

import main.BiliUser;

import java.util.ArrayList;
import java.util.List;

/**
 * create by Intellij IDEA
 * Author: Al-assad
 * E-mail: yulinying_1994@outlook.com
 * Github: https://github.com/Al-assad
 * Date: 2017/4/13 14:20
 * Description:  dao持久化层的实现
 */
public class BiliUserDao {

    public int saveUser(BiliUser biliUser){

        DBHelper dbHelper = new DBHelper();
        StringBuffer sql = new StringBuffer();
        //构造模板sql ,当插入主键重复时，忽略新数据
        sql.append("INSERT IGNORE INTO bilibili_user_active(mid,name,sex,level,sign,faceUrl,friends,fans,playNum,birthday,place)")
        .append("VALUES(?,?,?,?,?,?,?,?,?,?,?)");
        //构造模板填充序列
        List<String> sqlValues = new ArrayList<String>();
        sqlValues.add(biliUser.getMid()+"");
        sqlValues.add(biliUser.getName());
        sqlValues.add(biliUser.getSex());
        sqlValues.add(biliUser.getLevel()+"");
        sqlValues.add(biliUser.getSign());
        sqlValues.add(biliUser.getFaceUrl());
        sqlValues.add(biliUser.getFriends()+"");
        sqlValues.add(biliUser.getFans()+"");
        sqlValues.add(biliUser.getPlayNum()+"");
        sqlValues.add(biliUser.getBirthday());
        sqlValues.add(biliUser.getPlace());

        int result = dbHelper.executeUpdate(sql.toString(),sqlValues);
        return result;


    }

}
