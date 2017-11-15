package com.fukaimei.wechatpay.adapter;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.fukaimei.wechatpay.R;
import com.fukaimei.wechatpay.bean.ShareChanels;
import com.fukaimei.wechatpay.util.CacheUtil;
import com.tencent.connect.common.Constants;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.open.t.Weibo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class ShareGridAdapter extends BaseAdapter implements OnItemClickListener {
	private final static String TAG = "ShareGridAdapter";
	private LayoutInflater mInflater;
	private static Context mContext;
	private Handler mHandler;
	private String mUrl;
	private String mTitle;
	private static String mContent;
	private static String mImageUrl;
	private ArrayList<ShareChanels> mChannelList;

	private final String QQ_APPID = "100330589"; // 这里替换为开发者在QQ互联平台申请的应用id
	private static Tencent mTencent;
	private static Weibo mWeibo = null;
	private final String WX_APPID = "wxd930ea5d5a258f4f"; // 这里替换为开发者在微信开放平台申请的应用id
	private IWXAPI mWeixinApi;

	private int WEIXIN = 0;
	private int CIRCLE = 1;
	private int QQ = 2;
	private int QZONE = 3;
	private int WEIBO = 4;

	private int[] mShareIcons = { R.drawable.logo_wechat,
			R.drawable.logo_wechatcircle, R.drawable.logo_qq,
			R.drawable.logo_qzone, R.drawable.logo_tencentweibo };

	public ShareGridAdapter(final Context context, Handler handler, String url,
			String title, String content, final String imageUrl,
			ArrayList<ShareChanels> channelList) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
		mHandler = handler;
		mUrl = url;
		mTitle = title;
		mContent = content;
		if (imageUrl != null && Patterns.WEB_URL.matcher(imageUrl).matches()) {
			new Thread(new Runnable() {
				public void run() {
					try {
						mImageUrl = CacheUtil.getImagePath(imageUrl, CacheUtil.getFileCache(context));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		} else {
			mImageUrl = imageUrl;
		}

		if (channelList == null) {
			mChannelList = new ArrayList<ShareChanels>();
			mChannelList.add(new ShareChanels("微信好友", WEIXIN));
			mChannelList.add(new ShareChanels("微信朋友圈", CIRCLE));
			mChannelList.add(new ShareChanels("QQ好友", QQ));
			mChannelList.add(new ShareChanels("QQ空间", QZONE));
			mChannelList.add(new ShareChanels("腾讯微博", WEIBO));
		} else {
			mChannelList = channelList;
		}

		// 创建一个QQ实例，用于QQ分享、QQ空间分享、腾讯微博分享
		mTencent = Tencent.createInstance(QQ_APPID, mContext);

		// 将该app注册到微信，用于微信分享、微信朋友圈分享
		mWeixinApi = WXAPIFactory.createWXAPI(mContext, null);
		mWeixinApi.registerApp(WX_APPID);
		mWeixinApi = WXAPIFactory.createWXAPI(mContext, WX_APPID);
	}

	@Override
	public int getCount() {
		return mChannelList.size();
	}

	@Override
	public Object getItem(int position) {
		return mChannelList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.item_share, null);
			holder.tv_share_name = (TextView) convertView.findViewById(R.id.tv_share_name);
			holder.iv_share_icon = (ImageView) convertView.findViewById(R.id.iv_share_icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ShareChanels item = mChannelList.get(position);
		holder.tv_share_name.setText(item.channelName);
		holder.tv_share_name.setPadding(0, 0, 0, 0);
		holder.iv_share_icon.setImageResource(mShareIcons[item.channelType]);
		return convertView;
	}

	public final class ViewHolder {
		public TextView tv_share_name;
		public ImageView iv_share_icon;
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		ShareChanels item = mChannelList.get(arg2);
		mHandler.sendEmptyMessageDelayed(0, 1500);
		if (item.channelType == WEIXIN) {
			SendMessageToWX.Req req = new SendMessageToWX.Req();
			// transaction字段用于唯一标识一个请求
			req.transaction = "wx_share" + System.currentTimeMillis();
			req.message = getWXMessage();
			req.scene = SendMessageToWX.Req.WXSceneSession;
			mWeixinApi.sendReq(req);
		} else if (item.channelType == CIRCLE) {
			SendMessageToWX.Req req = new SendMessageToWX.Req();
			req.transaction = "wx_share" + System.currentTimeMillis();
			req.message = getWXMessage();
			req.scene = SendMessageToWX.Req.WXSceneTimeline;
			mWeixinApi.sendReq(req);
		} else if (item.channelType == QQ) {
			mShareListener = new ShareQQListener(mContext, item.channelName);
			Bundle params = new Bundle();
			params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
			params.putString(QQShare.SHARE_TO_QQ_TITLE, mTitle);
			params.putString(QQShare.SHARE_TO_QQ_SUMMARY, mContent);
			params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, mUrl);
			params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, mImageUrl);
			params.putString(QQShare.SHARE_TO_QQ_APP_NAME, mContext.getPackageName());
			mTencent.shareToQQ((Activity) mContext, params, mShareListener);
		} else if (item.channelType == QZONE) {
			mShareListener = new ShareQQListener(mContext, item.channelName);
			ArrayList<String> urlList = new ArrayList<String>();
			urlList.add(mImageUrl);
			Log.d(TAG, "mImageUrl=" + mImageUrl);
			Bundle params = new Bundle();
			params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
			params.putString(QzoneShare.SHARE_TO_QQ_TITLE, mTitle);
			params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, mContent);
			params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, mUrl);
			params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, urlList);
			Log.d(TAG, "begin shareToQzone");
			mTencent.shareToQzone((Activity) mContext, params, mShareListener);
			Log.d(TAG, "end shareToQzone");
		} else if (item.channelType == WEIBO) {
			// 腾讯微博分享需要QQ登录授权
			mTencent.login((Activity) mContext, "all", mLoginListener);
		}
	}

	private int THUMB_SIZE = 150;

	private WXMediaMessage getWXMessage() {
		WXMediaMessage msg = new WXMediaMessage();
		if (!TextUtils.isEmpty(mTitle) && TextUtils.isEmpty(mImageUrl)) {
			// 分享文本消息
			WXTextObject textObj = new WXTextObject();
			textObj.text = mContent;
			msg.mediaObject = textObj;
			msg.title = mTitle;
			msg.description = mContent;
		} else if (TextUtils.isEmpty(mTitle) && !TextUtils.isEmpty(mImageUrl)) {
			// 分享图片消息
			// WXImageObject imgObj = new WXImageObject();
			// imgObj.setImagePath(mImageUrl);
			Bitmap bmp = BitmapFactory.decodeFile(mImageUrl);
			WXImageObject imgObj = new WXImageObject(bmp);
			msg.mediaObject = imgObj;
			Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
			msg.thumbData = CacheUtil.bmpToByteArray(thumbBmp, true); // 设置缩略图
		} else if (!TextUtils.isEmpty(mTitle) && !TextUtils.isEmpty(mImageUrl)) {
			// 分享图文消息
			WXWebpageObject webpage = new WXWebpageObject();
			webpage.webpageUrl = mUrl;
			msg.title = mTitle;
			msg.description = mContent;
			msg.mediaObject = webpage;
			Log.d(TAG, "mImageUrl="+mImageUrl);
			Bitmap bmp = BitmapFactory.decodeFile(mImageUrl);
			Log.d(TAG, "bitmap getByteCount="+bmp.getByteCount());
			Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
			msg.thumbData = CacheUtil.bmpToByteArray(thumbBmp, true); // 设置缩略图
		}
		return msg;
	}

	public static final int REQUEST_ADD_PIC_T = 1001;
	public static IUiListener mLoginListener = new IUiListener() {

		@Override
		public void onComplete(Object object) {
			Log.d(TAG, "登录完成:" + object.toString());
			try {
				JSONObject jsonObject = (JSONObject) object;
				String token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
				String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
				String openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
				if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires) && !TextUtils.isEmpty(openId)) {
					mTencent.setAccessToken(token, expires);
					mTencent.setOpenId(openId);
					mWeibo = new Weibo(mContext, mTencent.getQQToken());
					if (mImageUrl == null || mImageUrl.length() <= 0) {
						mWeibo.sendText(mContent, new ShareQQListener(mContext, "微博文字"));
					} else {
						mWeibo.sendPicText(mContent, mImageUrl, new ShareQQListener(mContext, "微博图文"));
					}
				}
			} catch (Exception e) {
				Log.d(TAG, "登录异常:" + e.getMessage());
			}
		}

		@Override
		public void onError(UiError error) {
			Log.d(TAG, "登录失败:" + error.errorMessage);
		}

		@Override
		public void onCancel() {
			Log.d(TAG, "登录取消");
		}
	};

	public static ShareQQListener mShareListener;

	private static class ShareQQListener implements IUiListener {
		private Context context;
		private String channelName;

		public ShareQQListener(final Context context, final String channelName) {
			this.context = context;
			this.channelName = channelName;
		}

		@Override
		public void onComplete(Object object) {
			Toast.makeText(context, channelName + "分享完成:" + object.toString(), Toast.LENGTH_LONG).show();
		}

		@Override
		public void onError(UiError error) {
			Toast.makeText(context, channelName + "分享失败:" + error.errorMessage, Toast.LENGTH_LONG).show();
		}

		@Override
		public void onCancel() {
			Toast.makeText(context, channelName + "分享取消", Toast.LENGTH_LONG).show();
		}
	}

}
