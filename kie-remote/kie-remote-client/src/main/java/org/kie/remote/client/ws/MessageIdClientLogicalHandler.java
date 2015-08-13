package org.kie.remote.client.ws;

import static org.kie.services.shared.KieRemoteWebServiceContstants.WS_ADDR_NAMESPACE;
import static org.kie.services.shared.KieRemoteWebServiceContstants.WS_SECURITY_UTILITY_NAMESPACE;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.cxf.ws.addressing.RelatesToType;
import org.kie.services.shared.KieRemoteWebServiceContstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ideas in the code stolen from https://agiv-security.googlecode.com/hg/agiv-security-client/src/main/java/be/agiv/security/handler/WSAddressingHandler.java
 * 
 *
 */
public class MessageIdClientLogicalHandler implements SOAPHandler<SOAPMessageContext> {

    protected static final Logger logger = LoggerFactory.getLogger(MessageIdClientLogicalHandler.class);
    
    private static final String TO_ID_CONTEXT_ATTRIBUTE = MessageIdClientLogicalHandler.class.getName() + ".toId";

    private static final String MESSAGE_ID_CONTEXT_ATTRIBUTE = MessageIdClientLogicalHandler.class.getName() + ".messageId";
   

    private final JAXBContext jaxbContext;

    private String action;

    private String to;

    public MessageIdClientLogicalHandler(JAXBContext jaxbContext, String action, String to) {
        this.jaxbContext = jaxbContext;
        this.action = action;
        this.to = to;
    }
    
    @Override
    public boolean handleMessage( SOAPMessageContext context ) {
        Boolean outboundProperty = (Boolean) context .get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (true == outboundProperty.booleanValue()) {
            try {
                handleOutboundMessage(context);
            } catch (SOAPException e) {
                throw new ProtocolException(e);
            }
        } else {
            handleInboundMessage(context);
        }
        return true;
    }

    @Override
    public boolean handleFault( SOAPMessageContext context ) {
        return true;
    }

    @Override
    public void close( MessageContext context ) {
        // DBG Auto-generated method stub
        
    }

    @Override
    public Set<QName> getHeaders() {
        // DBG Auto-generated method stub
        return null;
    }


    private void handleInboundMessage(SOAPMessageContext context) {
        String messageId = (String) context.get(MESSAGE_ID_CONTEXT_ATTRIBUTE);
        // logger.debug("checking RelatesTo message id: " + messageId);
        Object[] headers = context.getHeaders(new QName(KieRemoteWebServiceContstants.WS_ADDR_NAMESPACE, "RelatesTo"), this.jaxbContext, false);
        for (Object headerObject : headers) {
            JAXBElement<RelatesToType> element = (JAXBElement<RelatesToType>) headerObject;
            RelatesToType relatesTo = element.getValue();
            if (false == messageId.equals(relatesTo.getValue())) {
                throw new ProtocolException("incorrect a:RelatesTo value");
            }
        }
    }

    private void handleOutboundMessage(SOAPMessageContext context)
            throws SOAPException {
        logger.debug("adding WS-Addressing headers");
        SOAPEnvelope envelope = context.getMessage().getSOAPPart().getEnvelope();
        SOAPHeader header = envelope.getHeader();
        if (null == header) {
            header = envelope.addHeader();
        }

        String wsuPrefix = null;
        String wsAddrPrefix = null;
        Iterator namespacePrefixesIter = envelope.getNamespacePrefixes();
        while (namespacePrefixesIter.hasNext()) {
            String namespacePrefix = (String) namespacePrefixesIter.next();
            String namespace = envelope.getNamespaceURI(namespacePrefix);
            if (WS_ADDR_NAMESPACE.equals(namespace)) {
                wsAddrPrefix = namespacePrefix;
            } else if (WS_SECURITY_UTILITY_NAMESPACE.equals(namespace)) {
                wsuPrefix = namespacePrefix;
            }
        }
        if (null == wsAddrPrefix) {
            wsAddrPrefix = getUniquePrefix("a", envelope);
            envelope.addNamespaceDeclaration(wsAddrPrefix, WS_ADDR_NAMESPACE);
        }
        if (null == wsuPrefix) {
            /*
             * Using "wsu" is very important for the IP-STS X509 credential.
             * Apparently the STS refuses when the namespace prefix of the
             * wsu:Id on the WS-Addressing To element is different from the
             * wsu:Id prefix on the WS-Security timestamp.
             */
            wsuPrefix = "wsu";
            envelope.addNamespaceDeclaration(wsuPrefix, WS_SECURITY_UTILITY_NAMESPACE);
        }

        SOAPFactory factory = SOAPFactory.newInstance();

        SOAPHeaderElement actionHeaderElement = header.addHeaderElement(new QName(WS_ADDR_NAMESPACE, "Action", wsAddrPrefix));
        actionHeaderElement.setMustUnderstand(true);
        actionHeaderElement.addTextNode(this.action);

        SOAPHeaderElement messageIdElement = header.addHeaderElement(new QName(WS_ADDR_NAMESPACE, "MessageID", wsAddrPrefix));
        String messageId = "urn:uuid:" + UUID.randomUUID().toString();
        context.put(MESSAGE_ID_CONTEXT_ATTRIBUTE, messageId);
        messageIdElement.addTextNode(messageId);

        SOAPHeaderElement replyToElement = header.addHeaderElement(new QName(WS_ADDR_NAMESPACE, "ReplyTo", wsAddrPrefix));
        SOAPElement addressElement = factory.createElement("Address", wsAddrPrefix, WS_ADDR_NAMESPACE);
        addressElement.addTextNode("http://www.w3.org/2005/08/addressing/anonymous");
        replyToElement.addChildElement(addressElement);

        SOAPHeaderElement toElement = header.addHeaderElement(new QName(WS_ADDR_NAMESPACE, "To", wsAddrPrefix));
        toElement.setMustUnderstand(true);

        toElement.addTextNode(this.to);

        String toIdentifier = "to-id-" + UUID.randomUUID().toString();
        toElement.addAttribute(new QName(WS_SECURITY_UTILITY_NAMESPACE, "Id", wsuPrefix),
                toIdentifier);
        try {
            toElement.setIdAttributeNS(WS_SECURITY_UTILITY_NAMESPACE, "Id", true);
        } catch (UnsupportedOperationException e) {
            // Axis2 has missing implementation of setIdAttributeNS
            logger.error("error setting Id attribute: " + e.getMessage());
        }
        context.put(TO_ID_CONTEXT_ATTRIBUTE, toIdentifier);
    }
    
    private String getUniquePrefix(String preferredPrefix, SOAPEnvelope envelope) {
        int suffixNr = 0;
        boolean conflict;
        String prefix = preferredPrefix;
        do {
            conflict = false;
            Iterator namespacePrefixesIter = envelope.getNamespacePrefixes();
            while (namespacePrefixesIter.hasNext()) {
                String existingPrefix = (String) namespacePrefixesIter.next();
                if (prefix.equals(existingPrefix)) {
                    conflict = true;
                    break;
                }
            }
            if (conflict) {
                suffixNr++;
                prefix = preferredPrefix + suffixNr;
            }
        } while (conflict);
        return prefix;
    }
}
