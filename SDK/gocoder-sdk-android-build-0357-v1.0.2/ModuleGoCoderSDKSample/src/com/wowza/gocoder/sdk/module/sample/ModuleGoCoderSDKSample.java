/*
 *  This is sample code provided by Wowza Media Systems, LLC.  All sample code is intended to be a reference for the
 *  purpose of educating developers, and is not intended to be used in any production environment.
 *
 *  IN NO EVENT SHALL WOWZA MEDIA SYSTEMS, LLC BE LIABLE TO YOU OR ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL,
 *  OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION,
 *  EVEN IF WOWZA MEDIA SYSTEMS, LLC HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  WOWZA MEDIA SYSTEMS, LLC SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. ALL CODE PROVIDED HEREUNDER IS PROVIDED "AS IS".
 *  WOWZA MEDIA SYSTEMS, LLC HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 *  Copyright © 2016 Wowza Media Systems, LLC. All rights reserved.
 */

package com.wowza.gocoder.sdk.module.sample;

import com.wowza.wms.application.*;

import java.util.List;

import com.wowza.wms.amf.*;
import com.wowza.wms.client.*;
import com.wowza.wms.module.*;
import com.wowza.wms.request.*;
import com.wowza.wms.response.ResponseFunction;
import com.wowza.wms.stream.*;
import com.wowza.wms.media.model.MediaCodecInfoAudio;
import com.wowza.wms.media.model.MediaCodecInfoVideo;

public class ModuleGoCoderSDKSample extends ModuleBase {

    private static final String CLASSNAME = ModuleGoCoderSDKSample.class.getSimpleName();

    private static final String RESULT_SUCCESS 	= "_result";
    private static final String RESULT_ERROR 	= "_error";

    private IApplicationInstance mAppInstance = null;

    /**
     * The handler invoked when a new application instance is started
     * @param appInstance The application instance
     */
    public void onAppStart(IApplicationInstance appInstance) {
        String fullname = appInstance.getApplication().getName() + "/" + appInstance.getName();
        getLogger().info(CLASSNAME+".onAppStart: " + fullname);

        this.mAppInstance = appInstance;
    }

    /**
     * Th handler invoked when an application instance is stopped
     * @param appInstance The application instance
     */
    public void onAppStop(IApplicationInstance appInstance) {
        String fullname = appInstance.getApplication().getName() + "/" + appInstance.getName();
        getLogger().info(CLASSNAME+".onAppStop: " + fullname);
    }

    /**
     * Event handler for the custom onScreenPress() data event
     *
     * @param client The client where the event originated
     * @param function The request function
     * @param functionParams The event parameters (x, y, and occurred for onScreenPress)
     *
     */
    public void onScreenPress(IClient client, RequestFunction function, AMFDataList functionParams) {
        getLogger().info(CLASSNAME+".onScreenPress() call received with the following parameters: " + functionParams.toString());
    }

    /**
     * Event handler for the custom onGetPingTime() data event. When invoked, this handler will
     * submit an asynchronous request to measure the round trip ping time
     * to and from the client who originated the event. When the request has completed, the
     * results will be sent back to the client where they can be processed via the callback
     * function specified when the event was originally sent.
     *
     * @param client The client where the event originated
     * @param function The request function
     * @param functionParams The event parameters (null for onGetPingTime)
     *
     */
    public void onGetPingTime(IClient client, RequestFunction function, AMFDataList functionParams) {
        getLogger().info(CLASSNAME+".onGetPingTime() module function call received from client id " + client.getClientId()
                + " with the following parameters: " + functionParams.toString());

        final double clientFunctionId = ((Double)functionParams.get(1).getValue()).doubleValue();
        // make sure the client sent a callback function id
        if (clientFunctionId <= 0)
            return;

        // send a ping request to client
        client.ping(new IModulePingResult() {
            @Override
            // called when a result is received from the ping request
            public void onResult(IClient client, long pingTime, int pingId, boolean result) {
                AMFDataObj resultParams = new AMFDataObj();

                if (result) {
                    // return the ping time to the client in the result parameter named "pingTime"
                    resultParams.put("pingTime", client.getPingRoundTripTime());
                } else {
                    // an error occurred, return an error code and ddescription
                    resultParams.put("code", "Ping.Timeout");
                    resultParams.put("description", "The ping request timed out.");
                }

                // Return the result back to the client callback with the function id s
                // specified in the originating function call request
                getLogger().info(CLASSNAME+".onGetPingTime(): returning result to client id " + client.getClientId()
                        + " with the following parameters: " + resultParams.toString());

                ResponseFunction resultResponse = new ResponseFunction(client);
                resultResponse.createDefaultMessage(result ? RESULT_SUCCESS : RESULT_ERROR, clientFunctionId);
                resultResponse.addBody(new AMFDataItem((double)client.getClientId()));
                resultResponse.addBody(resultParams);
                client.getRespFunctions().add(resultResponse);
            }
        });
    }

