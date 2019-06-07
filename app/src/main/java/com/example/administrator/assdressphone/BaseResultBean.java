package com.example.administrator.assdressphone;

import java.util.List;

public class BaseResultBean {

    private String mobile;
    private String name;
    private String identifier;
    private String phone_type;
    private List<PhoneBean> data;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPhone_type() {
        return phone_type;
    }

    public void setPhone_type(String phone_type) {
        this.phone_type = phone_type;
    }

    public List<PhoneBean> getData() {
        return data;
    }

    public void setData(List<PhoneBean> data) {
        this.data = data;
    }
}
