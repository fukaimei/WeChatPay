package com.fukaimei.wechatpay.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.fukaimei.wechatpay.bean.GetAccessTokenResult;
import com.fukaimei.wechatpay.bean.LocalRetCode;
import com.fukaimei.wechatpay.bean.WechatConstants;
import com.fukaimei.wechatpay.util.WechatUtil;

public class GetAccessTokenTask extends AsyncTask<String, Void, GetAccessTokenResult> {
    private static final String TAG = "GetAccessTokenTask";
    private Context context;
    private ProgressDialog dialog;
    private String[] goods_info;

    public GetAccessTokenTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context, "提示", "正在获取access token...");
    }

    @Override
    protected GetAccessTokenResult doInBackground(String... params) {
        goods_info = new String[]{params[0], params[1], params[2]};
        GetAccessTokenResult result = new GetAccessTokenResult();
        String url = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                WechatConstants.APP_ID, WechatConstants.APP_SECRET);
        Log.d(TAG, "get access token, url = " + url);

        byte[] buf = WechatUtil.httpGet(url);
        if (buf == null || buf.length == 0) {
            result.localRetCode = LocalRetCode.ERR_HTTP;
            return result;
        }

        String content = new String(buf);
        result.parseFrom(content);
        return result;
    }

    @Override
    protected void onPostExecute(GetAccessTokenResult result) {
        if (dialog != null) {
            dialog.dismiss();
        }

        Log.d(TAG, "RetCode=" + result.localRetCode + ", errCode=" + result.errCode + ", errMsg=" + result.errMsg);
        if (result.localRetCode == LocalRetCode.ERR_OK) {
            Toast.makeText(context, "获取access token成功, accessToken = " + result.accessToken, Toast.LENGTH_LONG).show();
            GetPrepayIdTask getPrepayId = new GetPrepayIdTask(context, result.accessToken);
            getPrepayId.execute(goods_info[0], goods_info[1], goods_info[2]);
        } else {
            Toast.makeText(context, "获取access token失败，原因: " + result.localRetCode.name(), Toast.LENGTH_LONG).show();
        }
    }
}
