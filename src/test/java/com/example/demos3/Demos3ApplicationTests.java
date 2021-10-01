package com.example.demos3;

import com.example.demos3.service.AssetType;
import com.example.demos3.service.StorageService;
import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Demos3ApplicationTests {

	@Autowired
	StorageService storageService;

	@Test
	void testFileUpload() {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("test.txt").getFile());

		String path = storageService.createAsset(file, AssetType.TEST);

		byte[] fileBytes = storageService.downloadAsset(file.getName(),AssetType.TEST);

		Assertions.assertNotNull(fileBytes);


	}

}
