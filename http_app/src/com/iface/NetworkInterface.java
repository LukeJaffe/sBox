package com.iface;

public interface NetworkInterface
{
    public final String SERVER_IP = "76.19.98.37";
    public final String LOGIN_PAGE = "http://76.19.98.37/webservice/login.php";

    public int login(String username, String password);
}