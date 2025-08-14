package com.aluminate.aluminate_organization_backend;

import com.aluminate.aluminate_organization_backend.config.util.RSAEncryptionUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.KeyPair;


@SpringBootTest()
class AluminateOrganizationBackendApplicationTests {

	@Test
	void contextLoads() throws Exception {
		KeyPair orgKeyPair = RSAEncryptionUtil.generateKeyPair();
		String orgPrivate = RSAEncryptionUtil.privateKeyToPem(orgKeyPair.getPrivate());
		String orgPublic = RSAEncryptionUtil.publicKeyToPem(orgKeyPair.getPublic());
		System.out.println("Organization Private Key: " + orgPrivate);
		System.out.println("Organization Public Key: " + orgPublic);
	}

}
