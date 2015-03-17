package org.bitnp.netcheckin2.network;

import android.os.Handler;
import android.os.Message;

import org.bitnp.netcheckin2.util.MD5;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ental_000 on 2015/3/12.
 */
public class LoginHelper {

    String TAG = "LoginHelper";

    private static int LOGIN_MODE_1 = 0x1, LOGIN_MODE_2 = 0x2, OFFLINE = 0x0;

    private String username, password;

    private String uid;

    private int loginState = OFFLINE;

    private String errorMessage;

    private Handler handler;

    static Pattern VALID_UID, VALID_KEEPLIVE_STATUS;

    public static String[] LOGIN_STATUS = {
            "user_tab_error","username_error" ,"non_auth_error" ,"password_error" ,"status_error",
            "available_error","ip_exist_error","usernum_error" ,"online_num_error","mode_error" ,
            "time_policy_error","flux_error" ,"minutes_error","ip_error" ,"mac_error", "sync_error",
            "login_ok"
    };
    public static String[] LOGIN_MESSAGE = {
            "认证程序未启动","用户名错误","您无须认证，可直接上网","密码错误","用户已欠费，请尽快充值。",
            "用户已禁用","您的IP尚未下线，请等待2分钟再试。","用户数已达上限","该帐号的登录人数已超过限额\n如果怀疑帐号被盗用，请联系管理员。",
            "系统已禁止WEB方式登录，请使用客户端","当前时段不允许连接","您的流量已超支","您的时长已超支",
            "您的IP地址不合法","您的MAC地址不合法","您的资料已修改，正在等待同步，请2分钟后再试。",
            "认证成功"
    };
    public static String[] KEEPLIVE_STATUS = {
            "keeplive_ok", "status_error","available_error","drop_error","flux_error","minutes_error"
    };
    public static String[] KEEPLIVE_MESSAGE = {
            "登录成功", "您的帐户余额不足","您的帐户被禁用","您被强制下线","您的流量已超支","minutes_error"
    };
    public static String[] LOGOUT_STATUS = {
            "user_tab_error", "username_error", "password_error", "logout_ok", "logout_error"
    };
    public static String[] LOGOUT_MESSAGE = {
            "认证程序未启动", "用户名错误", "密码错误", "注销成功,请等1分钟后登录", "您不在线上"
    };

    static{
        VALID_UID = Pattern.compile("^[\\d]+$");
        VALID_KEEPLIVE_STATUS = Pattern.compile("^[\\d]+,[\\d]+,[\\d]+,[\\d]+");
    }

    public void setAccount(String username, String password){
        this.username = username;
        this.password = password;
    }

    public void reset(){
        this.loginState = OFFLINE;
    }

    public void setHandler(Handler handler){
        this.handler = handler;
    }

