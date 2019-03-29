/*
 * Asset Share Commons
 *
 * Copyright (C) 2019 Adobe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.adobe.aem.commons.assetshare.content.renditions;

import com.adobe.aem.commons.assetshare.content.AssetModel;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.annotation.versioning.ProviderType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@ProviderType
public interface AssetRenditions {
    String VAR_ASSET_PATH = "${asset.path}";
    String VAR_ASSET_NAME = "${asset.name}";
    String VAR_ASSET_EXTENSION = "${asset.extension}";
    String VAR_RENDITION_NAME = "${rendition.name}";

    /**
     * @return a list of all registered AssetRenditionDispatchers in the system ordered in Descending order by service.ranking.
     */
    List<AssetRenditionDispatcher> getAssetRenditionDispatchers();

    /**
     * Derives the renditionName from the request which is typically used to resolve the AssetRenditionDispatcher and the mapping there-in.
     * <p>
     * This typically pulls the first segment of the suffix, however this logic may change overtime if the scheme changes.
     *
     * @param request the request TO the AssetRenditionServlet.
     *
     * @return the renditionName parsed from the
     */
    String getRenditionName(SlingHttpServletRequest request);

    /**
     * Creates a URL to the AssetRenditionServlet for the
     *
     * @param request the request object.
     * @param asset   the asset to rendition.
     * @param params  the params which describe how the AssetRenditionServlet should handle sending the asset rendition to the response.
     *
     * @return the URL that can be used to HTTP GET Request the specified asset rendition (does NOT include scheme/host/port).
     */
    String getUrl(SlingHttpServletRequest request, AssetModel asset, UrlParams params);

    /**
     * Creates a Map that is used to provide the label/values to drive the AssetRenditionsDatasource dropdown.
     * <p>
     * This is a convenience method, however each AssetRenditionDispatcher implementation can implement their own getOptions() rather than wrapping a call to this method.
     *
     * @param mappings the raw mappings from the
     *
     * @return the Options is the format LABEL:VALUE
     */
    Map<String, String> getOptions(Map<String, ? extends Object> mappings);

    /**
     * Replaces the 'variables' in the expressions with the corresponding bits derived from the request.
     *
     * @param request the request object to the AssetRenditionServlet.
     * @param expression the expression to replace the variables in.
     * @return the expression with the variables replaced with values derived from the request.
     */
    String evaluateExpression(SlingHttpServletRequest request, String expression);

    /**
     *
     */
    final class UrlParams<T> {
        private String renditionName;
        private boolean download;
        private ValueMap otherProperties;

        public UrlParams() {
            this.otherProperties = new ValueMapDecorator(new HashMap<>());
        }

        public UrlParams(final String renditionName, final boolean download) {
            this.renditionName = renditionName;
            this.download = download;
            this.otherProperties = new ValueMapDecorator(new HashMap<>());
        }

        public UrlParams(final String renditionName, final boolean download, final Map<String, Object> otherProperties) {
            this.renditionName = renditionName;
            this.download = download;
            this.otherProperties = new ValueMapDecorator(otherProperties);
        }

        public String getRenditionName() {
            return renditionName;
        }

        public void setRenditionName(String renditionName) {
            this.renditionName = renditionName;
        }

        public boolean isDownload() {
            return download;
        }

        public void setDownload(boolean download) {
            this.download = download;
        }

        public void put(String key, final Object value) {
            this.otherProperties.put(key, value);
        }

        public T get(String key, T defaultValue) {
            return otherProperties.get(key, defaultValue);
        }

        public T get(String key, Class<T> clazz) {
            return otherProperties.get(key, clazz);
        }
    }
}
