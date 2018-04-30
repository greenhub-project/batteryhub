/*
 * Copyright (c) 2016 Hugo Matalonga & João Paulo Fernandes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hmatalonga.greenhub.models;

import android.content.pm.PackageInfo;
import android.content.pm.Signature;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.LinkedList;
import java.util.List;

import com.hmatalonga.greenhub.models.data.AppSignature;
import com.hmatalonga.greenhub.util.LogUtils;
import com.hmatalonga.greenhub.util.StringHelper;

import static com.hmatalonga.greenhub.util.LogUtils.logE;

/**
 * Signatures.
 */
public class Signatures {

    private static final String TAG = "Signatures";

    public static List<AppSignature> getSignatureList(PackageInfo pak) {
        List<AppSignature> signatureList = new LinkedList<>();
        String[] pmInfos = pak.requestedPermissions;

        if (pmInfos != null) {
            byte[] bytes = Permissions.getPermissionBytes(pmInfos);
            String hexB = StringHelper.convertToHex(bytes);
            signatureList.add(new AppSignature(hexB));
        }
        Signature[] sigs = pak.signatures;

        for (Signature s : sigs) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("SHA-1");
                md.update(s.toByteArray());
                byte[] dig = md.digest();
                // Add SHA-1
                signatureList.add(new AppSignature(StringHelper.convertToHex(dig)));

                CertificateFactory fac = CertificateFactory.getInstance("X.509");
                if (fac == null)
                    continue;
                X509Certificate cert = (X509Certificate) 
                        fac.generateCertificate(new ByteArrayInputStream(s.toByteArray()));
                if (cert == null)
                    continue;
                PublicKey pkPublic = cert.getPublicKey();
                if (pkPublic == null)
                    continue;
                String al = pkPublic.getAlgorithm();
                switch (al) {
                    case "RSA": {
                        md = MessageDigest.getInstance("SHA-256");
                        RSAPublicKey rsa = (RSAPublicKey) pkPublic;
                        byte[] data = rsa.getModulus().toByteArray();
                        if (data[0] == 0) {
                            byte[] copy = new byte[data.length - 1];
                            System.arraycopy(data, 1, copy, 0, data.length - 1);
                            md.update(copy);
                        } else
                            md.update(data);
                        dig = md.digest();
                        // Add SHA-256 of modulus
                        signatureList.add(new AppSignature(StringHelper.convertToHex(dig)));
                        break;
                    }
                    case "DSA": {
                        DSAPublicKey dsa = (DSAPublicKey) pkPublic;
                        md = MessageDigest.getInstance("SHA-256");
                        byte[] data = dsa.getY().toByteArray();
                        if (data[0] == 0) {
                            byte[] copy = new byte[data.length - 1];
                            System.arraycopy(data, 1, copy, 0, data.length - 1);
                            md.update(copy);
                        } else
                            md.update(data);
                        dig = md.digest();
                        // Add SHA-256 of public key (DSA)
                        signatureList.add(new AppSignature(StringHelper.convertToHex(dig)));
                        break;
                    }
                    default:
                        LogUtils.logE(TAG, "Weird algorithm: " + al + " for " + pak.packageName);
                        break;
                }
            } catch (NoSuchAlgorithmException | CertificateException e) {
                e.printStackTrace();
            }

        }
        return signatureList;
    }
}