    /**
     * The handler invoked when a client connection is accepted. In this case, each of the other clients
     * will be sent an onConnectAccept() data event with the client id and ip address as parameters
     *
     * @param connectedClient The newly connected client
     */
    public void onConnectAccept(IClient connectedClient) {
        getLogger().info(CLASSNAME+".onConnectAccept() event call received for client id " + connectedClient.getClientId());

        AMFDataObj clientCallParams = new AMFDataObj();
        clientCallParams.put("clientId", connectedClient.getClientId());
        clientCallParams.put("clientIp", connectedClient.getIp());

        // Inform each of the other stream clients that a new client has connected, passing it's id and ip address
        List<IClient> allClients = mAppInstance.getClients();
        for(IClient client : allClients) {
            if (client.getClientId() != connectedClient.getClientId()) {
                getLogger().info("Sending onClientConnected() event to client id " + client.getClientId()
                        + " with the following parameters:" + clientCallParams.toString());

                // Send the function call request to the client, specifying a callback to be
                // invoked if the client function call returns a non-null result
                client.call("onClientConnected", new IModuleCallResult() {
                    @Override
                    public void onResult(IClient fromClient, RequestFunction requestFunction, AMFDataList resultParams) {
                        getLogger().info(CLASSNAME+".onClientConnected(): result received from client id " + fromClient.getClientId() + " with the following parameters:" + resultParams.toString());
                    }
                }, clientCallParams);
            }
        }
    }

    /**
     * The handler invoked when a client disconnects. In this case, each of the other clients
     * will be sent an onDisconnect() data event with the client id and ip address as parameters
     *
     * @param disconnectedClient The client that has disconnected
     */
    public void onDisconnect(IClient disconnectedClient) {
        getLogger().debug(CLASSNAME+".onDisconnect() event received for client id " + disconnectedClient.getClientId());

        AMFDataObj clientCallParams = new AMFDataObj();
        clientCallParams.put("clientId", disconnectedClient.getClientId());
        clientCallParams.put("clientIp", disconnectedClient.getIp());

        // Inform each of the other stream clients that a client has disconnected, passing it's id and ip address
        List<IClient> allClients = mAppInstance.getClients();
        for(IClient client : allClients) {
            if (client.getClientId() != disconnectedClient.getClientId()) {
                getLogger().info(CLASSNAME+".onDisconnect: Sending onClientDisconnected for client id " + disconnectedClient.getClientId() + " to client id " + client.getClientId());
                client.call("onClientDisconnected", null, clientCallParams);
            }
        }
    }

