package org.kie.remote.services.ws;

import static org.kie.services.shared.KieRemoteWebServiceContstants.*;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WsAddressingServiceLogicalHandler implements SOAPHandler<SOAPMessageContext> {


    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public void close( MessageContext arg0 ) {
        // no-op
    }

    @Override
    public boolean handleMessage( SOAPMessageContext context ) {
        Boolean isOutbound = (Boolean) context.get(SOAPMessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if( isOutbound ) {
            try {
                SOAPEnvelope envelope = context.getMessage().getSOAPPart().getEnvelope();
                SOAPHeader header = envelope.getHeader();

                /* extract the generated MessageID */
                String messageID = getMessageID(header);
                context.put(MESSAGE_ID, messageID);
                context.setScope(MESSAGE_ID, Scope.APPLICATION);

                /* change ReplyTo address */
                setReplyTo(header, (String) context.get(REPLY_TO));
            } catch( Exception ex ) {
                throw new RuntimeException(ex);
            }
        }

        return true;
    }
    
    protected void setReplyTo( SOAPHeader header, String address ) {
        /* change ReplyTo address using DOM */
        NodeList nodeListReplyTo = header.getElementsByTagName(REPLY_TO);
        NodeList nodeListAddress = nodeListReplyTo.item(0).getChildNodes();
        for( int i = 0; i < nodeListAddress.getLength(); i++ ) {
            Node node = nodeListAddress.item(i);
            if( "Address".equals(node.getLocalName()) ) {
                node.setTextContent(address);
                break;
            }
        }
    }

    protected String getMessageID( SOAPHeader header ) {
        NodeList nodeListMessageId = header.getElementsByTagName(MESSAGE_ID);
        return nodeListMessageId.item(0).getTextContent();
    }

    protected String getRelatesTo( SOAPHeader header ) {
        NodeList nodeListMessageId = header.getElementsByTagName(RELATES_TO);
        return nodeListMessageId.item(0).getTextContent();
    }

    protected String getReplyTo( SOAPHeader header ) {
        NodeList nodeListReplyTo = header.getElementsByTagName(REPLY_TO);
        NodeList nodeListAddress = nodeListReplyTo.item(0).getChildNodes();
        for( int i = 0; i < nodeListAddress.getLength(); i++ ) {
            Node node = nodeListAddress.item(i);
            if( "Address".equals(node.getLocalName()) ) {
                return node.getTextContent();
            }
        }
        return null;
    }

    public boolean handleFault( SOAPMessageContext context ) {
        return true;
    }



}