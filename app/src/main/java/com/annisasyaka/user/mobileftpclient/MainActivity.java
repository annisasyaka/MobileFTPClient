package com.annisasyaka.user.mobileftpclient;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    private EditText server, username, password;
    private Button btnLogin;

    boolean error = false;
    String protocol = "SSL";
    FTPSClient ftps;

    SSLContext context;

    public static final String MY_APP_PREFS = "MyAppPrefs";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ftps = new FTPSClient(true);
//        ftps.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

        server = (EditText) findViewById(R.id.add_hostname);
        username = (EditText) findViewById(R.id.add_usrname);
        password = (EditText) findViewById(R.id.add_password);

        btnLogin = (Button) findViewById(R.id.buttonLogin);
        btnLogin.setOnClickListener(this);

        InputStream caInput = getResources().openRawResource(R.raw.certificate);
        try {
            Certificate ca = CertificateFactory.getInstance("X.509").generateCertificate(caInput);
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

// Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

// Create an SSLContext that uses our TrustManager
            context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
        } catch (CertificateException e) {
            Log.e("Certificate", e.getLocalizedMessage());

        } catch (NoSuchAlgorithmException e) {
            Log.e("Certificate", e.getLocalizedMessage());

        } catch (KeyStoreException e) {
            Log.e("Certificate", e.getLocalizedMessage());

        } catch (KeyManagementException e) {
            Log.e("Certificate", e.getLocalizedMessage());

        } catch (IOException e) {
            Log.e("Certificate", e.getLocalizedMessage());

        }

    }

    @Override
    public void onClick(View v){
               if (v.getId() == btnLogin.getId()){
                   new login().execute();
               }
           }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        /*if (id = R.id.action_settings){
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    private class login extends AsyncTask<String, Integer, String>{

        String result = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try
            {
                int replay;

                ftps.setSocketFactory(context.getSocketFactory());
                ftps.connect(server.getText().toString());
                System.out.println("Connected to " + server.getText().toString() + ".");

                replay = ftps.getReplyCode();

                if (!FTPReply.isPositiveCompletion(replay)){
                    ftps.disconnect();
//                    System.err.println("FTP server refused connection.");
//                    System.exit(1);
                    return null;
                }
                ftps.setBufferSize(1000);

                if(!ftps.login(username.getText().toString()
                        ,password.getText().toString())){
                    ftps.logout();
                    error = true;
                    System.exit(1);
                }

                ftps.setFileType(FTP.BINARY_FILE_TYPE);
                ftps.enterLocalPassiveMode();
                ftps.sendCommand("OPTS UTF8 ON");

                //Globals.global_ftps = ftps;



            }
            catch (IOException e){
//                if (ftps.isConnected()){
//                    try
//                    {
//                        ftps.disconnect();
//                    }
//                    catch (IOException f){
//                        //do nothing
//                    }
//                }
                System.err.println("Could not connect to server");
                e.printStackTrace();
                Log.e("Login", e.getLocalizedMessage() );
                error = true;

                return null;
//                System.exit(1);
            }
            //log in

            System.out.println(Environment.getDataDirectory());
            System.out.println(Environment.getExternalStorageDirectory());
            return "success";
        }

        @Override
        protected void onPostExecute(String s) {
            if (s!=null){
                Toast.makeText(MainActivity.this, "Berhasil Yeay", Toast.LENGTH_SHORT).show();
                Globals.ftps = ftps;
                Intent i = new Intent(getApplicationContext(), Files.class);
                startActivity(i);
            } else {
                Toast.makeText(MainActivity.this, "Gagal", Toast.LENGTH_SHORT).show();
            }
        }
    }


}

