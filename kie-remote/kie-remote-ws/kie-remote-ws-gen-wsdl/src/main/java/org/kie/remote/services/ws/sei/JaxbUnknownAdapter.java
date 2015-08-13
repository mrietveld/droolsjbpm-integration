package org.kie.remote.services.ws.sei;

/*
 * Copyright 2010 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.kie.remote.services.ws.sei.CollectionWrapper.CollectionType;

public class JaxbUnknownAdapter extends XmlAdapter<Object, Object> {

    @Override
    public Object marshal( Object o ) throws Exception {
        Set<Object> marshalledObjs = new HashSet<Object>();
        if( o instanceof Collection ) {
            return marshal(o, marshalledObjs);
        } else if( o instanceof Map ) {
            return marshal(o, marshalledObjs);
        } else {
            return o;
        }
    }

    public Object marshal( Object o, Set<Object> marshalledObjs ) {
        if( o == null ) {
            return null;
        }
        if( !o.getClass().getPackage().getName().startsWith("java") ) {
            return o;
        }
        if( o instanceof Collection ) {
            if( !marshalledObjs.add(o) ) {
                throw new IllegalStateException("Recursively linked collections are not supported!");
            }
            CollectionType type;
            if( o instanceof List ) {
                type = CollectionType.LIST;
            } else if( o instanceof Set ) {
                type = CollectionType.SET;
            } else if( o instanceof Queue ) {
                type = CollectionType.QUEUE;
            } else {
                throw new IllegalArgumentException("Unknown collection type encountered during marhalling: "
                        + o.getClass().getName());
            }
            Object[] origElements = ((Collection) o).toArray();
            Object[] elements = new Object[origElements.length];
            for( int i = 0; i < origElements.length; ++i ) {
                elements[i] = marshal(origElements[i], marshalledObjs);
            }
            return new CollectionWrapper(elements, type);
        } else if( o instanceof Map ) {
            if( !marshalledObjs.add(o) ) {
                throw new IllegalStateException("Recursively linked collections are not supported!");
            }
            Map<String, Object> map = (Map<String, Object>) o; 
            StringObjectEntryList entryList = new StringObjectEntryList();
            for( Entry<String, Object> entry : map.entrySet() ) { 
                Object newVal = marshal(entry.getValue(), marshalledObjs);
                StringObjectEntry xmlEntr = new StringObjectEntry(entry.getKey(), newVal);
                entryList.getEntries().add(xmlEntr);
            }
            marshalledObjs.remove(o);
            return entryList;
        } else {
            return o;
        }
    }

    @Override
    public Object unmarshal( Object o ) throws Exception {
        if( o instanceof CollectionWrapper ) {
            CollectionWrapper c = (CollectionWrapper) o;
            Collection newCollection;
            Object [] elements = c.getElements();
            switch( c.getType() ) { 
            case LIST:
                newCollection = new ArrayList(elements.length);
                break;
            case SET:
                newCollection = new HashSet(elements.length);
                break;
            case QUEUE:
                newCollection = new ArrayDeque(elements.length);
                break;
            default:
                throw new IllegalStateException("Unexpected collection type: " + c.getType() );
            }
            List newElemlist = new ArrayList(elements.length);
            for( Object elem : elements ) { 
               Object newElem = unmarshal(elem);
               newElemlist.add(newElem);
            }
            newCollection.addAll(newElemlist);
            return newCollection;
        } else if( o instanceof StringObjectEntryList ) {
            StringObjectEntryList soel = (StringObjectEntryList) o;
            Map<String, Object> map = new HashMap<String, Object>();
            for( StringObjectEntry keyValue : soel.getEntries() ) { 
                Object newVal = unmarshal(keyValue.getValue());
                map.put(keyValue.getKey(), newVal);
            }
            return map;
        } else {
            return o;
        }
    }

}
