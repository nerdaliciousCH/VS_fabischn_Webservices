package ch.ethz.inf.vs.a2.fabischn.webservices;

import android.net.Uri;

import ch.ethz.inf.vs.a2.fabischn.webservices.http.HttpRawRequest;
import ch.ethz.inf.vs.a2.fabischn.webservices.http.RemoteServerConfiguration;

/**
 * Created by fabian on 16.10.16.
 */

public class HttpRawRequestImpl implements HttpRawRequest, RemoteServerConfiguration {
    private static final String CRLF= "\r\n";
    @Override
    public String generateRequest(String host, int port, String path) {
        String request = "GET " + path + " HTTP/1.1" + CRLF
                + "Host: " + host + ":" + Integer.toString(port)+ CRLF
                + "Accept: text/html" + CRLF
                + "Connection: close" + CRLF
                + CRLF;
        return request;
    }
}
