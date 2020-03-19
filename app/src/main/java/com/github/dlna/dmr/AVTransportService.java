package com.github.dlna.dmr;

import android.util.Log;

import com.github.dlna.Utils;

import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportErrorCode;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.avtransport.AbstractAVTransportService;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.model.DeviceCapabilities;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PlayMode;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.SeekMode;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportSettings;
import org.seamless.http.HttpFetch;
import org.seamless.util.URIUtil;

import java.net.URI;
import java.util.Map;

public class AVTransportService extends AbstractAVTransportService {
    private static final String TAG = "GstAVTransportService";

    final private Map<UnsignedIntegerFourBytes, MediaPlayer> players;

    AVTransportService(LastChange lastChange,
                       Map<UnsignedIntegerFourBytes, MediaPlayer> players) {
        super(lastChange);
        this.players = players;
    }

    private Map<UnsignedIntegerFourBytes, MediaPlayer> getPlayers() {
        return players;
    }

    private MediaPlayer getInstance(UnsignedIntegerFourBytes instanceId)
            throws AVTransportException {
        MediaPlayer player = getPlayers().get(instanceId);
        if (player == null) {
            throw new AVTransportException(AVTransportErrorCode.INVALID_INSTANCE_ID);
        }
        return player;
    }

    @Override
    public void setAVTransportURI(UnsignedIntegerFourBytes instanceId,
                                  String currentURI,
                                  String currentURIMetaData) throws AVTransportException {
        Log.d(TAG, currentURI + "---" + currentURIMetaData);
        URI uri;
        try {
            uri = new URI(currentURI);
        } catch (Exception ex) {
            throw new AVTransportException(
                    ErrorCode.INVALID_ARGS, "CurrentURI can not be null or malformed"
            );
        }

        if (currentURI.startsWith("http:")) {
            try {
                HttpFetch.validate(URIUtil.toURL(uri));
            } catch (Exception ex) {
                throw new AVTransportException(
                        AVTransportErrorCode.RESOURCE_NOT_FOUND, ex.getMessage()
                );
            }
        } else if (!currentURI.startsWith("file:")) {
            throw new AVTransportException(
                    ErrorCode.INVALID_ARGS, "Only HTTP and file: resource identifiers are supported"
            );
        }

        String type = "audio";
        if (!currentURIMetaData.contains("object.item.audioItem")) {
            throw new AVTransportException(ErrorCode.ILLEGAL_MIME_TYPE, "Only support audio type");
        }
        String name = currentURIMetaData.substring(currentURIMetaData.indexOf("<dc:title>") + 10,
                currentURIMetaData.indexOf("</dc:title>"));
        getInstance(instanceId).setURI(uri, type, name, currentURIMetaData);
    }

    @Override
    public MediaInfo getMediaInfo(UnsignedIntegerFourBytes instanceId)
            throws AVTransportException {
        return getInstance(instanceId).getCurrentMediaInfo();
    }

    @Override
    public TransportInfo getTransportInfo(UnsignedIntegerFourBytes instanceId)
            throws AVTransportException {
        return getInstance(instanceId).getCurrentTransportInfo();
    }

    @Override
    public PositionInfo getPositionInfo(UnsignedIntegerFourBytes instanceId)
            throws AVTransportException {
        return getInstance(instanceId).getCurrentPositionInfo();
    }

    @Override
    public DeviceCapabilities getDeviceCapabilities(UnsignedIntegerFourBytes instanceId)
            throws AVTransportException {
        getInstance(instanceId);
        return new DeviceCapabilities(new StorageMedium[]{StorageMedium.NETWORK});
    }

    @Override
    public TransportSettings getTransportSettings(UnsignedIntegerFourBytes instanceId)
            throws AVTransportException {
        getInstance(instanceId);
        return new TransportSettings(PlayMode.NORMAL);
    }

    @Override
    public void stop(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        getInstance(instanceId).stop();
    }

    @Override
    public void play(UnsignedIntegerFourBytes instanceId, String speed)
            throws AVTransportException {
        getInstance(instanceId).play();
    }

    @Override
    public void pause(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
        getInstance(instanceId).pause();
    }

    @Override
    public void record(UnsignedIntegerFourBytes instanceId) {
        // Not implemented
    }

    @Override
    public void seek(UnsignedIntegerFourBytes instanceId, String unit, String target)
            throws AVTransportException {
        SeekMode seekMode;
        try {
            seekMode = SeekMode.valueOrExceptionOf(unit);

            if (!seekMode.equals(SeekMode.REL_TIME)) {
                throw new IllegalArgumentException();
            }

            int pos = Utils.getRealTime(target) * 1000;
            getInstance(instanceId).seek(pos);
        } catch (IllegalArgumentException ex) {
            throw new AVTransportException(
                    AVTransportErrorCode.SEEKMODE_NOT_SUPPORTED, "Unsupported seek mode: " + unit
            );
        }
    }

    @Override
    public void next(UnsignedIntegerFourBytes instanceId) {
        // Not implemented
    }

    @Override
    public void previous(UnsignedIntegerFourBytes instanceId) {
        // Not implemented
    }

    @Override
    public void setNextAVTransportURI(UnsignedIntegerFourBytes instanceId,
                                      String nextURI,
                                      String nextURIMetaData) {
        // Not implemented
    }

    @Override
    public void setPlayMode(UnsignedIntegerFourBytes instanceId, String newPlayMode) {
        // Not implemented
    }

    @Override
    public void setRecordQualityMode(UnsignedIntegerFourBytes instanceId,
                                     String newRecordQualityMode) {
        // Not implemented
    }

    @Override
    protected TransportAction[] getCurrentTransportActions(UnsignedIntegerFourBytes instanceId)
            throws Exception {
        return getInstance(instanceId).getCurrentTransportActions();
    }

    @Override
    public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
        UnsignedIntegerFourBytes[] ids = new UnsignedIntegerFourBytes[getPlayers().size()];
        int i = 0;
        for (UnsignedIntegerFourBytes id : getPlayers().keySet()) {
            ids[i] = id;
            i++;
        }
        return ids;
    }
}
