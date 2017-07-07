package com.eve.httpssample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn:
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startHttpsConnection();
                    }
                });
                thread.start();
                break;
            default:
                break;
        }
    }

    public void startHttpsConnection() {
        HttpsURLConnection httpsURLConnection = null;
        BufferedReader reader = null;
        try {

            SSLContext sc = SSLContext.getInstance("SSL");
            TrustManager[] trustManagers = createTrustManager();
            if (trustManagers == null) {
                Log.e("TAG", "tmf");
                return;
            }
            sc.init(null, trustManagers, new SecureRandom());
            URL url = new URL("https://kyfw.12306.cn/otn/login/init");
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            httpsURLConnection.setConnectTimeout(5000);
            httpsURLConnection.setDoInput(true);
            httpsURLConnection.setUseCaches(false);
            httpsURLConnection.connect();


            reader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
            StringBuilder sBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sBuilder.append(line);
            }
            Log.e("TAG", "Wiki content=" + sBuilder.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } finally {
            if (httpsURLConnection != null) {
                httpsURLConnection.disconnect();
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private TrustManager[] createTrustManager() {
        BufferedInputStream cerInputStream = null;
        try {
            // 获取客户端存放的服务器公钥证书
            cerInputStream = new BufferedInputStream(getAssets().open("srca.cer"));
            // 根据公钥证书生成Certificate对象
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate ca = cf.generateCertificate(cerInputStream);
            Log.e("TAG", "ca=" + ((X509Certificate) ca).getSubjectDN());

            // 生成包含当前CA证书的keystore
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // 使用包含指定CA证书的keystore生成TrustManager[]数组
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);
            return tmf.getTrustManagers();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            if (cerInputStream != null) {
                try {
                    cerInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
