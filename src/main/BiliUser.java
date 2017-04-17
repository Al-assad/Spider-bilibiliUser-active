package main;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * create by Intellij IDEA
 * Author: Al-assad
 * E-mail: yulinying_1994@outlook.com
 * Github: https://github.com/Al-assad
 * Date: 2017/4/11 11:57
 * Description:  bilibili用户信息类
 */
public class BiliUser {


    private long mid;    //mid
    private String name;   //昵称
    private String sex ;   //性别[“男”，“女”，“保密”]
    private int level;     //等级[1-5]
    private String sign;   //用户签名
    private String faceUrl;   //用户头像图片链接
    private int friends;   //关注数量
    private int fans;      //粉丝数量
    private int playNum;    //上传视频播放量
    private String birthday;   //生日（系统默认项：0000-01-01）
    private String place ;    //所在地点

    public BiliUser(){
    }

    //getter and setter method
    public long getMid() {
        return mid;
    }

    public void setMid(long mid) {
        this.mid = mid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getFaceUrl() {
        return faceUrl;
    }

    public void setFaceUrl(String faceUrl) {
        this.faceUrl = faceUrl;
    }

    public int getFriends() {
        return friends;
    }

    public void setFriends(int friends) {
        this.friends = friends;
    }

    public int getFans() {
        return fans;
    }

    public void setFans(int fans) {
        this.fans = fans;
    }

    public int getPlayNum() {
        return playNum;
    }

    public void setPlayNum(int playNum) {
        this.playNum = playNum;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        //废弃以0000-开头的birthday记录
        if(birthday.equals("0000-01-01"))
            birthday = null;
        this.birthday = birthday;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }


    //toString method
    public String toString(){
        return "mid="+mid+", "
                +"name="+name+", "
                +"level="+level+", "
                +"sex="+sex+", "
                +"sign="+sign+", "
                +"friends="+friends+", "
                +"faceUrl="+faceUrl+", "
                +"fans="+fans+", "
                +"playNum="+playNum+", "
                +"brithday="+birthday+", "
                +"place="+place;
    }








}
