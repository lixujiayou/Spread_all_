package com.wangdao.our.spread_2.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.wangdao.our.spread_2.ExampleApplication;
import com.wangdao.our.spread_2.R;
import com.wangdao.our.spread_2.activity_.Article_info;
import com.wangdao.our.spread_2.activity_.LoginActivity;
import com.wangdao.our.spread_2.bean.MyArticle;
import com.wangdao.our.spread_2.slide_widget.AllUrl;
import com.wangdao.our.spread_2.slide_widget.shap_imageview.CustomImageView;
import com.wangdao.our.spread_2.slide_widget.shap_imageview.XCRoundRectImageView;
import com.wangdao.our.spread_2.slide_widget.widget_image.AsynImageLoader;
import com.wangdao.our.spread_2.slide_widget.widget_m.SwipeMenuListView;
import com.wangdao.our.spread_2.widget_pull.PullToRefreshBase;
import com.wangdao.our.spread_2.widget_pull.PullToRefreshScrollView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/4/21 0021.
 */
public class FragmentStatistics extends Fragment implements View.OnClickListener{


    private  TextView tvQ_1,tvQ_2,tvQ_3,tvQ_4;
    private HttpPost httpPost;
    private HttpResponse httpResponse = null;
    private List<NameValuePair> params = new ArrayList<NameValuePair>();
    private AllUrl allurl = new AllUrl();

    private ListView fs_Pull_ListView;
    private PullToRefreshScrollView fs_pull;
    private Context myContext;
    private View myView;
    private LayoutInflater myInflater;
    private fs_Adapter myFsAdapter;
    private List<MyArticle> allArticle = new ArrayList<>();
    //private ListView actualListView;
    private RefreshType mRefreshType = RefreshType.LOAD_MORE;
    private String lastItemTime;
    private boolean pullFromUser;
    private int currentType = 1;
    public enum RefreshType{
        REFRESH,LOAD_MORE
    }

    private String title_cu;
   // private String myIdd;

    private final String myUrl = "http://hmyx.ijiaque.com/app/article/articledetail.html";
    private String myCurrentId;
    private String myIdd;
    private TextView tvnull;

    private FsHandler fhandler = new FsHandler();
    private String mImgUrl;

