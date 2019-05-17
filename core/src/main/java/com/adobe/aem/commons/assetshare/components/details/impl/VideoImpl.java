package com.adobe.aem.commons.assetshare.components.details.impl;

import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditionParameters;
import com.adobe.aem.commons.assetshare.content.renditions.AssetRenditions;
import com.day.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Required;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.adobe.aem.commons.assetshare.components.details.Video;
import com.adobe.aem.commons.assetshare.content.AssetModel;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.commons.util.DamUtil;

/**
 *
 * Sling Model for Video Component
 *
 */
@Model(
        adaptables = { SlingHttpServletRequest.class },
        adapters = { Video.class },
        resourceType = { VideoImpl.RESOURCE_TYPE },
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class VideoImpl extends AbstractEmptyTextComponent implements Video {
    protected static final String RESOURCE_TYPE = "asset-share-commons/components/details/video";

    @Self
    @Required
    private SlingHttpServletRequest request;

    @Self
    @Required
    private AssetModel asset;

    @OSGiService
    @Required
    private AssetRenditions assetRenditions;

    /**
     * @deprecated replaced by renditionName
     */
    @Deprecated
    @ValueMapValue
    private String computedProperty;

    /**
     * @deprecated replaced by renditionName
     */
    @Deprecated
    @ValueMapValue
    private String renditionRegex;

    @ValueMapValue
    @Default(booleanValues = false)
    private boolean legacyMode;

    @ValueMapValue
    private String renditionName;

    private ValueMap combinedProperties;

    private String src = null;

    @PostConstruct
    public void init() {
        if (asset != null) {
            combinedProperties = asset.getProperties();
        }
    }

    @Override
    public boolean isEmpty() {
        return StringUtils.isBlank(getSrc());
    }

    @Override
    public boolean isReady() {
        return !isEmpty();
    }

    @Override
    public String getSrc() {
        if (src == null) {
            if (!legacyMode && StringUtils.isNotBlank(renditionName)) {
                final AssetRenditionParameters parameters =
                        new AssetRenditionParameters(asset, renditionName, false);
                src = assetRenditions.getUrl(request, asset, parameters);
            } else if (src == null) {
                src = getLegacySrc();
            }

            src = StringUtils.replace(src, "%20", " ");
            return Text.escapePath(src);
        }

        return src;
    }

    @Deprecated
    private String getLegacySrc() {
        String src = combinedProperties.get(computedProperty, String.class);

        if (StringUtils.isBlank(src) && StringUtils.isNotBlank(renditionRegex)) {
            fetchSrcFromRegex();
        }

        return src;
    }

    /**
     * Method fetches the rendition path from regex
     */
    @Deprecated
    private void fetchSrcFromRegex() {
        final Pattern pattern = Pattern.compile(renditionRegex);

        for (final Rendition rendition : asset.getRenditions()) {
            if (!"video/x-flv".equalsIgnoreCase(rendition.getMimeType())
                    && pattern.matcher(rendition.getName()).matches()) {
                src = rendition.getPath();
                break;
            }
        }
    }

    @Override
    public boolean isVideoAsset() {
        if (null != asset && null != asset.getResource()) {
            return DamUtil.isVideo(asset.getResource().adaptTo(Asset.class));
        }

        return false;
    }
}
