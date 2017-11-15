package com.fukaimei.wechatpay;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.fukaimei.wechatpay.task.GetAccessTokenTask;

public class WxpayActivity extends AppCompatActivity implements OnClickListener {
	private static final String TAG = "WxpayActivity";
	private EditText et_goods_title;
	private EditText et_goods_desc;
	private EditText et_goods_price;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wxpay);
		et_goods_title = (EditText) findViewById(R.id.et_goods_title);
		et_goods_desc = (EditText) findViewById(R.id.et_goods_desc);
		et_goods_price = (EditText) findViewById(R.id.et_goods_price);
		findViewById(R.id.btn_wechat).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_wechat) {
			String title = et_goods_title.getText().toString();
			String desc = et_goods_desc.getText().toString();
			String price = et_goods_price.getText().toString();
			new GetAccessTokenTask(this).execute(title, desc, price);
		}
	}

}
