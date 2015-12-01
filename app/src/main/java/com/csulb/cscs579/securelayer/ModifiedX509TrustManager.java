package com.csulb.cscs579.securelayer;

import android.util.Log;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by JAY on 10/30/15.
 */
public class ModifiedX509TrustManager implements X509TrustManager {
    X509TrustManager defaultTrustMgr;
    boolean accepted;

    public ModifiedX509TrustManager() {
        TrustManager managers[] = {null};
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            String algo = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(algo);
            tmf.init(ks);
            managers = tmf.getTrustManagers();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < managers.length; i++) {
            if (managers[i] instanceof X509TrustManager) {
                defaultTrustMgr = (X509TrustManager) managers[i];
                return;
            }
        }
        accepted = true;
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        defaultTrustMgr.checkClientTrusted(chain, authType);
    }

    public void setAccepted(boolean acc) {
        accepted = acc;
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        // First, check with the default.
        try {
            defaultTrustMgr.checkServerTrusted(chain, authType);
            // if there is no exception, return happily.
        } catch (CertificateException ce) {
            if (accepted) {
                // YES. They've accepted it in the setup dialog.
                return;
            } else {
                Log.e("Device Not trusted!", "Check Settings.");
                throw ce;
            }
        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        return defaultTrustMgr.getAcceptedIssuers();
    }

}