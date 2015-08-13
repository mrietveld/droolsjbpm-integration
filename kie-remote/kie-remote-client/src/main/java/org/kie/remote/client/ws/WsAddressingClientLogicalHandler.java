package org.kie.remote.client.ws;

import static org.kie.services.shared.KieRemoteWebServiceContstants.MESSAGE_ID;
import static org.kie.services.shared.KieRemoteWebServiceContstants.WS_ADDR_NAMESPACE;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.kie.remote.client.api.exception.RemoteApiException;

public class WsAddressingClientLogicalHandler implements SOAPHandler<SOAPMessageContext> {


    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public void close( MessageContext arg0 ) {
        // no-op
    }

    @Override
    public boolean handleFault( SOAPMessageContext context ) {
        return true;
    }
    
    @Override
    public boolean handleMessage( SOAPMessageContext context ) {
        Boolean isOutbound = (Boolean) context.get(SOAPMessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if( isOutbound ) {
            try {
                SOAPEnvelope envelope = context.getMessage().getSOAPPart().getEnvelope();

                // get WS-Addressing namespace prefix
                String wsAddrPrefix = null;
                Iterator namespacePrefixesIter = envelope.getNamespacePrefixes();
                while (namespacePrefixesIter.hasNext()) {
                    String namespacePrefix = (String) namespacePrefixesIter.next();
                    String namespace = envelope.getNamespaceURI(namespacePrefix);
                    if (WS_ADDR_NAMESPACE.equals(namespace)) {
                        wsAddrPrefix = namespacePrefix;
                    } 
                }
                if (null == wsAddrPrefix) {
                    // not even sure if a unique prefix is necessary, given that we're in control of the WSDL?
                    wsAddrPrefix = getUniquePrefix("wsa", envelope);
                    envelope.addNamespaceDeclaration(wsAddrPrefix, WS_ADDR_NAMESPACE);
                }

                // add unique message id
                SOAPHeader header = envelope.getHeader();
                SOAPHeaderElement messageIdElement = header.addHeaderElement(new QName(WS_ADDR_NAMESPACE, "MessageID", wsAddrPrefix));
                String messageId = "urn:uuid:" + UUID.randomUUID().toString();
                messageIdElement.addTextNode(messageId);
                
                // DBG
                System.out.println(messageId);

                // add the message id to the message context
                context.put(MESSAGE_ID, messageId);
                context.setScope(MESSAGE_ID, Scope.APPLICATION);
            } catch( Exception e ) {
                throw new RemoteApiException("Unable to add unique message id to outgoing request: "  + e.getMessage(), e);
            }
        }

        return true;
    }

    private String getUniquePrefix(String preferredPrefix, SOAPEnvelope envelope) {
        int suffixNr = 0;
        boolean notUnique = true;
        String prefix = preferredPrefix;
        UNIQUE: do {
            Iterator namespacePrefixesIter = envelope.getNamespacePrefixes();
            while (namespacePrefixesIter.hasNext() ) {
                String existingPrefix = (String) namespacePrefixesIter.next();
                if (prefix.equals(existingPrefix)) {
                    suffixNr++;
                    prefix = preferredPrefix + suffixNr;
                    continue UNIQUE;
                }
            }
            notUnique = false;
        } while (notUnique);
        
        return prefix;
    }

}