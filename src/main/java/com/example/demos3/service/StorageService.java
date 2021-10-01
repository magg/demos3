package com.example.demos3.service;

import java.io.File;

/**
 * External Storage interface.
 */
public interface StorageService {

    /**
     * Creates an asset in an external storage service.
     *
     * @param file      the asset to put
     * @param assetType asset type
     * @return path for the asset
     */
    String createAsset(File file, AssetType assetType);

    /**
     *
     * @param name
     * @param assetType
     * @return
     */

    byte[] downloadAsset(String name, AssetType assetType);
}