    /**
     * The handler invoked when a new stream is created
     *
     * @param stream The newly created stream
     */
    public void onStreamCreate(IMediaStream stream) {
        /*
         * Register a callback to intercept the onDeviceOrientation() stream event when sent by stream publishers
         */
        stream.registerCallback("onDeviceOrientation", new IMediaStreamCallback() {
            public void onCallback(IMediaStream stream, RequestFunction function, AMFDataList callParams) {
                getLogger().info(CLASSNAME+".onDeviceOrientation() stream event received with the following parameters:"  + callParams.toString());
            }
        });

        /*
         * Add a client listener callback to be invoked when various stream actions occur
         */
        stream.addClientListener(new IMediaStreamActionNotify3() {

            @Override
            public void onCodecInfoVideo(IMediaStream stream, MediaCodecInfoVideo codecInfoVideo) {
                getLogger().debug(CLASSNAME+".onCodecInfoVideo[" + codecInfoVideo.getFrameWidth() + "x" + codecInfoVideo.getFrameHeight() + "]");
            }

            @Override
            public void onCodecInfoAudio(IMediaStream stream, MediaCodecInfoAudio codecInfoAudio) {
                // Auto-generated method stub
            }

            @Override
            public void onMetaData(IMediaStream stream, AMFPacket metaDataPacket) {
                // Auto-generated method stub
            }

            @Override
            public void onPauseRaw(IMediaStream stream, boolean isPause, double location) {
                // Auto-generated method stub
            }

            @Override
            public void onPause(IMediaStream stream, boolean isPause, double location) {
                // Auto-generated method stub
            }

            @Override
            public void onPlay(IMediaStream stream, String streamName, double playStart, double playLen,
                               int playReset) {
                // Auto-generated method stub
            }

            @Override
            public void onPublish(IMediaStream stream, String streamName, boolean isRecord, boolean isAppend) {
                // Auto-generated method stub
            }

            @Override
            public void onSeek(IMediaStream stream, double location) {
                // Auto-generated method stub
            }

            @Override
            public void onStop(IMediaStream stream) {
                // Auto-generated method stub
            }

            @Override
            public void onUnPublish(IMediaStream stream, String streamName, boolean isRecord, boolean isAppend) {
                // Auto-generated method stub
            }

        });

    }


/*  Additional methods that can be automatically invoked in response to various connection and stream events

    public void onConnect(IClient client, RequestFunction function, AMFDataList params) {
        getLogger().debug(CLASSNAME+".onConnect: " + client.getClientId());
    }

    public void onConnectReject(IClient client) {
        getLogger().debug(CLASSNAME+".onConnectReject: " + client.getClientId());
    }

    public void onStreamDestroy(IMediaStream stream) {
        getLogger().debug(CLASSNAME+".onStreamDestroy: " + stream.getSrc());
    }

    public void onHTTPSessionCreate(IHTTPStreamerSession httpSession) {
        getLogger().debug(CLASSNAME+".onHTTPSessionCreate: " + httpSession.getSessionId());
    }

    public void onHTTPSessionDestroy(IHTTPStreamerSession httpSession) {
        getLogger().debug(CLASSNAME+".onHTTPSessionDestroy: " + httpSession.getSessionId());
    }

    public void onHTTPCupertinoStreamingSessionCreate(HTTPStreamerSessionCupertino httpSession) {
        getLogger().debug(CLASSNAME+".onHTTPCupertinoStreamingSessionCreate: " + httpSession.getSessionId());
    }

    public void onHTTPCupertinoStreamingSessionDestroy(HTTPStreamerSessionCupertino httpSession) {
        getLogger().debug(CLASSNAME+".onHTTPCupertinoStreamingSessionDestroy: " + httpSession.getSessionId());
    }

    public void onHTTPSmoothStreamingSessionCreate(HTTPStreamerSessionSmoothStreamer httpSession) {
        getLogger().debug(CLASSNAME+".onHTTPSmoothStreamingSessionCreate: " + httpSession.getSessionId());
    }

    public void onHTTPSmoothStreamingSessionDestroy(HTTPStreamerSessionSmoothStreamer httpSession) {
        getLogger().debug(CLASSNAME+".onHTTPSmoothStreamingSessionDestroy: " + httpSession.getSessionId());
    }

    public void onRTPSessionCreate(RTPSession rtpSession) {
        getLogger().debug(CLASSNAME+".onRTPSessionCreate: " + rtpSession.getSessionId());
    }

    public void onRTPSessionDestroy(RTPSession rtpSession) {
        getLogger().debug(CLASSNAME+".onRTPSessionDestroy: " + rtpSession.getSessionId());
    }

    public void onCall(String handlerName, IClient client, RequestFunction function, AMFDataList params) {
        getLogger().debug(CLASSNAME+".onCall: " + handlerName);
    }
*/


}