    private NetBroadcast netBroadcast;
    private IntentFilter intentFilter;
    private LinearLayout ll_nowifi;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_statistics,null);
        myInflater = inflater;
        myContext = this.getActivity();
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        netBroadcast = new NetBroadcast();
        myContext.registerReceiver(netBroadcast, intentFilter);

        initView();
        initListView();
        myFsAdapter = new fs_Adapter(allArticle);

        fs_Pull_ListView.setAdapter(myFsAdapter);
    //    initData("spread");
        setListViewHeightBasedOnChildren(fs_Pull_ListView);

        fs_Pull_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                myCurrentId = allArticle.get(position).getId();
                myIdd = allArticle.get(position).getIid();
                mImgUrl = allArticle.get(position).getIconUrl();
                title_cu = allArticle.get(position).getTitle();
                showDeleteDialog();
            }
        });
        return myView;
    }

    private class NetBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            ConnectivityManager connectionManager =
                    (ConnectivityManager) myContext.getSystemService(context.CONNECTIVITY_SERVICE);
            NetworkInfo netinfo = connectionManager.getActiveNetworkInfo();
            if(netinfo!=null&&netinfo.isAvailable()){
                initData("spread");
                ll_nowifi.setVisibility(View.GONE);
            }else{
                ll_nowifi.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 展示删除dialog
     */
    private Dialog deleteDialog;
    private View dialog_view;
    private TextView tv_dialog_delete,tv_dialog_cancle,tv_dialog_look;
    private void showDeleteDialog(){
        dialog_view = myInflater.inflate(R.layout.dialog_delete_my,null);
        tv_dialog_delete = (TextView) dialog_view.findViewById(R.id.dialog_delete_my_tv_delete);
        tv_dialog_look = (TextView) dialog_view.findViewById(R.id.dialog_delete_my_tv_look);
        tv_dialog_cancle = (TextView) dialog_view.findViewById(R.id.dialog_delete_my_tv_cancle);

        deleteDialog = new Dialog(myContext,R.style.dialog);
        deleteDialog.setContentView(dialog_view);
        deleteDialog.show();
        tv_dialog_delete.setOnClickListener(this);
        tv_dialog_look.setOnClickListener(this);
        tv_dialog_cancle.setOnClickListener(this);
    }

    private void initView(){

        ll_nowifi = (LinearLayout) myView.findViewById(R.id.fragment_statistics_ll_nowifi);
        fs_Pull_ListView  = (ListView) myView.findViewById(R.id.fragment_statistics_listview);
        fs_pull = (PullToRefreshScrollView) myView.findViewById(R.id.fragment_statistics_pull);
        tvnull = (TextView) myView.findViewById(R.id.fragment_statistics_tvnull);

        tvQ_1 = (TextView) myView.findViewById(R.id.fragment_statistics_tv_1);
        tvQ_2 = (TextView) myView.findViewById(R.id.fragment_statistics_tv_2);
        tvQ_3 = (TextView) myView.findViewById(R.id.fragment_statistics_tv_3);
        tvQ_4 = (TextView) myView.findViewById(R.id.fragment_statistics_tv_4);

        tvQ_1.setOnClickListener(this);
        tvQ_2.setOnClickListener(this);
        tvQ_3.setOnClickListener(this);
        tvQ_4.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fragment_statistics_tv_1:
                tvQ_1.setTextColor(getResources().getColor(R.color.colorPrimary));
                tvQ_2.setTextColor(getResources().getColor(R.color.textcolor_hui));
                tvQ_3.setTextColor(getResources().getColor(R.color.textcolor_hui));
                tvQ_4.setTextColor(getResources().getColor(R.color.textcolor_hui));
                initData("spread");
                currentType = 1;
                break;
            case R.id.fragment_statistics_tv_2:
                tvQ_2.setTextColor(getResources().getColor(R.color.colorPrimary));
                tvQ_1.setTextColor(getResources().getColor(R.color.textcolor_hui));
                tvQ_3.setTextColor(getResources().getColor(R.color.textcolor_hui));
                tvQ_4.setTextColor(getResources().getColor(R.color.textcolor_hui));
                initData("xposed");
                currentType =2;
                break;
            case R.id.fragment_statistics_tv_3:
                tvQ_3.setTextColor(getResources().getColor(R.color.colorPrimary));
                tvQ_2.setTextColor(getResources().getColor(R.color.textcolor_hui));
                tvQ_1.setTextColor(getResources().getColor(R.color.textcolor_hui));
                tvQ_4.setTextColor(getResources().getColor(R.color.textcolor_hui));
                initData("view");
                currentType = 3;
                break;

            case R.id.fragment_statistics_tv_4:
                tvQ_4.setTextColor(getResources().getColor(R.color.colorPrimary));
                tvQ_2.setTextColor(getResources().getColor(R.color.textcolor_hui));
                tvQ_3.setTextColor(getResources().getColor(R.color.textcolor_hui));
                tvQ_1.setTextColor(getResources().getColor(R.color.textcolor_hui));
                initData("time");
                currentType  = 4;
                break;
            case R.id.dialog_delete_my_tv_delete:
                deleteTong(myIdd);
                deleteDialog.dismiss();
                break;

            case R.id.dialog_delete_my_tv_look:
                SharedPreferences sharedPreferences = myContext.getSharedPreferences("user", myContext.MODE_PRIVATE);
                String auid = sharedPreferences.getString("uid", "");

                Log.i("qqqqq", myUrl + "?writing_id=" + myCurrentId);

                Intent lookIntent = new Intent(myContext, Article_info.class);
                lookIntent.putExtra("url", myUrl + "?writing_id=" + myCurrentId+"&uid="+auid);
                lookIntent.putExtra("uid", myCurrentId);
                lookIntent.putExtra("img",mImgUrl);
                lookIntent.putExtra("title",title_cu);
                startActivity(lookIntent);
                deleteDialog.dismiss();
                break;

            case R.id.dialog_delete_my_tv_cancle:
                deleteDialog.dismiss();
                break;
        }
    }

    /**
     * 删除统计
     */
    private String delete_result = "网络异常";
    private void deleteTong(String cuId){
        httpPost = new HttpPost(allurl.getDeleteMy());
        SharedPreferences sharedPreferences = myContext.getSharedPreferences("user", myContext.MODE_PRIVATE);
        String userToken = sharedPreferences.getString("user_token", "");
        params.add(new BasicNameValuePair("user_token", userToken));
        params.add(new BasicNameValuePair("id", cuId));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                    httpResponse = new DefaultHttpClient().execute(httpPost);
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        String result = EntityUtils.toString(httpResponse.getEntity());
                        JSONObject jo = new JSONObject(result);
                        delete_result = jo.getString("info");
                        if(jo.getString("status").equals("1")){
                            fhandler.sendEmptyMessage(11);
                        }else{
                            fhandler.sendEmptyMessage(12);
                        }
                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void initListView(){
        fs_pull.setMode(PullToRefreshBase.Mode.BOTH);
        fs_pull.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ScrollView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                if (currentType == 1) {
                    initData("spread");
                } else if (currentType == 2) {
                    initData("xposed");
                } else if (currentType == 3) {
                    initData("view");
                } else if (currentType == 4) {
                    initData("time");
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                if (currentType == 1) {
                    initData("spread");
                } else if (currentType == 2) {
                    initData("xposed");
                } else if (currentType == 3) {
                    initData("view");
                } else if (currentType == 4) {
                    initData("time");
                }
            }
        });
    }

    /**
     * 获得统计
     */
    private String myResult ;
    private void initData(String type){
        allArticle.clear();
        httpPost = new HttpPost(allurl.getStatistics());
        SharedPreferences sharedPreferences = myContext.getSharedPreferences("user", myContext.MODE_PRIVATE);
        String userToken = sharedPreferences.getString("user_token", "");
        params.add(new BasicNameValuePair("user_token", userToken));
        params.add(new BasicNameValuePair("type", type));
        params.add(new BasicNameValuePair("page", "1"));

new Thread(new Runnable() {
    @Override
    public void run() {
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            httpResponse = new DefaultHttpClient().execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String result = EntityUtils.toString(httpResponse.getEntity());
                JSONObject jo = new JSONObject(result);
                myResult = jo.getString("info");
                if(jo.getString("status").equals("1")){
                    JSONArray ja = jo.getJSONArray("data");
                    for(int i = 0;i<ja.length();i++){

                        JSONObject jo_2 = ja.getJSONObject(i);
                        MyArticle myArticle = new MyArticle();
                        myArticle.setIconUrl(jo_2.getString("writing_img"));
                        Log.i("xxxxx", "img=" + jo_2.getString("writing_img"));
                        myArticle.setExposureNum(jo_2.getString("xposed"));
                        myArticle.setClickNum(jo_2.getString("view"));
                        myArticle.setTitle(jo_2.getString("writing_title"));
                        myArticle.setId(jo_2.getString("writing_id"));//查看使用
                        myArticle.setTime(jo_2.getString("update_time"));
                        myArticle.setIid(jo_2.getString("id"));//删除用

                        allArticle.add(myArticle);
                    }
                    fhandler.sendEmptyMessage(1);
                }else{
                    fhandler.sendEmptyMessage(2);
                }

            }


        } catch (Exception e) {
            fhandler.sendEmptyMessage(2);
            e.printStackTrace();
        }
    }
}).start();
    }
    class FsHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                //产看统计成功
                case 1:
                    myFsAdapter.notifyDataSetChanged();
                    setListViewHeightBasedOnChildren(fs_Pull_ListView);
                    fs_pull.onRefreshComplete();
                    tvnull.setVisibility(View.GONE);
                    fs_Pull_ListView.setVisibility(View.VISIBLE);
                    break;
                //查看统计失败
                case 2:
                    fs_pull.onRefreshComplete();
                    tvnull.setText(myResult);
                    tvnull.setVisibility(View.VISIBLE);
                    fs_Pull_ListView.setVisibility(View.GONE);
                    break;
                //删除成功
                case 11:
                    if(currentType ==1){
                        initData("spread");
                    }else if(currentType ==2){
                        initData("xposed");
                    }else if(currentType ==3){
                        initData("view");
                    }else if(currentType ==4){
                        initData("time");
                    }
                    Toast.makeText(myContext,delete_result,Toast.LENGTH_SHORT).show();
                    break;
                //删除失败
                case 12:
                    Toast.makeText(myContext,delete_result,Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    class fs_Adapter extends BaseAdapter  {
        private fs_ViewHolder fs_viewHolder;
        AsynImageLoader asynImageLoader = new AsynImageLoader();
        List<MyArticle> list_myArticles;
        public fs_Adapter(List<MyArticle> list_myArticles){
            this.list_myArticles = list_myArticles;
        }
        @Override
        public int getCount() {
            return list_myArticles.size();
        }

        @Override
        public Object getItem(int position) {
            return list_myArticles.get(position);
        }
        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                fs_viewHolder = new fs_ViewHolder();
                convertView = myInflater.inflate(R.layout.item_statistics,null);
                fs_viewHolder.iv_icon = (ImageView) convertView.findViewById(R.id.item_statistics_icon);
                fs_viewHolder.tv_exposureNum = (TextView) convertView.findViewById(R.id.item_statistics_num);
                fs_viewHolder.tv_clickNum = (TextView) convertView.findViewById(R.id.item_statistics_dianji);
                fs_viewHolder.tv_time = (TextView) convertView.findViewById(R.id.item_statistics_time);
                convertView.setTag(fs_viewHolder);
            }else{
                fs_viewHolder = (fs_ViewHolder) convertView.getTag();
            }

            fs_viewHolder.tv_exposureNum.setText(list_myArticles.get(position).getExposureNum());
            fs_viewHolder.tv_clickNum.setText(list_myArticles.get(position).getClickNum());
            fs_viewHolder.tv_time.setText(list_myArticles.get(position).getTime());

        //    ImageLoader.getInstance().displayImage(list_myArticles.get(position).getIconUrl(), fs_viewHolder.iv_icon, ExampleApplication.getInstance().options2());

            ImageLoader.getInstance().displayImage(list_myArticles.get(position).getIconUrl() == null ? "" : list_myArticles.get(position).getIconUrl(), fs_viewHolder.iv_icon,
                    ExampleApplication.getInstance().getOptions(R.drawable.moren)
                    //,
//                    new SimpleImageLoadingListener() {
//                        @Override
//                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                            super.onLoadingComplete(imageUri, view, loadedImage);
//                            fs_viewHolder.iv_icon.setImageBitmap(loadedImage);
//                        }
//                    }
            );

        //    asynImageLoader.showImageAsyn(fs_viewHolder.iv_icon, list_myArticles.get(position).getIconUrl(), R.drawable.nopic);
            return convertView;
        }
    }


    public class fs_ViewHolder{
        ImageView iv_icon;
        TextView tv_exposureNum;
        TextView tv_clickNum;
        TextView tv_time;
    }

    private String getCurrentTime(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String times = formatter.format(new Date(System.currentTimeMillis()));
        return times;
    }
    /***
     * 动态设置listview的高度 item 总布局必须是linearLayout
     *
     * @param listView
     */
    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1))
                + 15;
        listView.setLayoutParams(params);
    }





    //生成圆角图片
    public static Bitmap GetRoundedCornerBitmap(Bitmap bitmap) {
        try {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(output);
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight());
            final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight()));
            final float roundPx = 14;
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.BLACK);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            final Rect src = new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight());

            canvas.drawBitmap(bitmap, src, rect, paint);
            return output;
        } catch (Exception e) {
            return bitmap;
        }
    }


}