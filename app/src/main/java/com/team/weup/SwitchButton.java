package com.team.weup;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class SwitchButton extends FrameLayout {
    //图片属性
    private ImageView openImage;
    private ImageView closeImage;
    //构造函数
    public SwitchButton(Context context){
        this(context,null);
    }
    public SwitchButton(Context context, AttributeSet attrs, int defStyleAttr){
        this(context, attrs);
    }
    public SwitchButton(Context context, AttributeSet attrs){
        super(context,attrs);
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs,R.styleable.SwitchButton);
        Drawable openDrawable = typedArray.getDrawable(R.styleable.SwitchButton_switchOpenImage);
        Drawable closeDrawable = typedArray.getDrawable(R.styleable.SwitchButton_switchCloseImage);
        //0是开，1是关
        int switchStatus = typedArray.getInt(R.styleable.SwitchButton_switchStatus,0);
        typedArray.recycle();
        //绑定布局文件
        LayoutInflater.from(context).inflate(R.layout.switch_button,this);
        //绑定控件id
        openImage = (ImageView)findViewById(R.id.switch_open);
        closeImage = (ImageView)findViewById(R.id.switch_close);
        //处于打开状态
        if(openDrawable != null){
            //加载图片
            openImage.setImageDrawable(openDrawable);
        }
        //处于关闭状态
        if(closeDrawable != null){
            closeImage.setImageDrawable(closeDrawable);
        }
        //判断开关当前状态
        if(switchStatus == 1){
            closeSwitch();
        }
    }

    //判断开关状态
    public boolean isSwitchOpen(){
        return openImage.getVisibility()== View.VISIBLE;
    }

    public void openSwitch(){
        openImage.setVisibility(View.VISIBLE);
        closeImage.setVisibility(View.INVISIBLE);
    }

    public void closeSwitch(){
        openImage.setVisibility(View.INVISIBLE);
        closeImage.setVisibility(View.VISIBLE);
    }


}
