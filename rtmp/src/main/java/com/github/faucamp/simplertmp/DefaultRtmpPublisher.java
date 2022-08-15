package com.github.faucamp.simplertmp;

import com.github.faucamp.simplertmp.io.RtmpConnection;
import net.ossrs.rtmp.ConnectCheckerRtmp;

/**
 * Srs implementation of an RTMP publisher
 *
 * @author francois, leoma, pedro
 */
public class DefaultRtmpPublisher implements RtmpPublisher {

  private RtmpConnection rtmpConnection;
//  private RtmpConnection rtmpConnection2;

  public DefaultRtmpPublisher(ConnectCheckerRtmp connectCheckerRtmp) {
    rtmpConnection = new RtmpConnection(connectCheckerRtmp);
//    rtmpConnection2 = new RtmpConnection(connectCheckerRtmp);
  }

  @Override
  public boolean connect(String url) {
//    boolean tt =  rtmpConnection2.connect(url+"2");
    return rtmpConnection.connect(url);
  }

  @Override
  public boolean publish(String publishType) {
//    boolean tt =  rtmpConnection2.publish(publishType);
    return rtmpConnection.publish(publishType);
  }

  @Override
  public void close() {
    rtmpConnection.close();
//    rtmpConnection2.close();
  }

  @Override
  public void publishVideoData(byte[] data, int size, int dts) {
    rtmpConnection.publishVideoData(data, size, dts);
//    rtmpConnection2.publishVideoData(data, size, dts);

  }

  @Override
  public void publishAudioData(byte[] data, int size, int dts) {
    rtmpConnection.publishAudioData(data, size, dts);
//    rtmpConnection2.publishAudioData(data, size, dts);
  }

  @Override
  public void setVideoResolution(int width, int height) {
    rtmpConnection.setVideoResolution(width, height);
//    rtmpConnection2.setVideoResolution(width, height);
  }

  @Override
  public void setAuthorization(String user, String password) {
    rtmpConnection.setAuthorization(user, password);
//    rtmpConnection2.setAuthorization(user, password);
  }

  @Override
  public void setLogs(boolean enable) {
    rtmpConnection.setLogs(enable);
//    rtmpConnection2.setLogs(enable);
  }
}
