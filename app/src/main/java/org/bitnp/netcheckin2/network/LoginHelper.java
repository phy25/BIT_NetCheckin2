package org.bitnp.netcheckin2.network;

import android.os.Handler;
import android.os.Message;

import org.bitnp.netcheckin2.util.LoginStateListener;
import org.bitnp.netcheckin2.util.MD5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ental_000 on 2015/3/12.
 */
public class LoginHelper {

    String TAG = "LoginHelper";

    private static int LOGIN_MODE_1 = 0x1, LOGIN_MODE_2 = 0x2, OFFLINE = 0x0;

    private static String username, password;

    private static String uid;

    private static int loginState = OFFLINE;

    private static String errorMessage;

    private static ArrayList<LoginStateListener> listeners = new ArrayList<LoginStateListener>();

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

    public static void setAccount(String u, String p){
        username = u;
        password = p;
    }

    public static void reset(){
        loginState = OFFLINE;

    }

    public static boolean registerListener(LoginStateListener listener){
        return listeners.add(listener);
    }

    public static boolean unRegisterLisener(LoginStateListener listener){
        return listeners.remove(listener);
    }

    public static void asyncLogin(){
        if(username == null || password == null || username.isEmpty() || password.isEmpty())
            return ;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(login2()){
                    getLoginState2();
                    loginState = LOGIN_MODE_2;
                    errorMessage = "登录成功";
                    updateInfo();
                } else if((errorMessage.length() != 0) && (!errorMessage.contains("err_code"))) {
                    updateInfo();
                } else {
                    if(login1()) {
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

                        Message msg = new Message();
                        msg.obj = errorMessage;
                        updateInfo();

                    } else {
                        Message msg = new Message();
                        msg.obj = findMessage(errorMessage, LOGIN_STATUS, LOGIN_MESSAGE);
                        updateInfo();
                    }
                }
            }
        }).start();
    }

    public static void asyncLogout(){
        if(loginState == 0)
            return ;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(loginState == LOGIN_MODE_1){
                    if(logout1()){
                        loginState = OFFLINE;
                        Message msg = new Message();
                        msg.obj = "注销成功";
                        updateInfo();
                    } else {
                        Message msg = new Message();
                        msg.obj = errorMessage;
                        updateInfo();
                    }
                } else {
                    if(logout2()){
                        loginState = OFFLINE;
                    }
                    Message msg = new Message();
                    msg.obj = errorMessage;
                    updateInfo();
                }
            }
        }).start();
    }

    public static void asyncForceLogout(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                if(forceLogout()){
                    loginState = OFFLINE;
                }
                Message msg = new Message();
                msg.obj = errorMessage;
                updateInfo();
            }
        }).start();
    }

    private static void updateInfo(){
        for(LoginStateListener i:listeners){
            i.onLoginStateChanged(errorMessage, loginState);
        }
    }

    private static String findMessage(String s, String[] status, String[] message) {
        for(int i = 0; i < status.length; i++) {
            if(s.equals(status[i])) {
                return message[i];
            }
        }
        return s;
    }

    private static boolean login1(){
        String url = "http://10.0.0.55/cgi-bin/do_login";
        String param = "username=" + username + "&password=" + MD5.getMD516(password).toLowerCase() + "&drop=" + "0" + "&type=1&n=100";
        String res = HttpRequest.sendPost(url, param);

        Matcher matcher = VALID_UID.matcher(res);
        if(matcher.matches()){
            uid = res;
            //this.loginState = LOGIN_MODE_1;
            errorMessage = "认证成功";
            return true;
        } else {
            errorMessage = findMessage(res, LOGIN_STATUS, LOGIN_MESSAGE);
            return false;
        }
    }

    private static boolean login2(){
        String url = "http://10.0.0.55/cgi-bin/srun_portal";
        String param = "action=login&username=" + username + "&password=" + password+"&ac_id=8&type=1&wbaredirect=&mac=&user_ip=";
        String res = HttpRequest.sendPost(url, param);

        if(res.contains("login_ok")||res.contains("help.html")){
            return true;
        } else {
            errorMessage = res;
            return false;
        }
    }

    private static boolean logout1(){
        if(loginState == LOGIN_MODE_1 && uid.length() > 0){
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

    private static boolean logout2(){
        if(loginState == LOGIN_MODE_2) {
            String url = "http://10.0.0.55/cgi-bin/srun_portal";
            String param = "action=logout";
            String res = HttpRequest.sendPost(url, param);
            errorMessage = res;
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

    private static boolean keeplive1(){
        String url = "http://10.0.0.55/cgi-bin/keeplive";
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("uid",uid);
        String res = HttpRequest.sendPost("http://10.0.0.55/cgi-bin/keeplive", params);
        Matcher matcher = VALID_KEEPLIVE_STATUS.matcher(res);
        if(matcher.matches())
            return true;
        else{
            errorMessage = findMessage(res, KEEPLIVE_STATUS, KEEPLIVE_MESSAGE);
            return false;
        }
    }

    private static boolean forceLogout(){
        String url = "http://10.0.0.55/cgi-bin/force_logout";
        String res = HttpRequest.sendPost(url, "username="+ username +"&password="+ password +"&drop=" + "0" + "&type=1&n=1");
        errorMessage = findMessage(res, LOGOUT_STATUS, LOGOUT_MESSAGE);
        return res.equals("logout_ok");
    }

    private static String getLoginState2(){
        return HttpRequest.sendPost("http://10.0.0.55/cgi-bin/rad_user_info", "");
    }

    public static int getLoginState(){
        return loginState;
    }

    public static String getErrorMessage(){
        return errorMessage;
    }
    
    public static boolean isAutoLogin(String SSID){
        // TODO check Auto Login SSID
        return true;
    }

}