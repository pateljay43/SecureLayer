package com.csulb.cscs579.securelayer;

/**
 * Created by JAY on 11/20/15.
 */
public class Constants {
    // commands
    // // upstream
    public static String verify = "verify";
    public static String sign_up = "signup";
    public static String error = "error";
    public static String id = "id";     // user id
    public static String hp = "hp";     // hash of password
    // // down stream
    public static String publickey = "PUBLICKEY";
    public static String dh = "dh";
    public static String g = "dhg";
    public static String p = "dhp";


    // server rsa key
//    public static String publickey = "publickey";

    // session key
    public static String sessionkey = "sessionkey";

}
