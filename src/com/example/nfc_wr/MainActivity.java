package com.example.nfc_wr;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author 瑞泽_Zerui 感应NFC标签后弹出的页面 注:谷歌推荐的数据格式为NDEF,所以本次DEMO的读写都是采用该格式.当然了,你还可以尝试其它格式的开发.
 */
public class MainActivity extends Activity {
    private TextView ifo_NFC;
    private NfcAdapter nfcAdapter;
    private String readResult = "";
    private PendingIntent pendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private boolean isFirst = true;
    private Button toWBtn;
    private IntentFilter ndef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 该方法完成接收到Intent时的初始化工作
        init();
    }


    /**
     * 检测工作,判断设备的NFC支持情况
     * 
     * @return
     */
    private Boolean ifNFCAvailable() {
        if (nfcAdapter == null) {
            ifo_NFC.setText("设备不支持NFC！");
            finish();
            return false;
        }
        if (nfcAdapter != null && !nfcAdapter.isEnabled()) {
            ifo_NFC.setText("请在系统设置中先启用NFC功能！");
            finish();
            return false;
        }
        return true;
    }

    /**
     * 初始化过程
     */
    private void init() {
        toWBtn = (Button) findViewById(R.id.toWBtn);
        toWBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(MainActivity.this, Write2Nfc.class);
                startActivity(intent);
            }
        });
        ifo_NFC = (TextView) findViewById(R.id.ifo_NFC);
        // NFC适配器，所有的关于NFC的操作从该适配器进行
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (!ifNFCAvailable()) {
            return;
        }
        // 将被调用的Intent，用于重复被Intent触发后将要执行的跳转
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        // 设定要过滤的标签动作，这里只接收ACTION_NDEF_DISCOVERED类型
        ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        ndef.addCategory("*/*");
        mFilters = new IntentFilter[] {ndef};// 过滤器
        mTechLists = new String[][] {new String[] {NfcA.class.getName()}, new String[] {NfcF.class.getName()}, new String[] {NfcB.class.getName()}, new String[] {NfcV.class.getName()}};// 允许扫描的标签类型

        if (isFirst) {
            if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
                System.out.println(getIntent().getAction());
                if (readFromTag(getIntent())) {
                    ifo_NFC.setText(readResult);
                    System.out.println("1.5...");
                } else {
                    ifo_NFC.setText("标签数据为空");
                }
            }
            isFirst = false;
        }
        System.out.println("onCreate...");
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
        System.out.println("onPause...");
    }

    /*
     * 重写onResume回调函数的意义在于处理多次读取NFC标签时的情况 (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        // 前台分发系统,这里的作用在于第二次检测NFC标签时该应用有最高的捕获优先权.
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, mFilters, mTechLists);


        System.out.println("onResume...");
    }

    /*
     * 有必要要了解onNewIntent回调函数的调用时机,请自行上网查询 (non-Javadoc)
     * 
     * @see android.app.Activity#onNewIntent(android.content.Intent)
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        System.out.println("onNewIntent1...");
        System.out.println(intent.getAction());
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            System.out.println("onNewIntent2...");
            if (readFromTag(intent)) {
                ifo_NFC.setText(readResult);
                System.out.println("onNewIntent3...");
            } else {
                ifo_NFC.setText("标签数据为空");
            }
        }

    }

    /**
     * 读取NFC标签数据的操作
     * 
     * @param intent
     * @return
     */
    private boolean readFromTag(Intent intent) {
        Parcelable[] rawArray = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawArray != null) {
            NdefMessage mNdefMsg = (NdefMessage) rawArray[0];
            NdefRecord mNdefRecord = mNdefMsg.getRecords()[0];
            try {
                if (mNdefRecord != null) {
                    readResult = new String(mNdefRecord.getPayload(), "UTF-8");
                    return true;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return false;
        }
        return false;
    }



}
