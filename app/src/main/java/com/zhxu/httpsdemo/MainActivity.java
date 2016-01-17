package com.zhxu.httpsdemo;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class MainActivity extends Activity {

    private static final String HTTPS_URL = "https://192.168.56.1:8443" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        runHttpsRequestWithHttpsURLConnection();
    }


    /**
     * 将输入流转换成字符串
     * @param in
     * @return
     */
    public String getStreamString(InputStream in){


        if(in != null){
            try {
                BufferedReader bfr = new BufferedReader(new InputStreamReader(in)) ;
                StringBuffer sb = new StringBuffer() ;
                String result = new String("");
                while((result = bfr.readLine() )!= null){
                    sb.append(result) ;
                }
                return sb.toString() ;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return null ;
    }


    /**
     * 通过HTTPS访问
     */
    private void runHttpsRequestWithHttpsURLConnection(){
        AsyncTask<String, Void, String> testTask = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String result = "";
                HttpsURLConnection conn = null;
                try {
                    URL url = new URL(HTTPS_URL);
                    conn = (HttpsURLConnection) url.openConnection();
                    conn.setSSLSocketFactory(setCertificates(MainActivity.this.getAssets().open("server.cer")));
                    conn.connect();
                    result = getStreamString(conn.getInputStream());
                    return result;
                } catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                System.out.println("result:"+result);
            }
        };

        testTask.execute();
    }


    public SSLSocketFactory setCertificates(InputStream... certificates){
        try{
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates){
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));

                try{
                    if (certificate != null)
                        certificate.close();
                } catch (IOException e){
                    e.printStackTrace() ;
                }
            }

            //取得SSL的SSLContext实例
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.
                    getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            //初始化keystore
            KeyStore clientKeyStore = KeyStore.getInstance("BKS");
            clientKeyStore.load(getAssets().open("zhxu_client.bks"), "123456".toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientKeyStore, "123456".toCharArray());

            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
            return sslContext.getSocketFactory() ;

        } catch (Exception e){
            e.printStackTrace();
        }
        return null ;
    }
}
