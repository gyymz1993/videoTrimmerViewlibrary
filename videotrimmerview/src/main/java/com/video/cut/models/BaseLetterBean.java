package com.video.cut.models;

import java.io.Serializable;

/**
 * Created by guoh on 2018/8/8.
 * 功能描述：
 * 需要的参数：
 */
public class BaseLetterBean implements Serializable{
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     {
     "id":"11233",// 当前字幕的唯一标识
     "te":"123",// 文本信息
     "startTime":1.0, // 开始时间
     "duration":3.0, // 持续时间
     "fn":"微软雅黑", // 字体类型
     "fs":17.0, // 字号大小
     "color":{
     "r":200,
     "g":200,
     "b":200,
     "a":1.0
     },// 颜色信息 ，r g b 为 0 - 255 ， a 为 0 - 1.0
     "pos":{
     "x":0,
     "y":0,
     "w":100,
     "h":100,
     "an":0,
     }// 位置信息 x y 相对于当前视频左上角的位置 ，an为角度信息
     }
     */


    private int id;

    private float startTime;
    private float duration;

    public float getStartTime() {
        return startTime;
    }

    public void setStartTime(float startTime) {
        this.startTime = startTime;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }
}
