package org.kie.remote.services.ws.security;

import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.commons.codec.binary.Base64;
import org.jboss.errai.security.shared.exception.FailedAuthenticationException;
import org.jboss.errai.security.shared.service.AuthenticationService;

import com.google.common.base.Charsets;

public class BasicAuthenticationHandler implements SOAPHandler<SOAPMessageContext> {

    private final static Logger logger = Logger.getLogger(BasicAuthenticationHandler.class);

    @Inject
    private AuthenticationService authenticationService;

    private String realmName = "Kie Remote Webservices Default Realm";
    
    @Override
    public boolean handleMessage( SOAPMessageContext context ) {
        Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        HttpServletResponse response = (HttpServletResponse) context.get(MessageContext.SERVLET_RESPONSE);
        if( outboundProperty.booleanValue() ) { 
            HttpServletRequest request = (HttpServletRequest) context.get(MessageContext.SERVLET_REQUEST);
           if( authenticate(request) ) { 
               
           } else { 
               try { 
                   challengeClient(request, response);
               } catch( IOException ioe ) { 
                   
               }
           }
        } else { 
            
        }
        return outboundProperty;
    }

    @Override
    public boolean handleFault( SOAPMessageContext context ) {
        return false;
    }

    @Override
    public void close( MessageContext context ) {
        // do nothing
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    public void challengeClient( final HttpServletRequest request, final HttpServletResponse response ) throws IOException {
        response.setHeader( "WWW-Authenticate", "Basic realm=\"" + this.realmName + "\"" );

        // we don't expect an ajax client, but just in case.. 
        if ( isAjaxRequest( request ) ) {
            response.sendError( HttpServletResponse.SC_FORBIDDEN );
        } else {
            response.sendError( HttpServletResponse.SC_UNAUTHORIZED );
        }
    }
    
    private boolean authenticate( final HttpServletRequest req ) {
        final String authHead = req.getHeader( "Authorization" );

        if ( authHead != null ) {
            final int index = authHead.indexOf( ' ' );
            final String[] credentials = new String( Base64.decodeBase64( authHead.substring( index ) ), Charsets.UTF_8 ).split( ":" );

            try {
                authenticationService.login( credentials[ 0 ], credentials[ 1 ] );
                return true;
            } catch ( final FailedAuthenticationException e ) {
                return false;
            }
        }

        return false;
    }

    private boolean isAjaxRequest( HttpServletRequest request ) {
        return request.getHeader( "X-Requested-With" ) != null && "XMLHttpRequest".equalsIgnoreCase( request.getHeader( "X-Requested-With" ) );
    }
}
