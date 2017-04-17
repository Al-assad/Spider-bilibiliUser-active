package main;

import ado.BiliUserDao;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.JsonPathSelector;
import us.codecraft.webmagic.utils.HttpConstant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * create by Intellij IDEA
 * Author: Al-assad
 * E-mail: yulinying_1994@outlook.com
 * Github: https://github.com/Al-assad
 * Date: 2017/4/11 11:54
 * Description: 程序运行的入口；
 *              实现PageProcessor接口，负责目标url的抽取逻辑；
 */

//TODO:ADO层

public class BiliPageProcessor implements PageProcessor{

    //构建Site对象，指定请求头键值字段
    private Site site = Site.me()
            .setRetryTimes(3)
            .setTimeOut(30000)
            .setSleepTime(1800)        //跟据试验，http://space.bilibili.com/ajax/member/GetInfo接口有IP接入限制，估计是60s内上限150次
            .setCycleRetryTimes(3)
            .setUseGzip(true)
            .addHeader("Host","space.bilibili.com")
            .addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0")
            .addHeader("Accept","application/json, text/plain, */*")
            .addHeader("Accept-Language","zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
            .addHeader("Accept-Encoding","gzip, deflate, br")
            .addHeader("X-Requested-With","XMLHttpRequest")
            .addHeader("Content-Type","application/x-www-form-urlencoded")
            .addHeader("Referer","http://space.bilibili.com/10513807/");

    private static final long BEGIN_MID = 2705;    //开始用户mid
    private static final int LIMIT_REQUEST = 5;    //目前b站的用户关注和粉丝请求接口，对page的访问限制数为5

    private BiliUserDao biliUserDao = new BiliUserDao();   //持久化对象
//    private final String TARGET_URL;   //用户信息主页请求接口 post
    private final String FRIENDS_URL = "http://space.bilibili\\.com/ajax/friend/GetAttentionList\\?mid=\\d+&page=\\d+"; //用户关注信息请求接口 get
    private final String FANS_URL = "http://space.bilibili\\.com/ajax/friend/GetFansList\\?mid=\\d+&page=\\d+";   //用户粉丝信息请求接口 get



    @Override
    public void process(Page page) {

        if(page.getUrl().regex(FRIENDS_URL).match() || page.getUrl().regex(FANS_URL).match()){
            /*请求url匹配 friends 和 fans 请求接口时，
          获取 get请求返回json中的mid数据,并添加用户主页请求到url处理队列*/

            List<String> mids = new JsonPathSelector("$.data.list[*].fid").selectList(page.getRawText());
            if (CollectionUtils.isNotEmpty(mids)) {
                for (String mid : mids) {
                    //构造用户信息主页的post请求
                    Request request = createPostRequest(mid);
                    //添加Request对象到URL请求队列
                    page.addTargetRequest(request);
                }
            }

        }else{
            /*请求url为用户主页请求url时，
              通过 post请求返回的json中的目标节点数据，并装载入数据库*/

            HttpClientDownloader da;

            String pageRawText = page.getRawText();
            //跳过连接失败页
            if(new JsonPathSelector("$.status").select(pageRawText).equals("false"))
                page.setSkip(true);
            //使用jsonPath获取json中的有效数据，并装载入BiliUser对象
            BiliUser user = new BiliUser();
            long mid = Long.parseLong(new JsonPathSelector("$.data.mid").select(pageRawText));
            user.setMid(mid);
            user.setName(new JsonPathSelector("$.data.name").select(pageRawText));
            user.setSex(new JsonPathSelector("$.data.sex").select(pageRawText));
            user.setLevel(Integer.parseInt(new JsonPathSelector("$.data.level_info.current_level").select(pageRawText)));
            user.setSign(new JsonPathSelector("$.data.sign").select(pageRawText));
            user.setFaceUrl( new JsonPathSelector("$.data.face").select(pageRawText));
            int friends = Integer.parseInt(new JsonPathSelector("$.data.friend").select(pageRawText));
            user.setFriends(friends);
            int fans = Integer.parseInt(new JsonPathSelector("$.data.fans").select(pageRawText));
            user.setFans(fans);
            user.setPlayNum(Integer.parseInt(new JsonPathSelector("$.data.playNum").select(pageRawText)));
            user.setBirthday(new JsonPathSelector("$.data.birthday").select(pageRawText));
            user.setPlace(new JsonPathSelector("$.data.place").select(pageRawText));

            //添加friends列表请求
            for(int i=1;i<=((friends/20)>LIMIT_REQUEST ? LIMIT_REQUEST : friends/20);i++){
                page.addTargetRequest("http://space.bilibili.com/ajax/friend/GetAttentionList?mid="+mid+"&page="+i);
            }
            //添加fans列表请求
            for(int i=1;i<=((fans/20)>LIMIT_REQUEST ? LIMIT_REQUEST : fans/20);i++){
                page.addTargetRequest("http://space.bilibili.com/ajax/friend/GetFansList?mid="+mid+"&page="+i);
            }

            System.out.println("\n"+user);   //控制台打印已抓取的用户信息
             biliUserDao.saveUser(user);    //保存BiliUser对象到数据库

        }

    }

    @Override
    public Site getSite() {
        return site;
    }

    //创建面向用户主页POST请求（http://space.bilibili.com/ajax/member/GetInfo）的Request对象
    private static Request createPostRequest(String mid){
        //构造post请求数据组和url
        Map<String, Object> nameValuePair = new HashMap<String, Object>();
        NameValuePair[] values = new NameValuePair[1];
        values[0] = new BasicNameValuePair("mid", String.valueOf(mid));
        nameValuePair.put("nameValuePair", values);
        String url = "http://space.bilibili.com/ajax/member/GetInfo?mid="+mid;   //bilibili用户信息获取接口
        //构造Request请求对象
        Request request = new Request(url);
        request.setExtras(nameValuePair);
        request.setMethod(HttpConstant.Method.POST);
        return request;
    }


    //运行主方法
    public static void main(String[] args){

        Spider.create(new BiliPageProcessor())
                .addRequest(createPostRequest(BEGIN_MID+""))    //添加一次对BEGIN_MID主页的POST请求
                .addUrl("http://space.bilibili.com/ajax/friend/GetFansList?mid="+BEGIN_MID+"&page=1")
                .addUrl("http://space.bilibili.com/ajax/friend/GetAttentionList?mid="+BEGIN_MID+"&page=1")
                .setDownloader(new MyDownloader())
                .thread(2)
                .run();



    }
}
