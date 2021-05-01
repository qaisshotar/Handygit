package com.example.handygit.Common;

import com.example.handygit.Model.UserModel;

public class Common {
    public static final String USER_INFO_REFERENCE ="Users" ;
    public static UserModel currentUser;

    public static String buildWelcomeMessage(){
        if (Common.currentUser !=null)
        {
            return new StringBuilder("Welcome")
                    .append(Common.currentUser.getFirstName())
                    .append("")
                    .append(Common.currentUser.getLastName()).toString();



        }
        else
            return "";

    }

}
