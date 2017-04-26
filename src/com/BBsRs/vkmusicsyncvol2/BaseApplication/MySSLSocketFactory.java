package com.BBsRs.vkmusicsyncvol2.BaseApplication;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.holoeverywhere.widget.Toast;

import android.content.Context;
import android.os.AsyncTask;

import com.BBsRs.vkmusicsyncvol2.LoginActivity;
public class MySSLSocketFactory extends SSLSocketFactory {
    SSLContext sslContext = SSLContext.getInstance("TLS");
    
    public MySSLSocketFactory(KeyStore truststore, final AsyncTask<String, String, String> AuthTask, final Context context) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
        super(truststore);

        TrustManager tm = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException  {
            	try {
            		chain[0].checkValidity();
            	} catch (Exception e){
                	AuthTask.cancel(true);
                	((LoginActivity) context).runOnUiThread(new Runnable(){
    					@Override
    					public void run() {
    						Toast.makeText(context, "There a problem with the security certificate for this web site. Cancel Loging in", Toast.LENGTH_LONG).show();
    					}
            		});
            		e.printStackTrace();
            	}
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        sslContext.init(null, new TrustManager[] { tm }, null);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }
}
