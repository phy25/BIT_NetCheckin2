package org.bitnp.netcheckin2.network;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import org.bitnp.netcheckin2.R;
import org.bitnp.netcheckin2.util.MD5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ental_000 on 2015/3/12.
 */
public class LoginHelper {

    private final static String TAG = "LoginHelper";

    public final static int LOGIN_MODE_1 = 0x1, LOGIN_MODE_2 = 0x2, OFFLINE = 0x0;

    private static String username, password;

    private static String uid;

    private static int loginState = OFFLINE;

    private static String responseMessage;

    private static ArrayList<LoginStateListener> listeners = new ArrayList<LoginStateListener>();

    private static Context context;

    static Pattern VALID_UID, VALID_KEEPLIVE_STATUS, VALID_BALANCE, VALID_COMMA;

    public static String[] LOGIN_STATUS = {
            "user_tab_error","username_error" ,"non_auth_error" ,"password_error" ,"status_error",
            "available_error","ip_exist_error","usernum_error" ,"online_num_error","mode_error" ,
            "time_policy_error","flux_error" ,"minutes_error","ip_error" ,"mac_error", "sync_error",
            "login_ok"
    };

    public static String[] LOGIN_MESSAGE;
    public static String[] KEEPLIVE_STATUS = {
            "keeplive_ok", "status_error","available_error","drop_error","flux_error","minutes_error"
    };
    public static String[] KEEPLIVE_MESSAGE;
    public static String[] LOGOUT_STATUS = {
            "user_tab_error", "username_error", "password_error", "logout_ok", "logout_error"
    };
    public static String[] LOGOUT_MESSAGE;

    static{
        VALID_UID = Pattern.compile("^[\\d]+$");
        VALID_KEEPLIVE_STATUS = Pattern.compile("^[\\d]+,[\\d]+,[\\d]+,[\\d]+");
        VALID_BALANCE = Pattern.compile("\"remain_flux\":\"([\\d,\\.]+)M\"");
        VALID_COMMA = Pattern.compile(",");
    }

    public static void setContext(Context mcontext){
        if(context == null){
            context = mcontext;
            LOGIN_MESSAGE = context.getResources().getStringArray(R.array.login_error_messages);
            KEEPLIVE_MESSAGE = context.getResources().getStringArray(R.array.keeplive_error_messages);
            LOGOUT_MESSAGE = context.getResources().getStringArray(R.array.logout_error_messages);
        }
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

    public static boolean unRegisterListener(LoginStateListener listener){
        return listeners.remove(listener);
    }

    public static void asyncLogin(){
        if(username == null || password == null || username.isEmpty() || password.isEmpty())
            return ;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "try to login");
                loginState = LOGIN_MODE_1;
                if(login2()){
                    Log.v(TAG, getLoginState2());
                    loginState = LOGIN_MODE_2;
                    responseMessage = context.getResources().getString(R.string.login_toast_success_mode2);
                } else if((responseMessage.length() != 0) && (!responseMessage.contains("err_code"))) {
                }
                if(!login1())
                    responseMessage = findMessage(responseMessage, LOGIN_STATUS, LOGIN_MESSAGE);
                updateInfo();
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
                    if(logout1()) {
                        loginState = OFFLINE;
                        updateInfo();
                    }
                } else {
                    if(logout2()) {
                        loginState = OFFLINE;
                        updateInfo();
                    }
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
                    responseMessage = "LOGOUT_OK";
                }
                updateInfo();
            }
        }).start();
    }

    public static void asyncGetState(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                responseMessage = getLoginState2();

                updateInfo();
            }
        }).start();


    }

    private static void updateInfo(){
        if(!responseMessage.equals(""))
            for(LoginStateListener i:listeners){
                i.onLoginStateChanged(responseMessage, loginState);
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
            responseMessage = context.getResources().getString(R.string.login_toast_success_mode1);
            return true;
        } else {
            responseMessage = findMessage(res, LOGIN_STATUS, LOGIN_MESSAGE);
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
            responseMessage = res;
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
            responseMessage = findMessage(res, LOGOUT_STATUS, LOGOUT_MESSAGE);
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
            responseMessage = res;
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
            responseMessage = findMessage(res, KEEPLIVE_STATUS, KEEPLIVE_MESSAGE);
            return false;
        }
    }

    private static boolean forceLogout(){
        String url = "http://10.0.0.55/cgi-bin/force_logout";
        String res = HttpRequest.sendPost(url, "username="+ username +"&password="+ password +"&drop=" + "0" + "&type=1&n=1");
        responseMessage = findMessage(res, LOGOUT_STATUS, LOGOUT_MESSAGE);
        return res.equals("logout_ok");
    }

    private static String getLoginState2(){
        return HttpRequest.sendPost("http://10.0.0.55/cgi-bin/rad_user_info", "");
    }


    /**
     * @return unit is GB
     * */
    public static float getBalance(String uid){
        String response = "";
        response = HttpRequest.sendGet("http://10.0.0.55/user_online.php", "");
        if(response.length() > 5) {
            Matcher matcher = VALID_BALANCE.matcher(response);
            if(matcher.find()){
                response = matcher.group(1);
            }
            matcher = VALID_COMMA.matcher(response);
            response = matcher.replaceAll("");
            float result;
            try {
                result = Float.parseFloat(response);
                Log.i(TAG, "Get balance " + result);
                result /= 1000; // the input is M
                // TODO: Don't know what will happen when balance is not enough
            }
            catch(NumberFormatException e){
                result = 0;
            }
            return result;
        }
        Log.e(TAG, "Can't get balance");
        return 0;
    }

    public static String getresponseMessage(){
        return responseMessage;
    }

    public static String getUid() {
        return uid;
    }

}
