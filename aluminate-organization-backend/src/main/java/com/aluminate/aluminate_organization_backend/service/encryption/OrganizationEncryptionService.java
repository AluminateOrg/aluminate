package com.aluminate.aluminate_organization_backend.service.encryption;


import com.aluminate.aluminate_organization_backend.config.util.RSAEncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.PrivateKey;
import java.security.PublicKey;

@Service
public class OrganizationEncryptionService {
    private final PrivateKey orgPrivateKey;
    private final PublicKey globalPublicKey;

    @Autowired
    public OrganizationEncryptionService(
            @Value("${encryption.organization.private-key}") String orgPrivateKeyPem,
            @Value("${encryption.global.public-key}") String globalPublicKeyPem) throws Exception {

        this.orgPrivateKey = RSAEncryptionUtil.privateKeyFromPem(orgPrivateKeyPem);
        this.globalPublicKey = RSAEncryptionUtil.publicKeyFromPem(globalPublicKeyPem);
    }

    public String encryptForGlobal(String plaintext) throws Exception {
        return RSAEncryptionUtil.encrypt(plaintext, globalPublicKey);
    }

    public String decryptFromGlobal(String ciphertext) throws Exception {
        return RSAEncryptionUtil.decrypt(ciphertext, orgPrivateKey);
    }
}