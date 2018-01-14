package com.example.navitest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {

    private Button naviButton;//负责打开导航Activity
    private Button mapButton;//负责打开地图Activity

    private ProgressDialog progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        naviButton = (Button) findViewById(R.id.navibutton);
        naviButton.setOnClickListener(this);

        mapButton = (Button) findViewById(R.id.locationbutton);
        mapButton.setOnClickListener(this);
    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if(progressDialog!=null){
            progressDialog.dismiss();
        }

    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.navibutton:
                showProgressDialog("提示", "正在加载中，请稍后");
                Intent intent1 = new Intent(MainActivity.this, BasicMapActivity.class);
                startActivity(intent1);
                break;
            case R.id.locationbutton:
                showProgressDialog("提示", "正在加载中，请稍后");
                Intent intent2 = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent2);
                break;
            default:
                break;
        }
    }

    public void showProgressDialog(String _title, String _message) {
        progressDialog = ProgressDialog.show(MainActivity.this, _title, _message);
    }

}