    public void asyncLogin(){
        if(username == null || password == null || username.isEmpty() || password.isEmpty())
            return ;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(login2()){
                    getLoginState2();

                    Message msg = new Message();
                    msg.obj = "登录成功";
                    handler.sendMessage(msg);
                    loginState = LOGIN_MODE_2;
                } else if((!(errorMessage.length() == 0)) && (!(errorMessage.contains("err_code")))) {
                    Message msg = new Message();
                    msg.obj = errorMessage;
                    handler.sendMessage(msg);
                } else {
                    if(login1()) {
                        Message msg = new Message();
                        msg.obj = errorMessage;
                        handler.sendMessage(msg);

                        //FIXME: the pattern of keeplive status might be incorrect
                        /*
                        int i = 0;
                        for(i = 0; i < 5; i++){
                            if(!keeplive1()){
                                Message msg1 = new Message();
                                msg1.obj = errorMessage;
                                handler.sendMessage(msg1);
                                break;
                            }
                        }
                        */
                        loginState = LOGIN_MODE_1;

                    } else {
                        Message msg = new Message();
                        msg.obj = findMessage(errorMessage, LOGIN_STATUS, LOGIN_MESSAGE);
                        handler.sendMessage(msg);
                    }
                }
            }
        }).start();
    }

    public void asyncLogout(){
        if(loginState == 0)
            return ;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(loginState == LOGIN_MODE_1){
                    if(logout1()){
                        Message msg = new Message();
                        msg.obj = "注销成功";
                        handler.sendMessage(msg);
                        loginState = OFFLINE;
                    } else {
                        Message msg = new Message();
                        msg.obj = errorMessage;
                        handler.sendMessage(msg);
                    }
                } else {
                    if(logout2()){
                        loginState = OFFLINE;
                    }
                    Message msg = new Message();
                    msg.obj = errorMessage;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }

    public void asyncForceLogout(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                if(forceLogout()){
                    loginState = OFFLINE;
                }
                Message msg = new Message();
                msg.obj = errorMessage;
                handler.sendMessage(msg);
            }
        }).start();
    }

    private String findMessage(String s, String[] status, String[] message) {
        for(int i = 0; i < status.length; i++) {
            if(s.equals(status[i])) {
                return message[i];
            }
        }
        return s;
    }

    private boolean login1(){
        String url = "http://10.0.0.55/cgi-bin/do_login";
        String param = "username=" + username + "&password=" + MD5.getMD516(password).toLowerCase() + "&drop=" + "0" + "&type=1&n=100";
        String res = HttpRequest.sendPost(url, param);

        Matcher matcher = VALID_UID.matcher(res);
        if(matcher.matches()){
            uid = res;
            //this.loginState = LOGIN_MODE_1;
            this.errorMessage = "认证成功";
            return true;
        } else {
            this.errorMessage = findMessage(res, LOGIN_STATUS, LOGIN_MESSAGE);
            return false;
        }
    }

    private boolean login2(){
        String url = "http://10.0.0.55/cgi-bin/srun_portal";
        String param = "action=login&username=" + username + "&password=" + password+"&ac_id=8&type=1&wbaredirect=&mac=&user_ip=";
        String res = HttpRequest.sendPost(url, param);

        if(res.contains("login_ok")||res.contains("help.html")){
            return true;
        } else {
            this.errorMessage = res;
            return false;
        }
    }

    private boolean logout1(){
        if(this.loginState == LOGIN_MODE_1 && uid.length() > 0){
            String url = "http://10.0.0.55/cgi-bin/do_logout";
            HashMap<String,String> params = new HashMap<String,String>();
            params.put("uid",uid);
            String res = HttpRequest.sendPost(url, params);
            System.out.println(res);
            errorMessage = findMessage(res, LOGOUT_STATUS, LOGOUT_MESSAGE);
            if(res.equals("logout_ok")){
                uid = "";
                return true;
            } else {
                return false;
            }
        } else
            return false;
    }

    private boolean logout2(){
        if(this.loginState == LOGIN_MODE_2) {
            String url = "http://10.0.0.55/cgi-bin/srun_portal";
            String param = "action=logout";
            String res = HttpRequest.sendPost(url, param);
            this.errorMessage = res;
            if (res.contains("注销成功")) {
                //this.loginState = OFFLINE;
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean keeplive1(){
        String url = "http://10.0.0.55/cgi-bin/keeplive";
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("uid",uid);
        String res = HttpRequest.sendPost("http://10.0.0.55/cgi-bin/keeplive", params);
        Matcher matcher = VALID_KEEPLIVE_STATUS.matcher(res);
        if(matcher.matches())
            return true;
        else{
            this.errorMessage = findMessage(res, KEEPLIVE_STATUS, KEEPLIVE_MESSAGE);
            return false;
        }
    }

    private boolean forceLogout(){
        String url = "http://10.0.0.55/cgi-bin/force_logout";
        String res = HttpRequest.sendPost(url, "username="+ username +"&password="+ password +"&drop=" + "0" + "&type=1&n=1");
        this.errorMessage = findMessage(res, LOGOUT_STATUS, LOGOUT_MESSAGE);
        return res.equals("logout_ok");
    }

    private String getLoginState2(){
        return HttpRequest.sendPost("http://10.0.0.55/cgi-bin/rad_user_info", "");
    }

    public int getLoginState(){
        return loginState;
    }

    public String getErrorMessage(){
        return errorMessage;
    }
    
    public boolean isAutoLogin(String SSID){
        // TODO check Auto Login SSID
        return true;
    }
}
