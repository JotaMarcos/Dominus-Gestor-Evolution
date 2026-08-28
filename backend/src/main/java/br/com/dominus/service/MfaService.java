package br.com.dominus.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MfaService {
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public String generateNewSecret() {
        final GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    public String getQrCodeUrl(String userEmail, String secretKey) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL("DominusGestor", userEmail,
                new GoogleAuthenticatorKey.Builder(secretKey).build());
    }

    public boolean verifyCode(String secretKey, int code) {
        return gAuth.authorize(secretKey, code);
    }
}
